package New_Foreflight.Weather;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import New_Foreflight.Weather.controller.WeatherServiceController;
import New_Foreflight.Weather.dto.AirportWeatherResponse;

@SpringBootTest
class WeatherServiceApplicationTests {

    @Autowired
    private WeatherServiceController controller;

    @Test
    void getAirportWeatherTest() {
        ResponseEntity<AirportWeatherResponse> response = controller.getAirportWeather("KLAX");

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());

        // Convert the response body to JSON.
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(response.getBody());
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
        JsonNode jsonNode = null;

        try {
            jsonNode = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
        // Output individual JSON fields.
        System.out.println("Weather Data for KLAX:");
        System.out.println("METAR Data: " + jsonNode.get("metar_data").asText());
        System.out.println("METAR Components: " + jsonNode.get("metar_components").toString());
        System.out.println("Flight Rules: " + jsonNode.get("flight_rules").asText());
    }

    @Test
    void contextLoads() {
    }
}
