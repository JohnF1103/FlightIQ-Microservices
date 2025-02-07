package New_Foreflight.Frequency;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import New_Foreflight.Frequency.controller.FrequencyServiceController;

@SpringBootTest
class FrequencyServiceApplicationTests {

    @Autowired
    private FrequencyServiceController controller;

    @Test
    void apiTest() {
        ResponseEntity<HashMap<String, String>> frequencies = controller.getFrequencies("KJFK");

        if (!frequencies.hasBody())
            fail("No frequencies found for requested airport.");
        else {
            for (Map.Entry<String, String> entry : frequencies.getBody().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                System.out.println(key + ": " + value);
            }
        }
    }

	@Test
	void contextLoads() {
	}
}
