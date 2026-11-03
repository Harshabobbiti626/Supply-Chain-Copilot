package com.supplychain.copilot.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class SimulationRequest {

    private UUID shipmentId;

    @NotBlank(message = "Disrupted port code is required")
    private String disruptedPort;

    @DecimalMin(value = "0.0", message = "Cost weight must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Cost weight must be <= 1.0")
    private Double costWeight;

    @DecimalMin(value = "0.0", message = "Time weight must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Time weight must be <= 1.0")
    private Double timeWeight;

    @Min(value = 1, message = "Demand TEU must be at least 1")
    private Integer demandTeu;

    private String reason;

    public SimulationRequest() {}

    public SimulationRequest(UUID shipmentId, String disruptedPort, Double costWeight,
                             Double timeWeight, Integer demandTeu, String reason) {
        this.shipmentId = shipmentId;
        this.disruptedPort = disruptedPort;
        this.costWeight = costWeight;
        this.timeWeight = timeWeight;
        this.demandTeu = demandTeu;
        this.reason = reason;
    }

    public UUID getShipmentId() { return shipmentId; }
    public void setShipmentId(UUID shipmentId) { this.shipmentId = shipmentId; }

    public String getDisruptedPort() { return disruptedPort; }
    public void setDisruptedPort(String disruptedPort) { this.disruptedPort = disruptedPort; }

    public Double getCostWeight() { return costWeight; }
    public void setCostWeight(Double costWeight) { this.costWeight = costWeight; }

    public Double getTimeWeight() { return timeWeight; }
    public void setTimeWeight(Double timeWeight) { this.timeWeight = timeWeight; }

    public Integer getDemandTeu() { return demandTeu; }
    public void setDemandTeu(Integer demandTeu) { this.demandTeu = demandTeu; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public static SimulationRequestBuilder builder() {
        return new SimulationRequestBuilder();
    }

    public static class SimulationRequestBuilder {
        private UUID shipmentId;
        private String disruptedPort;
        private Double costWeight;
        private Double timeWeight;
        private Integer demandTeu;
        private String reason;

        public SimulationRequestBuilder shipmentId(UUID shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        public SimulationRequestBuilder disruptedPort(String disruptedPort) {
            this.disruptedPort = disruptedPort;
            return this;
        }

        public SimulationRequestBuilder costWeight(Double costWeight) {
            this.costWeight = costWeight;
            return this;
        }

        public SimulationRequestBuilder timeWeight(Double timeWeight) {
            this.timeWeight = timeWeight;
            return this;
        }

        public SimulationRequestBuilder demandTeu(Integer demandTeu) {
            this.demandTeu = demandTeu;
            return this;
        }

        public SimulationRequestBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public SimulationRequest build() {
            SimulationRequest req = new SimulationRequest();
            req.shipmentId = this.shipmentId;
            req.disruptedPort = this.disruptedPort;
            req.costWeight = this.costWeight;
            req.timeWeight = this.timeWeight;
            req.demandTeu = this.demandTeu;
            req.reason = this.reason;
            return req;
        }
    }
}
