package com.example.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class GeoLocationController {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/api/geocode/reverse", produces = "application/json")
    public ResponseEntity<String> reverseGeocode(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        String cacheKey = String.format("%.4f,%.4f", lat, lon);
        if (cache.containsKey(cacheKey)) {
            return ResponseEntity.ok(cache.get(cacheKey));
        }

        try {
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&addressdetails=1", lat, lon);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "ReWear-SMKN24-App/1.0");
            headers.set("Accept-Language", "id");

            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(URI.create(url), org.springframework.http.HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                cache.put(cacheKey, response.getBody());
                return ResponseEntity.ok(response.getBody());
            }
        } catch (Exception ex) {
            // Fallback response if external API is unreachable
            String fallback = "{\"address\":{\"road\":\"Jl. Bambu Apus No. 24\",\"suburb\":\"Cipayung\",\"city\":\"Jakarta Timur\",\"postcode\":\"13890\"}}";
            return ResponseEntity.ok(fallback);
        }

        return ResponseEntity.badRequest().body("{}");
    }
}
