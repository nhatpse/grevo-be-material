package org.grevo.grevobematerial.service.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Geocoding service using Goong.io APIs.
 * Base URL: https://rsapi.goong.io
 * 
 * @see <a href="https://docs.goong.io/rest/">Goong API Documentation</a>
 */
@Service
public class GoongGeocodingProvider {

    private static final Logger log = LoggerFactory.getLogger(GoongGeocodingProvider.class);
    private static final String BASE_URL = "https://rsapi.goong.io";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${goong.api.key:}")
    private String apiKey;

    public GoongGeocodingProvider(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", "GrevoApp/1.0 (contact@grevo.org)")
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Reverse geocode coordinates to structured address.
     * API: GET /geocode?latlng={lat},{lng}&api_key={key}
     */
    public LocationResponse reverseGeocode(double lat, double lng) {
        log.info("Goong.io Reverse Geocode for {}, {}", lat, lng);
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Goong API Key is MISSING. Skipping geocoding.");
            return null;
        }

        try {
            String uri = UriComponentsBuilder.fromPath("/geocode")
                    .queryParam("latlng", lat + "," + lng)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            String response = restClient.get().uri(uri).retrieve().body(String.class);
            if (response == null) {
                log.warn("Goong response was null");
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                JsonNode addressComponents = firstResult.path("address_components");
                String formattedAddress = getTextOrEmpty(firstResult, "formatted_address");

                String addressDetails = "";
                String ward = "";
                String city = "";

                if (addressComponents.isArray()) {
                    int size = addressComponents.size();

                    // Build addressDetails from first components (excluding last 3: ward, district,
                    // city)
                    StringBuilder detailsBuilder = new StringBuilder();
                    int detailEndIndex = Math.max(0, size - 3);

                    for (int i = 0; i < detailEndIndex; i++) {
                        String component = getTextOrEmpty(addressComponents.get(i), "long_name").trim();
                        if (!component.isEmpty()) {
                            if (detailsBuilder.length() > 0)
                                detailsBuilder.append(", ");
                            detailsBuilder.append(component);
                        }
                    }
                    addressDetails = detailsBuilder.toString();

                    // Vietnamese address structure:
                    // - size-1: City/Province
                    // - size-2: District
                    // - size-3: Ward
                    if (size >= 3) {
                        ward = getTextOrEmpty(addressComponents.get(size - 3), "long_name").trim();
                    }

                    if (size == 2 && addressDetails.isEmpty()) {
                        addressDetails = getTextOrEmpty(addressComponents.get(0), "long_name").trim();
                        ward = "";
                    }

                    if (size >= 1) {
                        city = getTextOrEmpty(addressComponents.get(size - 1), "long_name").trim();
                    }

                    if (addressDetails.isEmpty() && size >= 4) {
                        addressDetails = getTextOrEmpty(addressComponents.get(0), "long_name").trim();
                    }
                }

                // Fallback: use formatted_address
                if (addressDetails.isEmpty() && !formattedAddress.isEmpty()) {
                    String[] parts = formattedAddress.split(",");
                    if (parts.length > 0)
                        addressDetails = parts[0].trim();
                }

                log.info("Parsed: Details={}, Ward={}, City={}", addressDetails, ward, city);

                return LocationResponse.builder()
                        .addressDetails(addressDetails)
                        .city(city)
                        .ward(ward)
                        .build();
            }

            log.info("Goong response has no results.");
            return null;

        } catch (Exception e) {
            log.warn("Goong geocoding failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Autocomplete search for places.
     * API: GET /Place/AutoComplete?input={text}&api_key={key}
     */
    public String autocomplete(String text, String sessionToken) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/Place/AutoComplete")
                    .queryParam("input", text)
                    .queryParam("api_key", apiKey);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                builder.queryParam("sessiontoken", sessionToken);
            }

            return restClient.get().uri(builder.toUriString()).retrieve().body(String.class);
        } catch (Exception e) {
            log.error("Goong autocomplete failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get place details by place_id.
     * API: GET /Place/Detail?place_id={id}&api_key={key}
     */
    public String getPlaceDetail(String placeId, String sessionToken) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/Place/Detail")
                    .queryParam("place_id", placeId)
                    .queryParam("api_key", apiKey);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                builder.queryParam("sessiontoken", sessionToken);
            }

            return restClient.get().uri(builder.toUriString()).retrieve().body(String.class);
        } catch (Exception e) {
            log.error("Goong place details failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate static map URL for a location.
     */
    public String getStaticMapUrl(double lat, double lng) {
        if (apiKey == null || apiKey.isBlank())
            return null;

        String coords = lat + "," + lng;
        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("rsapi.goong.io")
                .path("/staticmap/route")
                .queryParam("origin", coords)
                .queryParam("destination", coords)
                .queryParam("width", 600)
                .queryParam("height", 300)
                .queryParam("vehicle", "car")
                .queryParam("api_key", apiKey)
                .build()
                .toUriString();
    }

    private String getTextOrEmpty(JsonNode node, String field) {
        if (node == null || !node.has(field))
            return "";
        JsonNode value = node.get(field);
        if (value.isNull())
            return "";
        String text = value.asText();
        return (text == null || "null".equalsIgnoreCase(text)) ? "" : text.trim();
    }
}
