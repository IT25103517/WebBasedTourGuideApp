package com.tourguide.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Port of `app.get('/api/health', ...)` in app.js. */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("success", true, "message", "Web Based Tour Guide API is running");
    }
}
