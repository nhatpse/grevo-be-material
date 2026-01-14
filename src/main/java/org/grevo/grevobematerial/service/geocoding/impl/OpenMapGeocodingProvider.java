package org.grevo.grevobematerial.service.geocoding.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grevo.grevobematerial.dto.response.LocationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenMapGeocodingProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenMapGeocodingProvider.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openmap.api.key:}")
    private String apiKey;

    public OpenMapGeocodingProvider(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://mapapis.openmap.vn/v1")
                .defaultHeader("User-Agent", "GrevoApp/1.0 (contact@grevo.org)")
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Reverse geocode coordinates to structured address components.
     * 
     * @return LocationResponse with addressDetails, city, and ward
     */
    public LocationResponse reverseGeocode(double lat, double lng) {
        log.info("Attempting OpenMap.vn Reverse Geocode for {}, {}", lat, lng);
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenMap API Key is MISSING or EMPTY. Skipping OpenMap.");
            return null;
        }

        try {
            String uri = UriComponentsBuilder.fromPath("/geocode/reverse")
                    .queryParam("point.lat", lat)
                    .queryParam("point.lon", lng)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            log.info("Calling OpenMap URL: {} (Key length: {})", uri.replace(apiKey, "***"), apiKey.length());

            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (response == null) {
                log.warn("OpenMap response was null");
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.path("features");

            if (features.isArray() && features.size() > 0) {
                JsonNode props = features.get(0).path("properties");

                // Extract address components from OpenMap properties
                // OpenMap fields: name, street, neighbourhood, locality, region, country
                String name = getTextOrEmpty(props, "name");
                String street = getTextOrEmpty(props, "street");
                String neighbourhood = getTextOrEmpty(props, "neighbourhood");
                String locality = getTextOrEmpty(props, "locality"); // Ward/Commune
                String region = getTextOrEmpty(props, "region"); // City/Province

                // Build addressDetails: prioritize name > street > neighbourhood
                String addressDetails = buildAddressDetails(name, street, neighbourhood);

                // Ward: use locality (phường/xã)
                String ward = locality;

                // City: use region (tỉnh/thành phố)
                String city = region;

                log.info("Parsed address - Details: {}, Ward: {}, City: {}", addressDetails, ward, city);

                return LocationResponse.builder()
                        .addressDetails(addressDetails)
                        .city(city)
                        .ward(ward)
                        .build();
            }

            log.info("OpenMap response parsed but no features found.");
            return null;

        } catch (Exception e) {
            log.warn("OpenMap geocoding failed: {}", e.getMessage());
            return null;
        }
    }

    private String getTextOrEmpty(JsonNode node, String field) {
        if (!node.has(field)) {
            return "";
        }
        JsonNode value = node.get(field);
        // Handle JSON null or "null" string
        if (value.isNull()) {
            return "";
        }
        String text = value.asText();
        // Also filter out literal "null" string
        if (text == null || "null".equalsIgnoreCase(text)) {
            return "";
        }
        return text.trim();
    }

    private String buildAddressDetails(String name, String street, String neighbourhood) {
        StringBuilder sb = new StringBuilder();

        // Add name if it's valid and different from street (e.g., a POI name)
        if (isValidString(name) && !name.equals(street)) {
            sb.append(name);
        }

        // Add street if valid
        if (isValidString(street)) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(street);
        }

        // Add neighbourhood as fallback if no street
        if (sb.length() == 0 && isValidString(neighbourhood)) {
            sb.append(neighbourhood);
        }

        return sb.toString();
    }

    private boolean isValidString(String str) {
        return str != null && !str.isEmpty() && !"null".equalsIgnoreCase(str);
    }

    public String autocomplete(String text, String sessionToken) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            // Docs: /autocomplete?text={text}&sessiontoken={token}
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/autocomplete")
                    .queryParam("text", text)
                    .queryParam("apikey", apiKey);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                builder.queryParam("sessiontoken", sessionToken);
            }

            return restClient.get().uri(builder.toUriString()).retrieve().body(String.class);
        } catch (Exception e) {
            log.error("OpenMap autocomplete failed: {}", e.getMessage());
            return null;
        }
    }

    public String getPlaceDetail(String ids, String sessionToken) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            // Docs: /place?ids={id}&sessiontoken={token}
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/place")
                    .queryParam("ids", ids)
                    .queryParam("apikey", apiKey);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                builder.queryParam("sessiontoken", sessionToken);
            }

            return restClient.get().uri(builder.toUriString()).retrieve().body(String.class);
        } catch (Exception e) {
            log.error("OpenMap place details failed: {}", e.getMessage());
            return null;
        }
    }

    public String getStaticMapUrl(double lat, double lng) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("maps.openmap.vn")
                .path("/api/staticmap")
                .queryParam("center", lat + "," + lng)
                .queryParam("zoom", 15)
                .queryParam("size", "600x300")
                .queryParam("markers", "color:red|" + lat + "," + lng)
                .queryParam("apikey", apiKey)
                .build()
                .toUriString();
    }
}
