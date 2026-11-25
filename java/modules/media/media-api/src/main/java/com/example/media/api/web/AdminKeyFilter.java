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

/**
 * Guards the {@code /api/admin/**} endpoints with a static key header until the planned upload
 * signatures and permissions land. The key is configured as {@code media.admin-key} (the deployment
 * sets it through {@code MEDIA_ADMIN_KEY}); a missing or wrong {@code X-Admin-Key} is 401. Client
 * upload and read requests pass through untouched.
 */
@Component
@NullMarked
public class AdminKeyFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Admin-Key";

    private final String adminKey;

    public AdminKeyFilter(@Value("${media.admin-key}") String adminKey) {
        this.adminKey = adminKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!adminKey.equals(request.getHeader(HEADER))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }
}
