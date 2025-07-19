package New_Foreflight.Weather.dto;

import java.util.List;

public class SigmetResponse {

    private List<SigmetFeature> features;

    public SigmetResponse(List<SigmetFeature> features) {
        this.features = features;
    }

    public List<SigmetFeature> getFeatures() {
        return features;
    }

    public record SigmetFeature(String type, Geometry geometry, SigmetProperty properties) {
    }

    public record Geometry(String type, List<List<List<Double>>> coordinates) {
    }

    public record SigmetProperty(String id, String issueTime, String fir, String atsu, String sequence,
            String phenomenon, String start, String end) {
    }
}