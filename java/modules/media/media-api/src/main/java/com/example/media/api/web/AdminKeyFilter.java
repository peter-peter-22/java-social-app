package com.example.media.api.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Guards the {@code /api/admin/**} endpoints with a static key header. The key is configured as
 * {@code media.admin-key} (the deployment sets it through {@code MEDIA_ADMIN_KEY}); a missing or
 * wrong {@code X-Admin-Key} is 401. Client upload and read requests pass through untouched.
 *
 * <p>The key is the trust anchor of the admin boundary: it is compared in constant time so a wrong
 * key leaks no timing hint, and a blank configured key fails startup because it would authorize
 * every request that sends an empty header.</p>
 */
@Component
@NullMarked
public class AdminKeyFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Admin-Key";

    private final byte[] adminKey;

    public AdminKeyFilter(@Value("${media.admin-key}") String adminKey) {
        if (adminKey.isBlank())
            throw new IllegalStateException("media.admin-key must not be blank");
        this.adminKey = adminKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var presented = request.getHeader(HEADER);
        if (presented == null
                || !MessageDigest.isEqual(adminKey, presented.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }
}
