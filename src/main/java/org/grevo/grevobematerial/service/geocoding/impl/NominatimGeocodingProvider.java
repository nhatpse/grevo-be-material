package org.grevo.grevobematerial.service.geocoding.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grevo.grevobematerial.dto.response.AddressResponse;
import org.grevo.grevobematerial.service.geocoding.GeocodingProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NominatimGeocodingProvider implements GeocodingProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";

    public NominatimGeocodingProvider(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(NOMINATIM_URL)
                .defaultHeader("User-Agent", "GrevoApp/1.0 (contact@grevo.org)")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AddressResponse reverseGeocode(double latitude, double longitude) {
        try {
            String uri = UriComponentsBuilder.fromPath("")
                    .queryParam("format", "jsonv2")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("accept-language", "vi")
                    .toUriString();

            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (response == null)
                return null;

            JsonNode root = objectMapper.readTree(response);
            JsonNode addressNode = root.path("address");

            if (addressNode.isMissingNode())
                return null;

            // Extract street with full detail (including place/building name)
            String placeName = root.has("name") && !root.get("name").asText().isEmpty()
                    ? root.get("name").asText()
                    : null;
            String road = getText(addressNode, "road", "pedestrian", "street");
            String houseNumber = getText(addressNode, "house_number");

            // Build street: [PlaceName], [HouseNumber] [Road]
            StringBuilder streetBuilder = new StringBuilder();
            if (placeName != null) {
                streetBuilder.append(placeName);
            }
            if (houseNumber != null || road != null) {
                if (streetBuilder.length() > 0)
                    streetBuilder.append(", ");
                if (houseNumber != null)
                    streetBuilder.append(houseNumber).append(" ");
                if (road != null)
                    streetBuilder.append(road);
            }
            String street = streetBuilder.toString().trim();

            // Extract raw fields (suburb prioritized over neighbourhood)
            String rawWard = getText(addressNode, "suburb", "quarter", "village", "town", "neighbourhood");
            String rawDistrict = getText(addressNode, "district", "county", "city_district");
            String rawCity = getText(addressNode, "city", "municipality");
            String rawState = getText(addressNode, "state", "region", "province");
            String isoCode = getText(addressNode, "ISO3166-2-lvl4");

            // Fallback: ISO Code -> Province name
            if (rawState == null && isoCode != null) {
                rawState = mapIsoCodeToName(isoCode);
            }

            // Fallback: Parse display_name for Province
            if (rawState == null && root.has("display_name")) {
                String displayName = root.get("display_name").asText();
                String anchor = (rawCity != null) ? rawCity : rawDistrict;
                if (anchor != null) {
                    rawState = extractStateFromDisplayName(displayName, anchor);
                }
            }

            String ward = rawWard;
            String city = isCentralCity(rawCity) ? rawCity : rawState;

            // Hamlet promotion: If ward is Ấp/Thôn/Tổ, try to get Xã from district
            if (ward != null && isHamlet(ward) && rawDistrict != null) {
                String dLower = rawDistrict.toLowerCase();
                if (dLower.startsWith("xã") || dLower.startsWith("thị trấn")) {
                    ward = rawDistrict;
                }
            }

            // If ward still null, try to get from district
            if (ward == null && rawDistrict != null) {
                String dLower = rawDistrict.toLowerCase();
                if (dLower.contains("phường") || dLower.contains("xã") || dLower.contains("thị trấn")) {
                    ward = rawDistrict;
                }
            }

            return AddressResponse.builder()
                    .street(street.trim().isEmpty() ? null : street.trim())
                    .ward(ward)
                    .city(city)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isHamlet(String name) {
        String n = name.toLowerCase();
        return n.startsWith("ấp") || n.startsWith("khu phố") || n.startsWith("tổ") || n.startsWith("thôn");
    }

    private boolean isCentralCity(String name) {
        if (name == null)
            return false;
        String n = name.toLowerCase();
        return n.contains("hồ chí minh") || n.contains("hà nội") || n.contains("hải phòng") ||
                n.contains("đà nẵng") || n.contains("cần thơ");
    }

    private String mapIsoCodeToName(String iso) {
        return switch (iso) {
            case "VN-SG" -> "Thành phố Hồ Chí Minh";
            case "VN-HN" -> "Hà Nội";
            case "VN-HP" -> "Hải Phòng";
            case "VN-DN" -> "Đà Nẵng";
            case "VN-CT" -> "Cần Thơ";
            case "VN-57" -> "Bình Dương";
            default -> null;
        };
    }

    private String extractStateFromDisplayName(String displayName, String anchor) {
        String[] parts = displayName.split(",");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].trim().equalsIgnoreCase(anchor)) {
                String candidate = parts[i + 1].trim();
                if (candidate.matches("\\d+"))
                    continue;
                if (candidate.equalsIgnoreCase("Việt Nam"))
                    continue;
                return candidate;
            }
        }
        return null;
    }

    private String getText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).asText().isEmpty()) {
                return node.get(key).asText();
            }
        }
        return null;
    }
}
