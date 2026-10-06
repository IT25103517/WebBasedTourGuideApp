package com.tourguide.shared.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Port of middleware/auth.js. Runs on every request: if a Bearer token is
 * present and valid, the resolved user is stashed as a request attribute for
 * CurrentUserArgumentResolver to pick up. This filter never rejects a
 * request by itself - whether a missing/invalid token is fatal depends on
 * the target endpoint (see @CurrentUser(required = ...)), exactly like the
 * Node app applying `authenticate` to some routes and `optionalAuth` to
 * others.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String USER_ATTR = "currentUser";
    public static final String TOKEN_PRESENT_ATTR = "authTokenPresent";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            request.setAttribute(TOKEN_PRESENT_ATTR, Boolean.TRUE);
            String token = header.substring(7);
            Claims claims = jwtService.verify(token);
            if (claims != null) {
                request.setAttribute(USER_ATTR, jwtService.toUser(claims));
            }
        }
        chain.doFilter(request, response);
    }
}
