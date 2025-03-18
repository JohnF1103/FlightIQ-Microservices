package New_Foreflight.Weather.service;

import New_Foreflight.Weather.dto.AirportWeatherResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Value("${checkwx.api.url}")
    private String apiUrl;

    @Value("${checkwx.api.key}")
    private String apiKey;

    @Override
    public AirportWeatherResponse getAirportWeather(String icao) {
        if (WeatherServiceUtility.getCache(icao) != null)
            return WeatherServiceUtility.getCache(icao);
        String endpoint = apiUrl.replace("{station}", icao).replace("{key}", apiKey);
        RestTemplate restTemplate = new RestTemplate();
        String apiResponseJson = restTemplate.getForObject(endpoint, String.class);

        String rawMetar = parseRawMetarText(apiResponseJson);
        HashMap<String, Object> seperatedComponents = separateMetarComponents(apiResponseJson);
        String flightRules = getFlightConditions(apiResponseJson);
        AirportWeatherResponse response = new AirportWeatherResponse(rawMetar, seperatedComponents, flightRules);

        WeatherServiceUtility.addToCache(icao, response);
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
        String flightConditions = new JSONObject(apiResponseJson).getJSONArray("data").getJSONObject(0)
                .getString("flight_category").toString();

        return flightConditions;
    }

    @Override
    public String getWindsAloft(String airportCode, int altitude) {
        // TODO Auto-generated method stub
        return "";
    }
}
