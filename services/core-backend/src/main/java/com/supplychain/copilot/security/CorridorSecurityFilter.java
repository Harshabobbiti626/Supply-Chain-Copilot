package com.supplychain.copilot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CorridorSecurityFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String CORRIDORS_HEADER = "X-Authorized-Corridors";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String tenantHeader = request.getHeader(TENANT_HEADER);
            if (tenantHeader != null && !tenantHeader.isBlank()) {
                try {
                    TenantContext.setTenantId(UUID.fromString(tenantHeader.trim()));
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, keep null
                }
            }

            String corridorsHeader = request.getHeader(CORRIDORS_HEADER);
            if (corridorsHeader != null && !corridorsHeader.isBlank()) {
                Set<UUID> corridors = Arrays.stream(corridorsHeader.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> {
                            try {
                                return UUID.fromString(s);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(u -> u != null)
                        .collect(Collectors.toSet());
                TenantContext.setAuthorizedCorridors(corridors);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

