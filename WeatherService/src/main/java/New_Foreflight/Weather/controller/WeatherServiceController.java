package New_Foreflight.Weather.controller;

import New_Foreflight.Weather.dto.AirportWeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import New_Foreflight.Weather.service.WeatherService;

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

    @GetMapping(value = "/getPireps")
    public ResponseEntity<String> getPireps(
            @RequestParam String icao,
            @RequestParam int distance,
            @RequestParam int age) {
        try {
            String pireps = weatherService.getPirepData(icao, distance, age);
            
            return ResponseEntity.ok(pireps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no pireps");
        }
    }
    
    @GetMapping(value = "/getWindTemp")
    public ResponseEntity<String> getWindTemp(@RequestParam String region, @RequestParam String forcast, @RequestParam String level) {
        try {
            String windTemp = weatherService.getWindTemp(region,forcast,level);
            
            return ResponseEntity.ok(windTemp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no wind temp");
        }
    }
    
    @GetMapping(value = "/getAirSigmet")
    public ResponseEntity<String> getAirSigmet() {
        try {
            String airSigmet = weatherService.getAirSigmet();
            
            return ResponseEntity.ok(airSigmet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no air sigmet");
        }
    }
    
 
    
    @GetMapping(value = "/getGAirmet")
    public ResponseEntity<String> getGAirmet(@RequestParam int southLat,@RequestParam int westLon,@RequestParam int northLat,@RequestParam int eastLon) {
        try {
            String GAirmet = weatherService.getGAirmet(southLat, westLon,northLat,eastLon);
            
            return ResponseEntity.ok(GAirmet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no GAirmet");
        }
    }
    
    @GetMapping(value = "/getDewPointSpread")
    public ResponseEntity<String> getDewPointSpread(@RequestParam String icao) {
        try {
            String dewPointSpread = weatherService.getDewPointSpread(icao);
            
            return ResponseEntity.ok(dewPointSpread);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no DewPointSpread");
        }
    }

    @GetMapping(value = "/getTAF")
    public ResponseEntity<String> getTAF(@RequestParam String icao) {
        try {
            String taf = weatherService.getTAF(icao);

            return ResponseEntity.ok(taf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no TAF");
        }
    }
}
