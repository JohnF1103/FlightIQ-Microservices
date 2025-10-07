package New_Foreflight.Weather.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import New_Foreflight.Weather.database.AirportNode;
import New_Foreflight.Weather.database.AirportRepository;
import New_Foreflight.Weather.dto.AirportWeatherResponse;

/**
 * Utility class for WeatherService providing helper methods for parsing and caching weather data.
 */
@Component
public class WeatherServiceUtility {

    // Neo4j repository for airport data.
    @Autowired
    private AirportRepository airportRepository;

    private static Cache<String, AirportWeatherResponse> weatherCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    // Cached map of airports with corresponding winds aloft data.
    // Key is the airport code and value is a list of winds aloft in order of increased altitude.
    private static Cache<String, String[]> windsAloftData = CacheBuilder.newBuilder().build();
    // List of altitudes for which winds aloft data is available.
    private static List<Integer> windsAloftAltitudes;

    @FunctionalInterface
    protected static interface DataParser<T> {
        T parse(Object data);
    }

    protected static void addToWeatherCache(String icao, AirportWeatherResponse response) {
        weatherCache.put(icao, response);
    }

    protected static AirportWeatherResponse getWeatherCache(String icao) {
        return weatherCache.getIfPresent(icao);
    }

    protected static double calculateStandardTemperature(double altitude) {
        // Standard temperature at sea level is 15°C
        final double SEA_LEVEL_STANDARD_TEMP = 15.0;
        // Temperature decreases by 2°C per 1000 feet
        final double TEMP_DECREASE_RATE = 2.0;
        // Calculate the standard temperature at the given altitude
        double standardTemperature = SEA_LEVEL_STANDARD_TEMP - (altitude / 1000.0) * TEMP_DECREASE_RATE;

        return standardTemperature;
    }

    /*
     * Computes the density alttude for an airport at a given pressure altitude.
     * 
     * Using the OAT (outside air temp) and ISA which is the standard temperature at a given altitude, use the helper
     * function to compute this.
     * 
     * Implement the formula DA = Pressure_Altitude + (120 x (OAT – ISA))
     */
    protected static double computeDensityAltitude(HashMap<String, Object> weatherComponents) {
        double alt = Double.parseDouble(weatherComponents.get("elevation").toString());
        double degF = Double.parseDouble(weatherComponents.get("temperature").toString().split(" ")[0]);
        double degC = (degF - 32) / 1.8;
        double isa = calculateStandardTemperature(alt);

        return alt + (120 * (degC - isa));
    }

    /**
     * Adds a METAR component to the map if present in the JSON response.
     */
    protected static <T> void addComponentIfPresent(JSONObject result, String key, LinkedHashMap<String, Object> map,
            DataParser<T> parser) {
        if (result.has(key) && !result.isNull(key))
            map.put(key, parser.parse(result.get(key)));
    }

    protected static String parseWinds(Object windDataObj) {
        JSONObject windData = (JSONObject) windDataObj;
        int direction = windData.optInt("degrees", 0);
        int speedKts = windData.optInt("speed_kts", 0);
        int gustKts = windData.optInt("gust_kts", 0);

        return gustKts > 0 ? String.format("%d at %d-%d kts", direction, speedKts, gustKts)
                : String.format("%d at %d kts", direction, speedKts);
    }

    protected static String parseVisibility(Object visibilityDataObj) {
        JSONObject visibilityData = (JSONObject) visibilityDataObj;

        return visibilityData.optString("miles") + " SM";
    }

    /*
     * parses the clouds obj into a list of cloud ceilings and adds to the metar components. only displays if sky
     * conditions are not clear.
     *
     */
    protected static List<HashMap<String, String>> parseClouds(Object cloudsDataObj) {
        JSONArray cloudsArray = (JSONArray) cloudsDataObj;
        List<HashMap<String, String>> cloudsList = new ArrayList<>();
        JSONObject cloud;
        LinkedHashMap<String, String> cloudMap;
        String skyCode;

        for (int i = 0; i < cloudsArray.length(); i++) {
            cloud = cloudsArray.getJSONObject(i);
            cloudMap = new LinkedHashMap<>();
            skyCode = cloud.optString("code", "Unknown");

            cloudMap.put("code", skyCode);

            if (!"CLR".equalsIgnoreCase(skyCode))
                cloudMap.put("feet", cloud.optString("feet", "Unknown"));
            cloudsList.add(cloudMap);
        }
        return cloudsList;
    }

    protected static String parseTemperature(Object temperatureDataObj) {
        JSONObject tempData = (JSONObject) temperatureDataObj;

        return String.format("%s degrees F, %s degrees C", tempData.optString("fahrenheit"),
                tempData.optString("celsius"));
    }

    protected static String parseDewpoint(Object dewpointDataObj) {
        JSONObject dewpointData = (JSONObject) dewpointDataObj;

        return String.format("%s degrees F, %s degrees C", dewpointData.optString("fahrenheit"),
                dewpointData.optString("celsius"));
    }

