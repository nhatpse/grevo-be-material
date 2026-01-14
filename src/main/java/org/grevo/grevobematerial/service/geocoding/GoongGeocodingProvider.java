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
 * Geocoding service using Goong.io REST API V2.
 * Base URL: https://rsapi.goong.io
 * 
 * V2 Features:
 * - Updated administrative units after Vietnam's district/ward mergers
 * - Supports has_deprecated_administrative_unit param for backward
 * compatibility
 * 
 * @see <a href="https://help.goong.io/kb/rest-api-v2/">Goong API V2
 *      Documentation</a>
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
     * Reverse geocode coordinates to structured address using V2 API.
     * API: GET /v2/geocode?latlng={lat},{lng}&api_key={key}
     * 
     * V2 returns compound object with commune (ward) and province (city).
     */
    public LocationResponse reverseGeocode(double lat, double lng) {
        log.info("Goong.io V2 Reverse Geocode for {}, {}", lat, lng);
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Goong API Key is MISSING. Skipping geocoding.");
            return null;
        }

        try {
            String uri = UriComponentsBuilder.fromPath("/v2/geocode")
                    .queryParam("latlng", lat + "," + lng)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            String response = restClient.get().uri(uri).retrieve().body(String.class);
            if (response == null) {
                log.warn("Goong V2 response was null");
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);

                // V2 uses compound object for structured address
                JsonNode compound = firstResult.path("compound");
                String name = getTextOrEmpty(firstResult, "name");
                String formattedAddress = getTextOrEmpty(firstResult, "formatted_address");

                String addressDetails = name;
                String ward = getTextOrEmpty(compound, "commune");
                String city = getTextOrEmpty(compound, "province");

                // Fallback: use formatted_address for addressDetails
                if (addressDetails.isEmpty() && !formattedAddress.isEmpty()) {
                    String[] parts = formattedAddress.split(",");
                    if (parts.length > 0)
                        addressDetails = parts[0].trim();
                }

                log.info("V2 Parsed: Details={}, Ward={}, City={}", addressDetails, ward, city);

                return LocationResponse.builder()
                        .addressDetails(addressDetails)
                        .city(city)
                        .ward(ward)
                        .build();
            }

            log.info("Goong V2 response has no results.");
            return null;

        } catch (Exception e) {
            log.warn("Goong V2 geocoding failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Autocomplete search for places using V2 API.
     * API: GET /v2/place/autocomplete?input={text}&api_key={key}
     */
    public String autocomplete(String text, String sessionToken) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/v2/place/autocomplete")
                    .queryParam("input", text)
                    .queryParam("api_key", apiKey);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                builder.queryParam("sessiontoken", sessionToken);
            }

            return restClient.get().uri(builder.toUriString()).retrieve().body(String.class);
        } catch (Exception e) {
            log.error("Goong V2 autocomplete failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get place details by place_id using V2 API.
     * API: GET /v2/place/detail?place_id={id}&api_key={key}
     */
    public String getPlaceDetail(String placeId, String sessionToken) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/v2/place/detail")
                    .queryParam("place_id", placeId)
                    .queryParam("api_key", apiKey);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                builder.queryParam("sessiontoken", sessionToken);
            }

            return restClient.get().uri(builder.toUriString()).retrieve().body(String.class);
        } catch (Exception e) {
            log.error("Goong V2 place details failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get street name from coordinates using V2 Geocode Street API.
     * API: GET /v2/geocode/street?latlng={lat},{lng}&api_key={key}
     */
    public String getStreetName(double lat, double lng) {
        if (apiKey == null || apiKey.isBlank())
            return null;
        try {
            String uri = UriComponentsBuilder.fromPath("/v2/geocode/street")
                    .queryParam("latlng", lat + "," + lng)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            String response = restClient.get().uri(uri).retrieve().body(String.class);
            if (response == null)
                return null;

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                return getTextOrEmpty(results.get(0), "name");
            }
            return null;
        } catch (Exception e) {
            log.error("Goong V2 geocode street failed: {}", e.getMessage());
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
