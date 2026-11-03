package com.supplychain.copilot.security;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Set<UUID>> AUTHORIZED_CORRIDORS = new ThreadLocal<>();

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setAuthorizedCorridors(Set<UUID> corridorIds) {
        AUTHORIZED_CORRIDORS.set(corridorIds);
    }

    public static Set<UUID> getAuthorizedCorridors() {
        Set<UUID> corridors = AUTHORIZED_CORRIDORS.get();
        return corridors != null ? corridors : Collections.emptySet();
    }

    public static boolean isCorridorAuthorized(UUID corridorId) {
        Set<UUID> corridors = AUTHORIZED_CORRIDORS.get();
        return corridors == null || corridors.isEmpty() || corridors.contains(corridorId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        AUTHORIZED_CORRIDORS.remove();
    }
}

