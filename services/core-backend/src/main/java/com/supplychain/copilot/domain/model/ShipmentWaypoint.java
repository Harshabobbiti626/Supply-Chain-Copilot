package com.supplychain.copilot.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment_waypoints")
public class ShipmentWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    @JsonIgnore
    private Shipment shipment;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private Boolean passed = false;

    @Column(name = "estimated_arrival")
    private Instant estimatedArrival;

    public ShipmentWaypoint() {}

    public ShipmentWaypoint(UUID id, Shipment shipment, Integer sequenceOrder, String locationName, BigDecimal latitude, BigDecimal longitude, Boolean passed, Instant estimatedArrival) {
        this.id = id;
        this.shipment = shipment;
        this.sequenceOrder = sequenceOrder;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.passed = passed != null ? passed : false;
        this.estimatedArrival = estimatedArrival;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public Instant getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public static ShipmentWaypointBuilder builder() {
        return new ShipmentWaypointBuilder();
    }

    public static class ShipmentWaypointBuilder {
        private UUID id;
        private Shipment shipment;
        private Integer sequenceOrder;
        private String locationName;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Boolean passed = false;
        private Instant estimatedArrival;

        public ShipmentWaypointBuilder id(UUID id) { this.id = id; return this; }
        public ShipmentWaypointBuilder shipment(Shipment shipment) { this.shipment = shipment; return this; }
        public ShipmentWaypointBuilder sequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; return this; }
        public ShipmentWaypointBuilder locationName(String locationName) { this.locationName = locationName; return this; }
        public ShipmentWaypointBuilder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
        public ShipmentWaypointBuilder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public ShipmentWaypointBuilder passed(Boolean passed) { this.passed = passed; return this; }
        public ShipmentWaypointBuilder estimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; return this; }

        public ShipmentWaypoint build() {
            return new ShipmentWaypoint(id, shipment, sequenceOrder, locationName, latitude, longitude, passed, estimatedArrival);
        }
    }
}
