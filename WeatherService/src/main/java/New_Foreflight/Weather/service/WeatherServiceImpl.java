package New_Foreflight.Weather.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import New_Foreflight.Weather.dto.AirportWeatherResponse;
import New_Foreflight.Weather.dto.SigmetResponse;
import New_Foreflight.Weather.dto.SigmetResponse.SigmetFeature;

@Service
public class WeatherServiceImpl implements WeatherService {

    // Approx bounding box of continental U.S.
    private static final double US_MIN_LAT = 24.396308;
    private static final double US_MAX_LAT = 49.384358;
    private static final double US_MIN_LON = -125.0;
    private static final double US_MAX_LON = -66.93457;

    @Autowired
    private WeatherServiceUtility utility;

    @Value("${checkwx.api.url}")
    private String weatherApiUrl;

    @Value("${checkwx.api.key}")
    private String weatherApiKey;

    @Value("${aviation.weather.api.url}")
    private String windsAloftApiUrl;

    @Value("${sigmet.api.url}")
    private String sigmetApiUrl;

    @Value("${sigmet.api.ua}")
    private String sigmetApiHeader;

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

    /**
     * Provides all the Aviation Sigmet data.
     */
    @Override
    public SigmetResponse getSigmets() { // US Only coordinates, null may be convective?
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, sigmetApiHeader);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(sigmetApiUrl, HttpMethod.GET, request, SigmetResponse.class).getBody();
    }

    /**
     * Provides the Aviation Sigmet data, filted by startTime, endTime, and U.S. only.
     * 
     * Time is in ISO 8601 format.
     * year-mo-daThr:mi:sc.milZ
     * date T time TimeZone
     * T is the time delimiter
     * hr:minute:second.milisecond
     * Z is the TimeZone, Z is UTC
     * @param startTime date-time | ex: "2025-07-19T03:01:04Z"
     * @param endTime date-time | ex: "2025-07-24T03:01:04Z"
     */
    @Override
    public SigmetResponse getSigmets(String startTime) throws RuntimeException {
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(sigmetApiUrl);
        uriBuilder.queryParam("start", startTime);
        // uriBuilder.queryParam("end", endTime);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, sigmetApiHeader);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        SigmetResponse sigmetResponse = restTemplate.exchange(
            uriBuilder.toUriString(), 
            HttpMethod.GET, 
            request, 
            SigmetResponse.class
        ).getBody();

        if (sigmetResponse == null || sigmetResponse.getFeatures() == null) {
            return null;
        }

        List<SigmetFeature> filtered = sigmetResponse.getFeatures().stream().filter(this::isInUS).toList();
        return new SigmetResponse(filtered);
    }

    /**
     * Filter for bounding Sigmet data to the US.
     */
    private boolean isInUS(SigmetFeature feature) {
        if (feature.geometry() == null || feature.geometry().coordinates() == null) {
            return false;
        }

        for (List<List<Double>> polygon : feature.geometry().coordinates()) {
            for (List<Double> point : polygon) {
                double lat = point.get(0); // GeoJSON: [lat, lon]
                double lon = point.get(1);
                if (lat >= US_MIN_LAT && lat <= US_MAX_LAT &&
                    lon >= US_MIN_LON && lon <= US_MAX_LON) {
                    return true;
                }
            }
        }
        return false;
    }
}
