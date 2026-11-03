package com.supplychain.copilot.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CommitSimulationRequest {

    @NotBlank(message = "Simulation ID is required")
    private String simulationId;

    @NotNull(message = "Shipment ID is required")
    private UUID shipmentId;

    @NotNull(message = "Chosen alternative route option is required")
    private AlternativeRouteOption chosenOption;

    public CommitSimulationRequest() {}

    public CommitSimulationRequest(String simulationId, UUID shipmentId, AlternativeRouteOption chosenOption) {
        this.simulationId = simulationId;
        this.shipmentId = shipmentId;
        this.chosenOption = chosenOption;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(String simulationId) {
        this.simulationId = simulationId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public AlternativeRouteOption getChosenOption() {
        return chosenOption;
    }

    public void setChosenOption(AlternativeRouteOption chosenOption) {
        this.chosenOption = chosenOption;
    }

    public static CommitSimulationRequestBuilder builder() {
        return new CommitSimulationRequestBuilder();
    }

    public static class CommitSimulationRequestBuilder {
        private String simulationId;
        private UUID shipmentId;
        private AlternativeRouteOption chosenOption;

        public CommitSimulationRequestBuilder simulationId(String simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public CommitSimulationRequestBuilder shipmentId(UUID shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        public CommitSimulationRequestBuilder chosenOption(AlternativeRouteOption chosenOption) {
            this.chosenOption = chosenOption;
            return this;
        }

        public CommitSimulationRequest build() {
            CommitSimulationRequest req = new CommitSimulationRequest();
            req.simulationId = this.simulationId;
            req.shipmentId = this.shipmentId;
            req.chosenOption = this.chosenOption;
            return req;
        }
    }
}
