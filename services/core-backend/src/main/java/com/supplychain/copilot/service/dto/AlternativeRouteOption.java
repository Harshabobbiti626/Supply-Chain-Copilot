package com.supplychain.copilot.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class AlternativeRouteOption {

    @NotBlank(message = "Destination port code is required")
    private String destinationPort;

    @NotBlank(message = "Port name is required")
    private String portName;

    private UUID warehouseId;

    @NotNull(message = "Allocated TEU must not be null")
    private Integer allocatedTeu;

    private BigDecimal additionalCostUsd;
    private Integer transitDelayHours;
    private Double confidenceScore;
    private String rationale;
    private List<WaypointDto> waypoints;

    public AlternativeRouteOption() {}

    public AlternativeRouteOption(String destinationPort, String portName, UUID warehouseId,
                                  Integer allocatedTeu, BigDecimal additionalCostUsd,
                                  Integer transitDelayHours, Double confidenceScore,
                                  String rationale, List<WaypointDto> waypoints) {
        this.destinationPort = destinationPort;
        this.portName = portName;
        this.warehouseId = warehouseId;
        this.allocatedTeu = allocatedTeu;
        this.additionalCostUsd = additionalCostUsd;
        this.transitDelayHours = transitDelayHours;
        this.confidenceScore = confidenceScore;
        this.rationale = rationale;
        this.waypoints = waypoints;
    }

    public String getDestinationPort() { return destinationPort; }
    public void setDestinationPort(String destinationPort) { this.destinationPort = destinationPort; }

    public String getPortName() { return portName; }
    public void setPortName(String portName) { this.portName = portName; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public Integer getAllocatedTeu() { return allocatedTeu; }
    public void setAllocatedTeu(Integer allocatedTeu) { this.allocatedTeu = allocatedTeu; }

    public BigDecimal getAdditionalCostUsd() { return additionalCostUsd; }
    public void setAdditionalCostUsd(BigDecimal additionalCostUsd) { this.additionalCostUsd = additionalCostUsd; }

    public Integer getTransitDelayHours() { return transitDelayHours; }
    public void setTransitDelayHours(Integer transitDelayHours) { this.transitDelayHours = transitDelayHours; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public List<WaypointDto> getWaypoints() { return waypoints; }
    public void setWaypoints(List<WaypointDto> waypoints) { this.waypoints = waypoints; }

    public static AlternativeRouteOptionBuilder builder() {
        return new AlternativeRouteOptionBuilder();
    }

    public static class AlternativeRouteOptionBuilder {
        private String destinationPort;
        private String portName;
        private UUID warehouseId;
        private Integer allocatedTeu;
        private BigDecimal additionalCostUsd;
        private Integer transitDelayHours;
        private Double confidenceScore;
        private String rationale;
        private List<WaypointDto> waypoints;

        public AlternativeRouteOptionBuilder destinationPort(String destinationPort) {
            this.destinationPort = destinationPort;
            return this;
        }

        public AlternativeRouteOptionBuilder portName(String portName) {
            this.portName = portName;
            return this;
        }

        public AlternativeRouteOptionBuilder warehouseId(UUID warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }

        public AlternativeRouteOptionBuilder allocatedTeu(Integer allocatedTeu) {
            this.allocatedTeu = allocatedTeu;
            return this;
        }

        public AlternativeRouteOptionBuilder additionalCostUsd(BigDecimal additionalCostUsd) {
            this.additionalCostUsd = additionalCostUsd;
            return this;
        }

        public AlternativeRouteOptionBuilder transitDelayHours(Integer transitDelayHours) {
            this.transitDelayHours = transitDelayHours;
            return this;
        }

        public AlternativeRouteOptionBuilder confidenceScore(Double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public AlternativeRouteOptionBuilder rationale(String rationale) {
            this.rationale = rationale;
            return this;
        }

        public AlternativeRouteOptionBuilder waypoints(List<WaypointDto> waypoints) {
            this.waypoints = waypoints;
            return this;
        }

        public AlternativeRouteOption build() {
            AlternativeRouteOption opt = new AlternativeRouteOption();
            opt.destinationPort = this.destinationPort;
            opt.portName = this.portName;
            opt.warehouseId = this.warehouseId;
            opt.allocatedTeu = this.allocatedTeu;
            opt.additionalCostUsd = this.additionalCostUsd;
            opt.transitDelayHours = this.transitDelayHours;
            opt.confidenceScore = this.confidenceScore;
            opt.rationale = this.rationale;
            opt.waypoints = this.waypoints;
            return opt;
        }
    }

    public static class WaypointDto {
        private String name;
        private Double lat;
        private Double lon;

        public WaypointDto() {}

        public WaypointDto(String name, Double lat, Double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }

        public static WaypointDtoBuilder builder() {
            return new WaypointDtoBuilder();
        }

        public static class WaypointDtoBuilder {
            private String name;
            private Double lat;
            private Double lon;

            public WaypointDtoBuilder name(String name) { this.name = name; return this; }
            public WaypointDtoBuilder lat(Double lat) { this.lat = lat; return this; }
            public WaypointDtoBuilder lon(Double lon) { this.lon = lon; return this; }

            public WaypointDto build() {
                return new WaypointDto(name, lat, lon);
            }
        }
    }
}
