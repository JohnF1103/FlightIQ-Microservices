package New_Foreflight.Weather.Service;

import New_Foreflight.Weather.DTO.AirportWeatherResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class WeatherServiceImpl implements Weatherservice {

    @Value("${checkwx.api.url}")
    private String apiUrl;

    @Value("${checkwx.api.key}")
    private String apiKey;

    @Override
    public AirportWeatherResponse getAirportWeather(String icao) {
        String endpoint = apiUrl.replace("{station}", icao) + "?x-api-key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        String apiResponseJSON = restTemplate.getForObject(endpoint, String.class);

        String RawMETAR = parseRawMETARText(apiResponseJSON);
        HashMap<String, Object> SeperatedComponents = separateMetarComponents(apiResponseJSON);
        String FLightRules = getFlightConditions(apiResponseJSON);

        return new AirportWeatherResponse(RawMETAR, SeperatedComponents, FLightRules);
    }

    @Override
    public String parseRawMETARText(String apiResponse) {
        return new JSONObject(apiResponse).getJSONArray("data").getJSONObject(0).getString("raw_text");
    }

    @Override
    public HashMap<String, Object> separateMetarComponents(String info) {
        JSONObject result = new JSONObject(info).getJSONArray("data").getJSONObject(0);

        LinkedHashMap<String, Object> metarComponents = new LinkedHashMap<>();

        // Add METAR components using reusable methods
        addComponentIfPresent(result, "wind", metarComponents, this::parseWinds);
        addComponentIfPresent(result, "visibility", metarComponents, this::parseVisibility);
        addComponentIfPresent(result, "clouds", metarComponents, this::parseClouds);
        addComponentIfPresent(result, "temperature", metarComponents, this::parseTemperature);
        addComponentIfPresent(result, "dewpoint", metarComponents, this::parseDewpoint);
        addComponentIfPresent(result, "barometer", metarComponents, this::parsePressure);
        addComponentIfPresent(result, "humidity", metarComponents, this::parseHumidity);
        addComponentIfPresent(result, "elevation", metarComponents, this::parseElevation);

        metarComponents.put("density_altitude", computeDensityAltitude(metarComponents));

        return metarComponents;
    }

    @Override
    public String getFlightConditions(String apiResponseJSON) {

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

        String flightConditions = new JSONObject(apiResponseJSON).getJSONArray("data").getJSONObject(0)
                .getString("flight_category").toString();

        return flightConditions;
    }

    public static double calculateStandardTemperature(int altitude) {
        // Standard temperature at sea level is 15°C
        final double SEA_LEVEL_STANDARD_TEMP = 15.0;

        // Temperature decreases by 2°C per 1000 feet
        final double TEMP_DECREASE_RATE = 2.0;

        // Calculate the standard temperature at the given altitude
        double standardTemperature = SEA_LEVEL_STANDARD_TEMP - (altitude / 1000.0) * TEMP_DECREASE_RATE;

        return standardTemperature;
    }

    private double computeDensityAltitude(HashMap<String, Object> WeatherComponents) {

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
         * using the OAT(outside air temp) and ISA which is the standard tempature at a given altitude. use the helper
         * function to compute this.
         * 
         * implement the formula DA = Pressure_Altitude + (120 x (OAT – ISA))
         */

        double ALT = Double.parseDouble(WeatherComponents.get("elevation").toString());

        double degF = Double.parseDouble(WeatherComponents.get("temperature").toString().split(" ")[0]);
  
        double degC = (degF - 32) / 1.8;
  
        double ISA = calculateStandardTemperature((int) ALT);
  
        return ALT + (120 * (degC - ISA));
    }

    /**
     * Helper method to add a component if it exists in the JSON object.the "handle clouds handle vis etc.
     *
     */
    private <T> void addComponentIfPresent(JSONObject result, String key, LinkedHashMap<String, Object> map,
            DataParser<T> parser) {
        if (result.has(key) && !result.isNull(key)) {
            map.put(key, parser.parse(result.get(key)));
        }
    }

    // Define functional interface for reusable parsers
    @FunctionalInterface
    private interface DataParser<T> {
        T parse(Object data);
    }

    // Parsers for METAR components
    private String parseWinds(Object windDataObj) {
        JSONObject windData = (JSONObject) windDataObj;
        int direction = windData.optInt("degrees", 0);
        int speedKts = windData.optInt("speed_kts", 0);
        int gustKts = windData.optInt("gust_kts", 0);

        return gustKts > 0 ? String.format("%d at %d-%d kts", direction, speedKts, gustKts)
                : String.format("%d at %d kts", direction, speedKts);
    }

    private String parseVisibility(Object visibilityDataObj) {
        JSONObject visibilityData = (JSONObject) visibilityDataObj;
        return visibilityData.optString("miles") + " SM";
    }

    /*
     * parses the clouds obj into a list of cloud ceilings and adds to the metar components. only displays if sky
     * conditions are not clear.
     *
     */
    private List<HashMap<String, String>> parseClouds(Object cloudsDataObj) {
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

    private String parseTemperature(Object temperatureDataObj) {
        JSONObject tempData = (JSONObject) temperatureDataObj;
        return String.format("%s degrees F, %s degrees C", tempData.optString("fahrenheit"),
                tempData.optString("celsius"));
    }

    private String parseDewpoint(Object dewpointDataObj) {
        JSONObject dewpointData = (JSONObject) dewpointDataObj;
        return String.format("%s degrees F, %s degrees C", dewpointData.optString("fahrenheit"),
                dewpointData.optString("celsius"));
    }

    private String parsePressure(Object pressureDataObj) {
        JSONObject pressureData = (JSONObject) pressureDataObj;
        return "hg: " + pressureData.optString("hg");
    }

    private String parseHumidity(Object humidityDataObj) {
        JSONObject humidityData = (JSONObject) humidityDataObj;
        return humidityData.optString("percent") + " %";
    }

    private String parseElevation(Object ElevationDataObj) {

        JSONObject elevationData = (JSONObject) ElevationDataObj;

        return elevationData.optString("feet");
    }
}
