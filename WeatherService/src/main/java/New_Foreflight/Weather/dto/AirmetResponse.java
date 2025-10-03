package New_Foreflight.Weather.dto;


import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirmetResponse {
    @JsonProperty("airmet_results")
    private Integer airmetResults;

    @JsonProperty("airmet_components")
    private List<HashMap<String, Object>> airmetComponents;

    public AirmetResponse() {
    }

    public AirmetResponse(Integer results, List<HashMap<String, Object>> airmetComponents) {
        this.airmetResults = results;
        this.airmetComponents = airmetComponents;
    }

    public Integer getResults() {
        return airmetResults;
    }

    public void setResults(Integer results) {
        this.airmetResults = results;
    }

    public List<HashMap<String, Object>> getData() {
        return airmetComponents;
    }

    public void setData(List<HashMap<String, Object>> data) {
        this.airmetComponents = data;
    }
}
