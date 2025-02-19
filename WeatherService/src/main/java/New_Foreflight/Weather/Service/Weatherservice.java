package New_Foreflight.Weather.Service;

import New_Foreflight.Weather.DTO.AirportWeatherResponse;

import java.util.HashMap;

public interface Weatherservice {

     AirportWeatherResponse getAirportWeather(String iaco);
     String parseRawMETARText(String apiResponse);
     HashMap<String, Object> separateMetarComponents(String info);
     String getFlightConditions(String apiResponseJSON);
     String getWindAtCoords(double lat, double lon);
}


