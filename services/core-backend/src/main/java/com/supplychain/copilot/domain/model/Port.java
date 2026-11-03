package com.supplychain.copilot.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ports")
public class Port {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "draft_depth_meters", nullable = false, precision = 4, scale = 1)
    private BigDecimal draftDepthMeters;

    @Column(name = "max_capacity_teu", nullable = false)
    private Integer maxCapacityTeu;

    @Column(name = "available_capacity_teu", nullable = false)
    private Integer availableCapacityTeu;

    @Column(name = "cost_per_teu", nullable = false, precision = 7, scale = 2)
    private BigDecimal costPerTeu;

    @Column(name = "transit_delay_hours", nullable = false)
    private Integer transitDelayHours;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public Port() {}

    public Port(UUID id, String code, String name, String city, String country,
                BigDecimal latitude, BigDecimal longitude, BigDecimal draftDepthMeters,
                Integer maxCapacityTeu, Integer availableCapacityTeu, BigDecimal costPerTeu,
                Integer transitDelayHours, Boolean active, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.draftDepthMeters = draftDepthMeters != null ? draftDepthMeters : BigDecimal.valueOf(15.0);
        this.maxCapacityTeu = maxCapacityTeu != null ? maxCapacityTeu : 1000;
        this.availableCapacityTeu = availableCapacityTeu != null ? availableCapacityTeu : 500;
        this.costPerTeu = costPerTeu != null ? costPerTeu : BigDecimal.valueOf(5.00);
        this.transitDelayHours = transitDelayHours != null ? transitDelayHours : 0;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getDraftDepthMeters() { return draftDepthMeters; }
    public void setDraftDepthMeters(BigDecimal draftDepthMeters) { this.draftDepthMeters = draftDepthMeters; }

    public Integer getMaxCapacityTeu() { return maxCapacityTeu; }
    public void setMaxCapacityTeu(Integer maxCapacityTeu) { this.maxCapacityTeu = maxCapacityTeu; }

    public Integer getAvailableCapacityTeu() { return availableCapacityTeu; }
    public void setAvailableCapacityTeu(Integer availableCapacityTeu) { this.availableCapacityTeu = availableCapacityTeu; }

    public BigDecimal getCostPerTeu() { return costPerTeu; }
    public void setCostPerTeu(BigDecimal costPerTeu) { this.costPerTeu = costPerTeu; }

    public Integer getTransitDelayHours() { return transitDelayHours; }
    public void setTransitDelayHours(Integer transitDelayHours) { this.transitDelayHours = transitDelayHours; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static PortBuilder builder() {
        return new PortBuilder();
    }

    public static class PortBuilder {
        private UUID id;
        private String code;
        private String name;
        private String city;
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal draftDepthMeters = BigDecimal.valueOf(15.0);
        private Integer maxCapacityTeu = 1000;
        private Integer availableCapacityTeu = 500;
        private BigDecimal costPerTeu = BigDecimal.valueOf(5.00);
        private Integer transitDelayHours = 0;
        private Boolean active = true;
        private Instant createdAt;

        public PortBuilder id(UUID id) { this.id = id; return this; }
        public PortBuilder code(String code) { this.code = code; return this; }
        public PortBuilder name(String name) { this.name = name; return this; }
        public PortBuilder city(String city) { this.city = city; return this; }
        public PortBuilder country(String country) { this.country = country; return this; }
        public PortBuilder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
        public PortBuilder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public PortBuilder draftDepthMeters(BigDecimal draftDepthMeters) { this.draftDepthMeters = draftDepthMeters; return this; }
        public PortBuilder maxCapacityTeu(Integer maxCapacityTeu) { this.maxCapacityTeu = maxCapacityTeu; return this; }
        public PortBuilder availableCapacityTeu(Integer availableCapacityTeu) { this.availableCapacityTeu = availableCapacityTeu; return this; }
        public PortBuilder costPerTeu(BigDecimal costPerTeu) { this.costPerTeu = costPerTeu; return this; }
        public PortBuilder transitDelayHours(Integer transitDelayHours) { this.transitDelayHours = transitDelayHours; return this; }
        public PortBuilder active(Boolean active) { this.active = active; return this; }
        public PortBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Port build() {
            return new Port(id, code, name, city, country, latitude, longitude, draftDepthMeters,
                    maxCapacityTeu, availableCapacityTeu, costPerTeu, transitDelayHours, active, createdAt);
        }
    }
}

