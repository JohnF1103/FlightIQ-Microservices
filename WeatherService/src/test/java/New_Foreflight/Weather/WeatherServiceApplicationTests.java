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
        ResponseEntity<AirportWeatherResponse> weatherResponse = controller.getAirportWeather("KLAX");

        assertTrue(weatherResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(weatherResponse.getBody());

        // Convert the response body to JSON.
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(weatherResponse.getBody());
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
        JsonNode jsonNode = null;

        try {
            jsonNode = objectMapper.readTree(jsonString);

            // Output individual JSON fields.
            System.out.println("Weather Data for KLAX:");
            System.out.println("METAR Data: " + jsonNode.get("metar_data").asText());
            System.out.println("METAR Components: " + jsonNode.get("metar_components").toString());
            System.out.println("Flight Rules: " + jsonNode.get("flight_rules").asText());
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void getWindsAloftTest() {
        ResponseEntity<String> windsAloftResponse = controller.getWindsAloft("KABI", 41345);

        assertTrue(windsAloftResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(windsAloftResponse.getBody());

        // Output the winds aloft data.
        System.out.println("Winds Aloft Data for KABI:");
        System.out.println(windsAloftResponse.getBody());
    }

    @Test
    void contextLoads() {
    }
}
