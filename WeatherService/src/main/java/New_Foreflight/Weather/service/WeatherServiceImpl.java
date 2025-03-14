package New_Foreflight.Weather.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import New_Foreflight.Weather.dto.AirportWeatherResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static Cache<String, AirportWeatherResponse> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();

    @Value("${checkwx.api.url}")
    private String apiUrl;

    @Value("${checkwx.api.key}")
    private String apiKey;

    @Override
    public AirportWeatherResponse getAirportWeather(String icao) {
        if (getCache(icao) != null)
            return getCache(icao);
        String endpoint = apiUrl.replace("{station}", icao).replace("{key}", apiKey);
        RestTemplate restTemplate = new RestTemplate();
        String apiResponseJson = restTemplate.getForObject(endpoint, String.class);

        String rawMetar = parseRawMetarText(apiResponseJson);
        HashMap<String, Object> seperatedComponents = separateMetarComponents(apiResponseJson);
        String flightRules = getFlightConditions(apiResponseJson);
        AirportWeatherResponse response = new AirportWeatherResponse(rawMetar, seperatedComponents, flightRules);

        addToCache(icao, response);
        return response;
    }

    @Override
    public String parseRawMetarText(String apiResponse) {
        return new JSONObject(apiResponse).getJSONArray("data").getJSONObject(0).getString("raw_text");
    }

    @Override
    public HashMap<String, Object> separateMetarComponents(String info) {
        JSONObject result = new JSONObject(info).getJSONArray("data").getJSONObject(0);

        LinkedHashMap<String, Object> metarComponents = new LinkedHashMap<>();

        // Add METAR components using reusable methods
        addComponentIfPresent(result, "wind", metarComponents, WeatherServiceImpl::parseWinds);
        addComponentIfPresent(result, "visibility", metarComponents, WeatherServiceImpl::parseVisibility);
        addComponentIfPresent(result, "clouds", metarComponents, WeatherServiceImpl::parseClouds);
        addComponentIfPresent(result, "temperature", metarComponents, WeatherServiceImpl::parseTemperature);
        addComponentIfPresent(result, "dewpoint", metarComponents, WeatherServiceImpl::parseDewpoint);
        addComponentIfPresent(result, "barometer", metarComponents, WeatherServiceImpl::parsePressure);
        addComponentIfPresent(result, "humidity", metarComponents, WeatherServiceImpl::parseHumidity);
        addComponentIfPresent(result, "elevation", metarComponents, WeatherServiceImpl::parseElevation);

        metarComponents.put("density_altitude", computeDensityAltitude(metarComponents));

        return metarComponents;
    }

    @Override
    public String getFlightConditions(String apiResponseJson) {
        /*
         * VFR conditions are defined as visibility greater than 5 statute miles and a cloud ceiling above 3,000 feet.
         *
         * MVFR conditions occur when visibility is between 3 and 5 statute miles or the cloud ceiling is between 1,000
         * and 3,000 feet.
         *
         * IFR conditions are for visibility less than or equal to 3 statute miles or a cloud ceiling at or below 1,000
         * feet.
         *
         * Returns the flight conditions from the API response as a string
         *
         */

        String flightConditions = new JSONObject(apiResponseJson).getJSONArray("data").getJSONObject(0)
                .getString("flight_category").toString();

        return flightConditions;
    }

    @Override
    public String getWindsAloft(String airportCode, int altitude) {
        // TODO Auto-generated method stub
        return "";
    }

    private static double calculateStandardTemperature(double altitude) {
        // Standard temperature at sea level is 15°C
        final double SEA_LEVEL_STANDARD_TEMP = 15.0;
        // Temperature decreases by 2°C per 1000 feet
        final double TEMP_DECREASE_RATE = 2.0;
        // Calculate the standard temperature at the given altitude
        double standardTemperature = SEA_LEVEL_STANDARD_TEMP - (altitude / 1000.0) * TEMP_DECREASE_RATE;

        return standardTemperature;
    }

    private static double computeDensityAltitude(HashMap<String, Object> weatherComponents) {
        /*
         * this funciton should compute the density alttude for an airport at a given pressure altitude
         * 
         * 
         * An API call to extract the proper data for presssure altitude at an airport so for now just use 3000 feet
         * 
         * the needed airport elevation can be found here
         * 
         * https://www.checkwxapi.com/documentation/station
         * 
         * using the OAT(outside air temp) and ISA which is the standard temperature at a given altitude. use the helper
         * function to compute this.
         * 
         * Implement the formula DA = Pressure_Altitude + (120 x (OAT – ISA))
         */
        double alt = Double.parseDouble(weatherComponents.get("elevation").toString());
        double degF = Double.parseDouble(weatherComponents.get("temperature").toString().split(" ")[0]);
        double degC = (degF - 32) / 1.8;
        double isa = calculateStandardTemperature(alt);

        return alt + (120 * (degC - isa));
    }

    /**
     * Helper method to add a component if it exists in the JSON object.the "handle clouds handle vis etc.
     *
     */
    private static <T> void addComponentIfPresent(JSONObject result, String key, LinkedHashMap<String, Object> map,
            DataParser<T> parser) {
        if (result.has(key) && !result.isNull(key))
            map.put(key, parser.parse(result.get(key)));
    }

    // Define functional interface for reusable parsers
    @FunctionalInterface
    private static interface DataParser<T> {
        T parse(Object data);
    }

    // Parsers for METAR components
    private static String parseWinds(Object windDataObj) {
        JSONObject windData = (JSONObject) windDataObj;
        int direction = windData.optInt("degrees", 0);
        int speedKts = windData.optInt("speed_kts", 0);
        int gustKts = windData.optInt("gust_kts", 0);

        return gustKts > 0 ? String.format("%d at %d-%d kts", direction, speedKts, gustKts)
                : String.format("%d at %d kts", direction, speedKts);
    }

    private static String parseVisibility(Object visibilityDataObj) {
        JSONObject visibilityData = (JSONObject) visibilityDataObj;

        return visibilityData.optString("miles") + " SM";
    }

    /*
     * parses the clouds obj into a list of cloud ceilings and adds to the metar components. only displays if sky
     * conditions are not clear.
     *
     */
    private static List<HashMap<String, String>> parseClouds(Object cloudsDataObj) {
        JSONArray cloudsArray = (JSONArray) cloudsDataObj;
        List<HashMap<String, String>> cloudsList = new ArrayList<>();

        for (int i = 0; i < cloudsArray.length(); i++) {
            JSONObject cloud = cloudsArray.getJSONObject(i);
            LinkedHashMap<String, String> cloudMap = new LinkedHashMap<>();

            String skyCode = cloud.optString("code", "Unknown");
            cloudMap.put("code", skyCode);

            if (!"CLR".equalsIgnoreCase(skyCode)) {
                cloudMap.put("feet", cloud.optString("feet", "Unknown"));
            }

            cloudsList.add(cloudMap);
        }
        return cloudsList;
    }

    private static String parseTemperature(Object temperatureDataObj) {
        JSONObject tempData = (JSONObject) temperatureDataObj;

        return String.format("%s degrees F, %s degrees C", tempData.optString("fahrenheit"),
                tempData.optString("celsius"));
    }

    private static String parseDewpoint(Object dewpointDataObj) {
        JSONObject dewpointData = (JSONObject) dewpointDataObj;

        return String.format("%s degrees F, %s degrees C", dewpointData.optString("fahrenheit"),
                dewpointData.optString("celsius"));
    }

    private static String parsePressure(Object pressureDataObj) {
        JSONObject pressureData = (JSONObject) pressureDataObj;

        return "hg: " + pressureData.optString("hg");
    }

    private static String parseHumidity(Object humidityDataObj) {
        JSONObject humidityData = (JSONObject) humidityDataObj;

        return humidityData.optString("percent") + " %";
    }

    private static String parseElevation(Object elevationDataObj) {
        JSONObject elevationData = (JSONObject) elevationDataObj;

        return elevationData.optString("feet");
    }

    private static void addToCache(String icao, AirportWeatherResponse response) {
        cache.put(icao, response);
    }

    private static AirportWeatherResponse getCache(String icao) {
        return cache.getIfPresent(icao);
    }
}
