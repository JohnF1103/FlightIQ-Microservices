package New_Foreflight.Frequency.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import New_Foreflight.Frequency.model.FrequencyService;

@RestController
@RequestMapping("/api/v1")
public class FrequencyServiceController {

    @Autowired
    private FrequencyService frequencyService;

    @GetMapping(value = "/getAirportFrequencies")
    public ResponseEntity<HashMap<String, String>> getFrequencies(@RequestParam String airportCode) {
        try {
            return ResponseEntity.ok(frequencyService.getFrequencies(airportCode));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}