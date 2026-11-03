package com.supplychain.copilot.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "tenant_code", unique = true, nullable = false, length = 50)
    private String tenantCode;

    @Column(nullable = false, length = 50)
    private String tier = "STANDARD";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public Tenant() {}

    public Tenant(UUID id, String name, String tenantCode, String tier, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.tenantCode = tenantCode;
        this.tier = tier != null ? tier : "STANDARD";
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static TenantBuilder builder() {
        return new TenantBuilder();
    }

    public static class TenantBuilder {
        private UUID id;
        private String name;
        private String tenantCode;
        private String tier = "STANDARD";
        private Instant createdAt;

        public TenantBuilder id(UUID id) { this.id = id; return this; }
        public TenantBuilder name(String name) { this.name = name; return this; }
        public TenantBuilder tenantCode(String tenantCode) { this.tenantCode = tenantCode; return this; }
        public TenantBuilder tier(String tier) { this.tier = tier; return this; }
        public TenantBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Tenant build() {
            return new Tenant(id, name, tenantCode, tier, createdAt);
        }
    }
}
