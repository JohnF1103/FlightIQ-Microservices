package New_Foreflight.Weather.service;

import java.util.HashMap;

import New_Foreflight.Weather.dto.AirportWeatherResponse;
import New_Foreflight.Weather.dto.SigmetResponse;

public interface WeatherService {

    public AirportWeatherResponse getAirportWeather(String iaco);

    public String parseRawMetarText(String apiResponse);

    public HashMap<String, Object> separateMetarComponents(String info);

    public String getFlightConditions(String apiResponseJSON);

    public String getWindsAloft(String airportCode, int altitude);

    public String getWindsAloft(double latitude, double longitude, int altitude);

    public SigmetResponse getSigmets();

    public SigmetResponse getSigmets(String startTime) throws RuntimeException;
}
