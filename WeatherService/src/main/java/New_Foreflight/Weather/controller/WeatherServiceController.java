package New_Foreflight.Weather.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import New_Foreflight.Weather.dto.SigmetResponse;
import New_Foreflight.Weather.service.WeatherService;

import New_Foreflight.Weather.dto.AirportWeatherResponse;

@RestController
@RequestMapping("/api/v1")
public class WeatherServiceController {

    @Autowired
    WeatherService weatherService;

    @GetMapping(value = "/getAirportWeather")
    public ResponseEntity<AirportWeatherResponse> getAirportWeather(@RequestParam String airportCode) {
        try {
            return ResponseEntity.ok(weatherService.getAirportWeather(airportCode));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/getWindsAloft")
    public ResponseEntity<String> getWindsAloft(@RequestParam String airportCode, @RequestParam int altitude) {
        try {
            return ResponseEntity.ok(weatherService.getWindsAloft(airportCode, altitude));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/getWindsAloftByCoords")
    public ResponseEntity<String> getWindsAloftByCoords(@RequestParam double latitude, @RequestParam double longitude,
            @RequestParam int altitude) {
        try {
            return ResponseEntity.ok(weatherService.getWindsAloft(latitude, longitude, altitude));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getSigmets")
    public ResponseEntity<SigmetResponse> getSigmets() {
        try {
            return ResponseEntity.ok(weatherService.getSigmets());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getSigmetsByTime")
    public ResponseEntity<SigmetResponse> getSigmetsByTime(@RequestParam String startTime) {
        try {
            return ResponseEntity.ok(weatherService.getSigmets(startTime));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