    protected static String parsePositionString(Object positionObject) {
        JSONObject pSONObject = (JSONObject) positionObject;
        String orientation = pSONObject.getJSONObject("orientation").optString("from");
        String distance = pSONObject.getJSONObject("distance").optString("miles");
        return distance + " miles " + orientation;
    }

    protected static String parsePressure(Object pressureDataObj) {
        JSONObject pressureData = (JSONObject) pressureDataObj;

        return "hg: " + pressureData.optString("hg");
    }

    protected static String parseHumidity(Object humidityDataObj) {
        JSONObject humidityData = (JSONObject) humidityDataObj;

        return humidityData.optString("percent") + " %";
    }

    protected static String parseElevation(Object elevationDataObj) {
        JSONObject elevationData = (JSONObject) elevationDataObj;

        return elevationData.optString("feet");
    }

    private AirportNode getAirportFromIcao(String icaoCode) {
        AirportNode airport = airportRepository.findByIcao(icaoCode)
                .orElseThrow(() -> new RuntimeException("Airport not found"));

        return airport;
    }

    private boolean isWindsAloftAirport(String icaoCode) {
        try {
            return getAirportFromIcao(icaoCode).isWindsAloftAirport();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static double calculateHaversineNM(double lat1, double lon1, double lat2, double lon2) {
        // Earth radius in nautical miles.
        final double R = 3440.065;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private Pair<String, Double> getClosestAirport(String airportCode) {
        AirportNode sourceAirport = getAirportFromIcao(airportCode);
        double sourceLat = sourceAirport.getLatitude();
        double sourceLon = sourceAirport.getLongitude();
        List<AirportNode> windsAloftAirports = new ArrayList<>();

        airportRepository.findAll().forEach(airport -> {
            if (airport.isWindsAloftAirport() && !airport.getIcao().equals(airportCode))
                windsAloftAirports.add(airport);
        });
        AirportNode closest = null;
        double minDist = Double.MAX_VALUE;

        for (AirportNode airport : windsAloftAirports) {
            double distNM = calculateHaversineNM(sourceLat, sourceLon, airport.getLatitude(), airport.getLongitude());

            if (distNM < minDist) {
                minDist = distNM;
                closest = airport;
            }
        }

        if (closest != null)
            return Pair.of(closest.getIcao(), minDist);
        else
            return Pair.of("", Double.MAX_VALUE);
    }

    private Pair<String, Double> getClosestAirport(double latitude, double longitude) {
        List<AirportNode> windsAloftAirports = new ArrayList<>();

        airportRepository.findAll().forEach(airport -> {
            if (airport.isWindsAloftAirport())
                windsAloftAirports.add(airport);
        });
        AirportNode closest = null;
        double minDist = Double.MAX_VALUE;

        for (AirportNode airport : windsAloftAirports) {
            double distNM = calculateHaversineNM(latitude, longitude, airport.getLatitude(), airport.getLongitude());

            if (distNM < minDist) {
                minDist = distNM;
                closest = airport;
            }
        }

        if (closest != null)
            return Pair.of(closest.getIcao(), minDist);
        else
            return Pair.of("", Double.MAX_VALUE);
    }

    /**
     * Clears the windsAloftData cache at 12:01AM GMT, 6:01AM GMT, 12:01PM GMT, and 6:01PM GMT daily.
     */
    @Scheduled(cron = "0 1 0/6 * * ?", zone = "GMT")
    protected static void clearWindsAloftDataCache() {
        windsAloftData.invalidateAll();
    }

    private void fetchWindsAloftData(String windsAloftApiUrl) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(windsAloftApiUrl, String.class);

        if (response == null || response.isEmpty()) {
            System.err.println("No winds aloft data available.");
            return;
        }
        String[] lines = response.split("\n");
        String[] windSpeeds;
        String line, airportCode;

        for (int i = 8; i < lines.length; i++) {
            line = lines[i];
            airportCode = "K" + line.substring(0, 3);

            if (!isWindsAloftAirport(airportCode))
                continue;
            windSpeeds = new String[9];

            windSpeeds[0] = line.substring(4, 8).trim();
            windSpeeds[1] = line.substring(9, 16).trim();
            windSpeeds[2] = line.substring(17, 24).trim();
            windSpeeds[3] = line.substring(25, 32).trim();
            windSpeeds[4] = line.substring(33, 40).trim();
            windSpeeds[5] = line.substring(41, 48).trim();
            windSpeeds[6] = line.substring(49, 54).trim();
            windSpeeds[7] = line.substring(56, 61).trim();
            windSpeeds[8] = line.substring(62, 69).trim();

            for (int j = 0; j < windSpeeds.length; j++)
                if (windSpeeds[j].isEmpty())
                    windSpeeds[j] = "N/A";
            windsAloftData.put(airportCode, windSpeeds);
        }
    }

    private void initializeWindsAloftData(String windsAloftApiUrl) {
        // If the pre-set altitudes list is empty, assign the default values.
        if (windsAloftAltitudes == null || windsAloftAltitudes.isEmpty())
            windsAloftAltitudes = Lists.newArrayList(3000, 6000, 9000, 12000, 18000, 24000, 30000, 34000, 39000);

        // If the cache is empty, fetch the winds aloft data from the API.
        if (windsAloftData.asMap().isEmpty())
            fetchWindsAloftData(windsAloftApiUrl);
    }

    private static Pair<String, String> decodeWindsAloftRaw(String windsAloftRaw) {
        try {
            if (windsAloftRaw.equals("9900"))
                return Pair.of("VARIABLE", "LIGHT");
            String windCode;

            if (windsAloftRaw.contains("+"))
                windCode = windsAloftRaw.split("\\+")[0];
            else if (windsAloftRaw.contains("-"))
                windCode = windsAloftRaw.split("-")[0];
            else
                windCode = windsAloftRaw;

            if (windCode.length() == 4) {
                int direction = Integer.parseInt(windCode.substring(0, 2)) * 10;
                int speed = Integer.parseInt(windCode.substring(2));

                return Pair.of(direction + "", speed + "");
            }

            if (windCode.length() == 6) {
                int direction = Integer.parseInt(windCode.substring(0, 2)) * 10;
                int speed = Integer.parseInt(windCode.substring(2, 4));

                if (100 <= speed && speed <= 199) {
                    if (100 <= direction && direction <= 199) {
                        direction = direction - 50;
                        speed = speed - 100;
                    } else {
                        direction = direction + 50;
                        speed = speed + 100;
                    }
                }

                if (speed >= 99)
                    return Pair.of(direction + "", "199 or greater");
                return Pair.of(direction + "", speed + "");
            }
            return Pair.of("N/A", "N/A");
        } catch (NumberFormatException | StringIndexOutOfBoundsException exception) {
            return Pair.of("N/A", "N/A");
        }
    }

    private static String getWindsAloftResponse(String closestAirport, double closestAirportDistance, int altitude) {
        // Round altitude to the nearest value in the list of altitudes for which winds aloft data is available.
        int altitudeRounded = windsAloftAltitudes.stream()
                .min((a, b) -> Integer.compare(Math.abs(a - altitude), Math.abs(b - altitude))).get();
        int index;

        if (altitudeRounded <= windsAloftAltitudes.get(0))
            index = 0;
        else if (altitudeRounded >= windsAloftAltitudes.get(windsAloftAltitudes.size() - 1))
            index = windsAloftAltitudes.size() - 1;
        else
            index = windsAloftAltitudes.indexOf(altitudeRounded);
        String windsAloftRaw = windsAloftData.getIfPresent(closestAirport)[index];
        Pair<String, String> windData = decodeWindsAloftRaw(windsAloftRaw);

        // Return the data in form "direction@speed@raw@updated_airport_code@distance_from_orginal_airport_in_miles"
        return "" + windData.getLeft() + "@" + windData.getRight() + "@" + windsAloftRaw + "@" + closestAirport + "@"
                + closestAirportDistance;
    }

    /**
     * Returns the winds aloft data for a given airport and altitude.
     * 
     * The data is fetched from the winds aloft API which is refreshed every 6 hours.
     * 
     * The nearest airport with winds aloft data is returned if the given airport does not have data, and the altitude
     * is rounded to the nearest 3000 feet.
     */
    protected String getWindsAloftData(String airportCode, int altitude, String windsAloftApiUrl) {
        initializeWindsAloftData(windsAloftApiUrl);
        double closestAirportDistance = 0.00;

        // If the airport is not in the list of airports with winds aloft data, get the closest airport with data.
        if (!isWindsAloftAirport(airportCode)) {
            Pair<String, Double> closestAirport = getClosestAirport(airportCode);

            if (closestAirport.getLeft().isEmpty())
                return "No winds aloft data available for the given airport or nearest airport.";
            airportCode = closestAirport.getLeft();
            closestAirportDistance = Math.round(closestAirport.getRight() * 100.0) / 100.0;
        }
        return getWindsAloftResponse(airportCode, closestAirportDistance, altitude);
    }

    protected String getWindsAloftData(double latitude, double longitude, int altitude, String windsAloftApiUrl) {
        initializeWindsAloftData(windsAloftApiUrl);
        Pair<String, Double> closestAirport = getClosestAirport(latitude, longitude);
        String closestAirportCode = closestAirport.getLeft();
        double closestAirportDistance = Math.round(closestAirport.getRight() * 100.0) / 100.0;

        return getWindsAloftResponse(closestAirportCode, closestAirportDistance, altitude);
    }
}
