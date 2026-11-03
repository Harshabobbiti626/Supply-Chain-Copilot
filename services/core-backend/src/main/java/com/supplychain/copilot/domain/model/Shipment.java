package com.supplychain.copilot.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corridor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LogisticsCorridor corridor;

    @Column(name = "tracking_number", unique = true, nullable = false, length = 100)
    private String trackingNumber;

    @Column(name = "carrier_code", nullable = false, length = 50)
    private String carrierCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, length = 50)
    private TransportMode transportMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 50)
    private ShipmentStatus currentStatus = ShipmentStatus.IN_TRANSIT;

    @Column(name = "current_latitude", precision = 9, scale = 6)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 9, scale = 6)
    private BigDecimal currentLongitude;

    @Column(name = "origin_port", nullable = false, length = 100)
    private String originPort;

    @Column(name = "destination_port", nullable = false, length = 100)
    private String destinationPort;

    @Column(name = "estimated_arrival")
    private Instant estimatedArrival;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<ShipmentWaypoint> waypoints = new ArrayList<>();

    public Shipment() {}

    public Shipment(UUID id, LogisticsCorridor corridor, String trackingNumber, String carrierCode,
                    TransportMode transportMode, ShipmentStatus currentStatus, BigDecimal currentLatitude,
                    BigDecimal currentLongitude, String originPort, String destinationPort,
                    Instant estimatedArrival, Integer version, Instant updatedAt, List<ShipmentWaypoint> waypoints) {
        this.id = id;
        this.corridor = corridor;
        this.trackingNumber = trackingNumber;
        this.carrierCode = carrierCode;
        this.transportMode = transportMode;
        this.currentStatus = currentStatus != null ? currentStatus : ShipmentStatus.IN_TRANSIT;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.originPort = originPort;
        this.destinationPort = destinationPort;
        this.estimatedArrival = estimatedArrival;
        this.version = version != null ? version : 0;
        this.updatedAt = updatedAt;
        this.waypoints = waypoints != null ? waypoints : new ArrayList<>();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LogisticsCorridor getCorridor() { return corridor; }
    public void setCorridor(LogisticsCorridor corridor) { this.corridor = corridor; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }

    public TransportMode getTransportMode() { return transportMode; }
    public void setTransportMode(TransportMode transportMode) { this.transportMode = transportMode; }

    public ShipmentStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(ShipmentStatus currentStatus) { this.currentStatus = currentStatus; }

    public BigDecimal getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(BigDecimal currentLatitude) { this.currentLatitude = currentLatitude; }

    public BigDecimal getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(BigDecimal currentLongitude) { this.currentLongitude = currentLongitude; }

    public String getOriginPort() { return originPort; }
    public void setOriginPort(String originPort) { this.originPort = originPort; }

    public String getDestinationPort() { return destinationPort; }
    public void setDestinationPort(String destinationPort) { this.destinationPort = destinationPort; }

    public Instant getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ShipmentWaypoint> getWaypoints() { return waypoints; }
    public void setWaypoints(List<ShipmentWaypoint> waypoints) { this.waypoints = waypoints; }

    public static ShipmentBuilder builder() {
        return new ShipmentBuilder();
    }

    public static class ShipmentBuilder {
        private UUID id;
        private LogisticsCorridor corridor;
        private String trackingNumber;
        private String carrierCode;
        private TransportMode transportMode;
        private ShipmentStatus currentStatus = ShipmentStatus.IN_TRANSIT;
        private BigDecimal currentLatitude;
        private BigDecimal currentLongitude;
        private String originPort;
        private String destinationPort;
        private Instant estimatedArrival;
        private Integer version = 0;
        private Instant updatedAt;
        private List<ShipmentWaypoint> waypoints = new ArrayList<>();

        public ShipmentBuilder id(UUID id) { this.id = id; return this; }
        public ShipmentBuilder corridor(LogisticsCorridor corridor) { this.corridor = corridor; return this; }
        public ShipmentBuilder trackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return this; }
        public ShipmentBuilder carrierCode(String carrierCode) { this.carrierCode = carrierCode; return this; }
        public ShipmentBuilder transportMode(TransportMode transportMode) { this.transportMode = transportMode; return this; }
        public ShipmentBuilder currentStatus(ShipmentStatus currentStatus) { this.currentStatus = currentStatus; return this; }
        public ShipmentBuilder currentLatitude(BigDecimal currentLatitude) { this.currentLatitude = currentLatitude; return this; }
        public ShipmentBuilder currentLongitude(BigDecimal currentLongitude) { this.currentLongitude = currentLongitude; return this; }
        public ShipmentBuilder originPort(String originPort) { this.originPort = originPort; return this; }
        public ShipmentBuilder destinationPort(String destinationPort) { this.destinationPort = destinationPort; return this; }
        public ShipmentBuilder estimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; return this; }
        public ShipmentBuilder version(Integer version) { this.version = version; return this; }
        public ShipmentBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public ShipmentBuilder waypoints(List<ShipmentWaypoint> waypoints) { this.waypoints = waypoints; return this; }

        public Shipment build() {
            return new Shipment(id, corridor, trackingNumber, carrierCode, transportMode, currentStatus, currentLatitude, currentLongitude, originPort, destinationPort, estimatedArrival, version, updatedAt, waypoints);
        }
    }
}
