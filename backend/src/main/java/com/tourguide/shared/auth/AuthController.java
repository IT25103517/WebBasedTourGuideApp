package com.tourguide.shared.auth;

import com.tourguide.shared.auth.service.AuthService;
import com.tourguide.shared.security.CurrentUser;
import com.tourguide.shared.security.CurrentUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Port of shared/auth.routes.js. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest body) {
        var result = service.register(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "Account created", "data", result));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest body) {
        var result = service.login(body);
        return Map.of("success", true, "message", "Logged in", "data", result);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@CurrentUser CurrentUserDetails user) {
        return Map.of("success", true, "data", service.me(user.id()));
    }
}
