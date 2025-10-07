package New_Foreflight.Weather.service;

import New_Foreflight.Weather.dto.AirportWeatherResponse;

import java.util.HashMap;

public interface WeatherService {

    public AirportWeatherResponse getAirportWeather(String icao, Boolean b);

    public String parseRawMetarText(String apiResponse);
    
    public HashMap<String, Object> separateMetarComponents(String info);

    public String getFlightConditions(String apiResponseJson);

    public String getPirepData(String airportCode, int dist, int time);

    public String getAirSigmet();

    public String getWindTemp(String region, String forecast, String level);

    public String getMetar(String airportcode, int hours);

    public String getGAirmet(int southLat, int westLon, int northLat, int eastLon);

    public String getDewPointSpread(String icao);

    public String getWindsAloft(String airportCode, int altitude);

    public String getWindsAloft(double latitude, double longitude, int altitude);
}
