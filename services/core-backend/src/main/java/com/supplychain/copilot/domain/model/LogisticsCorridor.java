package com.supplychain.copilot.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "logistics_corridors")
public class LogisticsCorridor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnore
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(name = "origin_region", nullable = false, length = 100)
    private String originRegion;

    @Column(name = "destination_region", nullable = false, length = 100)
    private String destinationRegion;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public LogisticsCorridor() {}

    public LogisticsCorridor(UUID id, Tenant tenant, String name, String originRegion, String destinationRegion, Boolean active, Instant createdAt) {
        this.id = id;
        this.tenant = tenant;
        this.name = name;
        this.originRegion = originRegion;
        this.destinationRegion = destinationRegion;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginRegion() { return originRegion; }
    public void setOriginRegion(String originRegion) { this.originRegion = originRegion; }

    public String getDestinationRegion() { return destinationRegion; }
    public void setDestinationRegion(String destinationRegion) { this.destinationRegion = destinationRegion; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static LogisticsCorridorBuilder builder() {
        return new LogisticsCorridorBuilder();
    }

    public static class LogisticsCorridorBuilder {
        private UUID id;
        private Tenant tenant;
        private String name;
        private String originRegion;
        private String destinationRegion;
        private Boolean active = true;
        private Instant createdAt;

        public LogisticsCorridorBuilder id(UUID id) { this.id = id; return this; }
        public LogisticsCorridorBuilder tenant(Tenant tenant) { this.tenant = tenant; return this; }
        public LogisticsCorridorBuilder name(String name) { this.name = name; return this; }
        public LogisticsCorridorBuilder originRegion(String originRegion) { this.originRegion = originRegion; return this; }
        public LogisticsCorridorBuilder destinationRegion(String destinationRegion) { this.destinationRegion = destinationRegion; return this; }
        public LogisticsCorridorBuilder active(Boolean active) { this.active = active; return this; }
        public LogisticsCorridorBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public LogisticsCorridor build() {
            return new LogisticsCorridor(id, tenant, name, originRegion, destinationRegion, active, createdAt);
        }
    }
}
