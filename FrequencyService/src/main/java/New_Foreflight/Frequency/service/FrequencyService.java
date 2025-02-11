package New_Foreflight.Frequency.service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestParam;

import New_Foreflight.Frequency.dto.AirportFrequencyResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FrequencyService {

    @Value("${airportdb.api.url}")
    private String apiUrl;

    @Value("${airportdb.api.token}")
    private String apiToken;

    @Value("${airportdb.api.key}")
    private String apiKey;

    public AirportFrequencyResponse getFrequencies(@RequestParam String airportCode) {
        String url = apiUrl.replace("{code}", airportCode).replace("{token}", apiToken).replace("{key}", apiKey);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return new AirportFrequencyResponse(parseFrequencies(response.body()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, String> parseFrequencies(String json_frequencies) {
        HashMap<String, String> frequenciesHashMap = new HashMap<>();

        if (json_frequencies != null && !json_frequencies.isEmpty()) {
            try {
                JSONObject json_object = new JSONObject(json_frequencies);

                if (json_object.has("freqs")) {
                    JSONArray freqs_array = json_object.getJSONArray("freqs");

                    for (int i = 0; i < freqs_array.length(); i++) {
                        JSONObject freq = freqs_array.getJSONObject(i);
                        String description = freq.optString("description", null);
                        String frequency_mhz = freq.optString("frequency_mhz", null);

                        if (description == null || description.isEmpty() || frequency_mhz == null
                                || frequency_mhz.isEmpty()) {
                            System.out.println("Error parsing JSON at index " + i + " for airport "
                                    + json_object.getString("code"));
                            continue;
                        }

                        frequenciesHashMap.put(description, frequency_mhz);
                    }
                } else {
                    System.out.println("No 'freqs' key found or it does not contain an array");
                }
            } catch (Exception exception) {
                System.err.println("Error parsing JSON");
            }
        } else {
            System.out.println("JSON is empty or null");
        }
        return frequenciesHashMap.isEmpty() ? null : frequenciesHashMap;
    }
}
