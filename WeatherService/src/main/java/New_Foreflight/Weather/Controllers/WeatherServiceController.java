package New_Foreflight.Weather.Controllers;

import New_Foreflight.Weather.DTO.AirportWeatherResponse;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import New_Foreflight.Weather.Service.Weatherservice;

@RestController
@RequestMapping("/api/v1")
public class WeatherServiceController {

    @Autowired
    Weatherservice weatherservice;



        @GetMapping(value = "/getAirportInfo")
        public ResponseEntity<AirportWeatherResponse> getAirportInfo(@RequestParam String airportCode) {
            try {
                AirportWeatherResponse response = weatherservice.getAirportWeather(airportCode);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

        @GetMapping("/getWindAtCoords")
        public ResponseEntity <String> getWindAtCoords(
            @RequestParam double lat,
            @RequestParam double lon) {
        try {
            String windData = weatherservice.getWindAtCoords(lat, lon);
            return ResponseEntity.ok(windData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
