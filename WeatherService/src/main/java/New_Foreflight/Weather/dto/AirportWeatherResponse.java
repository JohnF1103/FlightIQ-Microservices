package New_Foreflight.Weather.dto;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirportWeatherResponse {

    @JsonProperty("metar_data")
    private String metarData;

    @JsonProperty("metar_components")
    private HashMap<String, Object> METARcomponents;

    @JsonProperty("flight_rules")
    private String flightRules;

    public AirportWeatherResponse(String metarData, HashMap<String, Object> metarcomponents, String flightRules) {
        this.metarData = metarData;
        this.METARcomponents = metarcomponents;
        this.flightRules = flightRules;
    }
}
