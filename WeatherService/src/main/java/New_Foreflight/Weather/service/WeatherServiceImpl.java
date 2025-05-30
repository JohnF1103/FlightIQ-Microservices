package New_Foreflight.Weather.service;

import New_Foreflight.Weather.dto.AirportWeatherResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherServiceUtility utility;

    @Value("${checkwx.api.url}")
    private String weatherApiUrl;

    @Value("${checkwx.api.key}")
    private String weatherApiKey;

    @Value("${aviation.weather.api.url}")
    private String windsAloftApiUrl;

    @Override
    public AirportWeatherResponse getAirportWeather(String icao) {
        if (WeatherServiceUtility.getWeatherCache(icao) != null)
            return WeatherServiceUtility.getWeatherCache(icao);
        String endpoint = weatherApiUrl.replace("{station}", icao).replace("{key}", weatherApiKey);
        RestTemplate restTemplate = new RestTemplate();
        String apiResponseJson = restTemplate.getForObject(endpoint, String.class);

        String rawMetar = parseRawMetarText(apiResponseJson);
        HashMap<String, Object> seperatedComponents = separateMetarComponents(apiResponseJson);
        String flightRules = getFlightConditions(apiResponseJson);
        AirportWeatherResponse response = new AirportWeatherResponse(rawMetar, seperatedComponents, flightRules);

        WeatherServiceUtility.addToWeatherCache(icao, response);
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
        WeatherServiceUtility.addComponentIfPresent(result, "wind", metarComponents, WeatherServiceUtility::parseWinds);
        WeatherServiceUtility.addComponentIfPresent(result, "visibility", metarComponents,
                WeatherServiceUtility::parseVisibility);
        WeatherServiceUtility.addComponentIfPresent(result, "clouds", metarComponents,
                WeatherServiceUtility::parseClouds);
        WeatherServiceUtility.addComponentIfPresent(result, "temperature", metarComponents,
                WeatherServiceUtility::parseTemperature);
        WeatherServiceUtility.addComponentIfPresent(result, "dewpoint", metarComponents,
                WeatherServiceUtility::parseDewpoint);
        WeatherServiceUtility.addComponentIfPresent(result, "barometer", metarComponents,
                WeatherServiceUtility::parsePressure);
        WeatherServiceUtility.addComponentIfPresent(result, "humidity", metarComponents,
                WeatherServiceUtility::parseHumidity);
        WeatherServiceUtility.addComponentIfPresent(result, "elevation", metarComponents,
                WeatherServiceUtility::parseElevation);
        metarComponents.put("density_altitude", WeatherServiceUtility.computeDensityAltitude(metarComponents));

        return metarComponents;
    }

    /*
     * VFR conditions are defined as visibility greater than 5 statute miles and a cloud ceiling above 3,000 feet.
     *
     * MVFR conditions occur when visibility is between 3 and 5 statute miles or the cloud ceiling is between 1,000 and
     * 3,000 feet.
     *
     * IFR conditions are for visibility less than or equal to 3 statute miles or a cloud ceiling at or below 1,000
     * feet.
     *
     * Returns the flight conditions from the API response as a string
     *
     */
    @Override
    public String getFlightConditions(String apiResponseJson) {
        return new JSONObject(apiResponseJson).getJSONArray("data").getJSONObject(0).getString("flight_category")
                .toString();
    }

    /**
     * Provides the winds aloft data for a given airport and altitude.
     * 
     * If a given airport does not have winds aloft data, then the data from the nearest airport is returned.
     */
    @Override
    public String getWindsAloft(String airportCode, int altitude) {
        return utility.getWindsAloftData(airportCode, altitude, new String(windsAloftApiUrl));
    }

    /**
     * Provides the winds aloft data for a given latitude, longitude, and altitude.
     * 
     * The airport from which the data is sourced is the nearest airport to the given latitude and longitude. This
     * airport is identified in the response.
     */
    @Override
    public String getWindsAloft(double latitude, double longitude, int altitude) {
        return utility.getWindsAloftData(latitude, longitude, altitude, new String(windsAloftApiUrl));
    }
}
