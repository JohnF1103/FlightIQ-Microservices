package New_Foreflight.Frequency;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import New_Foreflight.Frequency.controller.FrequencyServiceController;
import New_Foreflight.Frequency.dto.AirportFrequencyResponse;

@SpringBootTest
class FrequencyServiceApplicationTests {

    @Autowired
    private FrequencyServiceController controller;

    @Test
    void getFrequenciesTest() {
        ResponseEntity<AirportFrequencyResponse> response = controller.getAirportFrequencies("KJFK");

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
        System.out.println("Frequencies for KJFK:");
        System.out.println(jsonNode.get("frequencies").toString());
    }

    @Test
    void contextLoads() {
    }
}
