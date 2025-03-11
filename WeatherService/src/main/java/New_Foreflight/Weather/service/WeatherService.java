package New_Foreflight.Weather.service;

import New_Foreflight.Weather.dto.AirportWeatherResponse;

import java.util.HashMap;

public interface WeatherService {

    public AirportWeatherResponse getAirportWeather(String iaco);

    public String parseRawMetarText(String apiResponse);

    public HashMap<String, Object> separateMetarComponents(String info);

    public String getFlightConditions(String apiResponseJSON);

    public String getWindsAloft(String airportCode, int altitude);
}
