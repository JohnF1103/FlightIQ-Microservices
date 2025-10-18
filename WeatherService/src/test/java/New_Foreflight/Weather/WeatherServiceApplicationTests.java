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
import New_Foreflight.Weather.dto.AirmetResponse;
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
        // Test for which winds aloft data is available for the given airport.
        ResponseEntity<String> windsAloftResponse = controller.getWindsAloft("KABI", 7200);

        assertTrue(windsAloftResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(windsAloftResponse.getBody());

        // Output the winds aloft data.
        System.out.println("Winds Aloft Data for KABI:");
        System.out.println(windsAloftResponse.getBody());

        // Test for which winds aloft data is not available for the given airport,
        // so nearest airport is used.
        windsAloftResponse = controller.getWindsAloft("KLGA", 38560);

        assertTrue(windsAloftResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(windsAloftResponse.getBody());

        // Output the winds aloft data.
        System.out.println("Winds Aloft Data for KLGA:");
        System.out.println(windsAloftResponse.getBody());
        assertTrue(windsAloftResponse.getBody().contains("KJFK"));

        // Test for which winds aloft data is available for the given latitude and
        // longitude.
        windsAloftResponse = controller.getWindsAloftByCoords(40.6, -73.7, 3600);

        assertTrue(windsAloftResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(windsAloftResponse.getBody());

        System.out.println("Winds Aloft Data for Latitude 40.6 and Longitude -73.7:");
        System.out.println(windsAloftResponse.getBody());
    }

    @Test
    void contextLoads() {
    }

    @Test
    void getWxAirmetTest() {
        // Using coordinates for a test location (e.g., somewhere over the US)
        double latitude = 40.7128;
        double longitude = -74.0060;
        
        ResponseEntity<AirmetResponse> airmetResponse = controller.getWxAirmet(latitude, longitude);

        assertTrue(airmetResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(airmetResponse.getBody());

        // Convert the response body to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(airmetResponse.getBody());
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
        
        JsonNode jsonNode = null;

        try {
            jsonNode = objectMapper.readTree(jsonString);

            // Output individual JSON fields
            System.out.println("AIRMET Data for location: " + latitude + ", " + longitude);
            System.out.println("Results Count: " + jsonNode.get("results").asInt());
            
            JsonNode dataArray = jsonNode.get("data");
            if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                System.out.println("\nAIRMET Details:");
                for (int i = 0; i < dataArray.size(); i++) {
                    JsonNode airmet = dataArray.get(i);
                    System.out.println("\n--- AIRMET " + (i + 1) + " ---");
                    System.out.println("Category: " + airmet.get("category").asText());
                    System.out.println("Hazard Type: " + airmet.get("hazard").get("type").get("code").asText() 
                        + " - " + airmet.get("hazard").get("type").get("text").asText());
                    System.out.println("Severity: " + airmet.get("hazard").get("severity").get("code").asText()
                        + " - " + airmet.get("hazard").get("severity").get("text").asText());
                    System.out.println("Valid From: " + airmet.get("timestamp").get("from").asText());
                    System.out.println("Valid To: " + airmet.get("timestamp").get("to").asText());
                    System.out.println("Altitude: " + airmet.get("altitude").get("minimum").get("feet").asDouble() 
                        + " - " + airmet.get("altitude").get("maximum").get("feet").asDouble() + " feet");
                }
            } else {
                System.out.println("No AIRMET data found for this location.");
            }
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
    }
}
