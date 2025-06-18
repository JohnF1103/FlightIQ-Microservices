package New_Foreflight.Weather.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import New_Foreflight.Weather.DTO.AirportWeatherResponse;
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
        
 
        
        @GetMapping(value = "/getPireps")
        public ResponseEntity<String> getPireps(
                @RequestParam String airportCode,
                @RequestParam int distance,
                @RequestParam int age) {
            try {
                String pireps = weatherservice.getPirepData(airportCode, distance, age);
                return ResponseEntity.ok(pireps);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no pireps");
            }
        }
        
        @GetMapping(value = "/getwindTemp")
        public ResponseEntity<String> getwindTemp(@RequestParam String reigon, @RequestParam String forcast, @RequestParam String level) {
            try {
                String windTemp = weatherservice.getwindTemp(reigon,forcast,level);
                return ResponseEntity.ok(windTemp);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no pireps");
            }
        }
        
        @GetMapping(value = "/getairSigmet")
        public ResponseEntity<String> getairSigmet() {
            try {
                String airSigmet = weatherservice.getairSigmet();
                return ResponseEntity.ok(airSigmet);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("no pireps");
            }
        }

}
