package New_Foreflight.Weather.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import New_Foreflight.Weather.dto.AirportWeatherResponse;

/**
 * Utility class for WeatherService providing helper methods for parsing and caching weather data.
 */
public class WeatherServiceUtility {

    private static Cache<String, AirportWeatherResponse> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();

    @FunctionalInterface
    protected static interface DataParser<T> {
        T parse(Object data);
    }

    protected static void addToCache(String icao, AirportWeatherResponse response) {
        cache.put(icao, response);
    }

    protected static AirportWeatherResponse getCache(String icao) {
        return cache.getIfPresent(icao);
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

        for (int i = 0; i < cloudsArray.length(); i++) {
            JSONObject cloud = cloudsArray.getJSONObject(i);
            LinkedHashMap<String, String> cloudMap = new LinkedHashMap<>();

            String skyCode = cloud.optString("code", "Unknown");

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
}
