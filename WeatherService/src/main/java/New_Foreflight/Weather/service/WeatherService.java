package New_Foreflight.Weather.service;

import New_Foreflight.Weather.dto.AirportWeatherResponse;

import java.util.HashMap;

public interface WeatherService {

    AirportWeatherResponse getAirportWeather(String iaco);

    String parseRawMETARText(String apiResponse);

    HashMap<String, Object> separateMetarComponents(String info);

    String getFlightConditions(String apiResponseJSON);
}
