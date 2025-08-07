package New_Foreflight.Weather.Service;

import New_Foreflight.Weather.DTO.AirportWeatherResponse;

import java.util.HashMap;

public interface Weatherservice {


     AirportWeatherResponse getAirportWeather(String iaco);
     String parseRawMETARText(String apiResponse);
     HashMap<String, Object> separateMetarComponents(String info);
     
     String getFlightConditions(HashMap<String, Object> WeatherComponents);

     String getPirepData(String airportCode, int dist, int time); 
     String getairSigmet(); 
     String getwindTemp(String reigon, String forcast, String level); 
     String getMetar(String airportcode, int hours);
     String getGAirmet(int southLat, int westLon, int northLat, int eastLon);
     String getDewPointSpread(String icao);

    String getWindsAloft(String airportCode, int altitude);

}

    AirportWeatherResponse getAirportWeather(String iaco);

    String parseRawMETARText(String apiResponse);


    HashMap<String, Object> separateMetarComponents(String info);

    String getFlightConditions(String apiResponseJSON);
}
