package New_Foreflight.Frequency.dto;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirportFrequencyResponse {

    @JsonProperty("frequencies")
    private HashMap<String, String> frequencies;

    public AirportFrequencyResponse(HashMap<String, String> frequencies) {
        this.frequencies = frequencies;
    }
}
