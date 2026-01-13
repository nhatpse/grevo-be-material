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
    private final RestClient overpassClient;
    private final ObjectMapper objectMapper;
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    public NominatimGeocodingProvider(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(NOMINATIM_URL)
                .defaultHeader("User-Agent", "GrevoApp/1.0 (contact@grevo.org)")
                .build();
        this.overpassClient = RestClient.builder()
                .baseUrl(OVERPASS_URL)
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

            // Extract street from display_name for maximum detail
            String displayName = root.has("display_name") ? root.get("display_name").asText() : null;
            String rawWardForStreet = getText(addressNode, "suburb", "quarter", "village", "town", "neighbourhood");

            String street = null;
            if (displayName != null && rawWardForStreet != null) {
                // Extract everything before ward/suburb from display_name
                int wardIndex = displayName.indexOf(rawWardForStreet);
                if (wardIndex > 0) {
                    street = displayName.substring(0, wardIndex).trim();
                    // Remove trailing comma
                    if (street.endsWith(",")) {
                        street = street.substring(0, street.length() - 1).trim();
                    }
                }
            }

            // Fallback to building street from individual components if display_name
            // extraction failed
            if (street == null || street.isEmpty()) {
                String placeName = root.has("name") && !root.get("name").asText().isEmpty()
                        ? root.get("name").asText()
                        : null;
                String road = getText(addressNode, "road", "pedestrian", "street");
                String houseNumber = getText(addressNode, "house_number");
                String amenity = getText(addressNode, "amenity", "shop", "office", "building", "tourism", "leisure");

                StringBuilder streetBuilder = new StringBuilder();
                if (amenity != null) {
                    streetBuilder.append(amenity);
                }
                if (placeName != null && !placeName.equals(road) && !placeName.equals(amenity)) {
                    if (streetBuilder.length() > 0)
                        streetBuilder.append(", ");
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
                street = streetBuilder.toString().trim();
            }

            // Enhancement: If street lacks specific POI info (building, shop, house
            // number),
            // try to find nearby POI using Overpass API for more detailed address
            String road = getText(addressNode, "road", "pedestrian", "street");
            String houseNumber = getText(addressNode, "house_number");
            String amenity = getText(addressNode, "amenity", "shop", "office", "building", "tourism", "leisure");

            // Check if we need to enhance the street with nearby POI
            // We need enhancement if there's no specific building/shop/house number
            boolean needsPOIEnhancement = (houseNumber == null && amenity == null &&
                    (street == null || !containsSpecificLocation(street)));

            System.out.println("[DEBUG] Street: " + street);
            System.out.println("[DEBUG] containsSpecificLocation: "
                    + (street != null ? containsSpecificLocation(street) : "null"));
            System.out.println("[DEBUG] needsPOIEnhancement: " + needsPOIEnhancement);

            if (needsPOIEnhancement) {
                String nearbyPOI = findNearbyPOI(latitude, longitude);
                System.out.println("[DEBUG] nearbyPOI from Overpass: " + nearbyPOI);
                if (nearbyPOI != null && !nearbyPOI.isEmpty()) {
                    if (street != null && !street.isEmpty()) {
                        street = nearbyPOI + ", " + street;
                    } else {
                        street = nearbyPOI + (road != null ? ", " + road : "");
                    }
                }
            }

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
            if (rawState == null && displayName != null) {
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

    /**
     * Check if street contains specific location indicators (building, block, house
     * number)
     * Optimized for Vietnam addresses
     */
    private boolean containsSpecificLocation(String street) {
        if (street == null || street.isEmpty())
            return false;
        String lower = street.toLowerCase();

        // Vietnam-specific building/location indicators
        return lower.contains("tòa") || // Tòa nhà
                lower.contains("block") || // Block
                lower.contains("lô") || // Lô đất
                lower.contains("căn") || // Căn hộ
                lower.contains("tầng") || // Tầng/Floor
                lower.contains("lầu") || // Lầu
                lower.contains("phòng") || // Phòng
                lower.contains("shop") || // Shop
                lower.contains("kiot") || // Kiot
                lower.contains("sảnh") || // Sảnh
                // House number patterns: "số 123", "123 đường", standalone numbers at start
                lower.matches("^\\d+\\s+.*") || // Starts with number followed by space
                lower.matches(".*số\\s+\\d+.*"); // Contains "số 123" pattern
    }

    private String getText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).asText().isEmpty()) {
                return node.get(key).asText();
            }
        }
        return null;
    }

    /**
     * Find nearest POI (shop, amenity, building) within 50m radius using Overpass
     * API - optimized for Vietnam locations
     */
    private String findNearbyPOI(double latitude, double longitude) {
        try {
            // Overpass query to find POIs within 100m - prioritize buildings and name
            //  places
            String query = String.format(
                    "[out:json][timeout:5];" +
                            "(" +
                            // Buildings first (most accurate for Vietnam apartments/condos)
                            "  way[\"name\"][\"building\"](around:100,%f,%f);" +
                            "  node[\"name\"][\"building\"](around:100,%f,%f);" +
                            // Residential areas (Vinhomes, etc.)
                            "  node[\"name\"][\"landuse\"=\"residential\"](around:100,%f,%f);" +
                            "  node[\"name\"][\"residential\"](around:100,%f,%f);" +
                            // Shops and amenities
                            "  node[\"name\"][\"shop\"](around:100,%f,%f);" +
                            "  node[\"name\"][\"amenity\"](around:100,%f,%f);" +
                            "  way[\"name\"][\"shop\"](around:100,%f,%f);" +
                            "  way[\"name\"][\"amenity\"](around:100,%f,%f);" +
                            // Offices and tourism
                            "  node[\"name\"][\"office\"](around:100,%f,%f);" +
                            "  node[\"name\"][\"tourism\"](around:100,%f,%f);" +
                            ");" +
                            "out center 1;",
                    latitude, longitude, latitude, longitude, latitude, longitude, latitude, longitude,
                    latitude, longitude, latitude, longitude, latitude, longitude, latitude, longitude,
                    latitude, longitude, latitude, longitude);

            String response = overpassClient.post()
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body("data=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8))
                    .retrieve()
                    .body(String.class);

            if (response == null)
                return null;

            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.path("elements");

            if (elements.isArray() && elements.size() > 0) {
                JsonNode firstElement = elements.get(0);
                JsonNode tags = firstElement.path("tags");

                if (tags.has("name")) {
                    String poiName = tags.get("name").asText();
                    // Also try to get house_number from POI
                    String houseNum = tags.has("addr:housenumber") ? tags.get("addr:housenumber").asText() : null;
                    String street = tags.has("addr:street") ? tags.get("addr:street").asText() : null;

                    StringBuilder result = new StringBuilder(poiName);
                    if (houseNum != null && street != null) {
                        result.append(", ").append(houseNum).append(" ").append(street);
                    }
                    return result.toString();
                }
            }
            return null;
        } catch (Exception e) {
            // Silently fail - Overpass is optional enhancement
            System.out.println("[DEBUG] Overpass error: " + e.getMessage());
            return null;
        }
    }
}
