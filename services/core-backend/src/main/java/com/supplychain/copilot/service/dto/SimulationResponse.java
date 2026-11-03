package com.supplychain.copilot.service.dto;

import java.util.List;
import java.util.UUID;

public class SimulationResponse {
    private String simulationId;
    private UUID shipmentId;
    private String disruptedPort;
    private List<AlternativeRouteOption> recommendedOptions;
    private String executiveSummary;
    private Long computationTimeMs;
    private String solverEngine; // "PYTHON_OR_TOOLS" or "JAVA_DETERMINISTIC_MILP"

    public SimulationResponse() {}

    public SimulationResponse(String simulationId, UUID shipmentId, String disruptedPort,
                              List<AlternativeRouteOption> recommendedOptions,
                              String executiveSummary, Long computationTimeMs, String solverEngine) {
        this.simulationId = simulationId;
        this.shipmentId = shipmentId;
        this.disruptedPort = disruptedPort;
        this.recommendedOptions = recommendedOptions;
        this.executiveSummary = executiveSummary;
        this.computationTimeMs = computationTimeMs;
        this.solverEngine = solverEngine;
    }

    public String getSimulationId() { return simulationId; }
    public void setSimulationId(String simulationId) { this.simulationId = simulationId; }

    public UUID getShipmentId() { return shipmentId; }
    public void setShipmentId(UUID shipmentId) { this.shipmentId = shipmentId; }

    public String getDisruptedPort() { return disruptedPort; }
    public void setDisruptedPort(String disruptedPort) { this.disruptedPort = disruptedPort; }

    public List<AlternativeRouteOption> getRecommendedOptions() { return recommendedOptions; }
    public void setRecommendedOptions(List<AlternativeRouteOption> recommendedOptions) { this.recommendedOptions = recommendedOptions; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public Long getComputationTimeMs() { return computationTimeMs; }
    public void setComputationTimeMs(Long computationTimeMs) { this.computationTimeMs = computationTimeMs; }

    public String getSolverEngine() { return solverEngine; }
    public void setSolverEngine(String solverEngine) { this.solverEngine = solverEngine; }

    public static SimulationResponseBuilder builder() {
        return new SimulationResponseBuilder();
    }

    public static class SimulationResponseBuilder {
        private String simulationId;
        private UUID shipmentId;
        private String disruptedPort;
        private List<AlternativeRouteOption> recommendedOptions;
        private String executiveSummary;
        private Long computationTimeMs;
        private String solverEngine;

        public SimulationResponseBuilder simulationId(String simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public SimulationResponseBuilder shipmentId(UUID shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        public SimulationResponseBuilder disruptedPort(String disruptedPort) {
            this.disruptedPort = disruptedPort;
            return this;
        }

        public SimulationResponseBuilder recommendedOptions(List<AlternativeRouteOption> recommendedOptions) {
            this.recommendedOptions = recommendedOptions;
            return this;
        }

        public SimulationResponseBuilder executiveSummary(String executiveSummary) {
            this.executiveSummary = executiveSummary;
            return this;
        }

        public SimulationResponseBuilder computationTimeMs(Long computationTimeMs) {
            this.computationTimeMs = computationTimeMs;
            return this;
        }

        public SimulationResponseBuilder solverEngine(String solverEngine) {
            this.solverEngine = solverEngine;
            return this;
        }

        public SimulationResponse build() {
            SimulationResponse res = new SimulationResponse();
            res.simulationId = this.simulationId;
            res.shipmentId = this.shipmentId;
            res.disruptedPort = this.disruptedPort;
            res.recommendedOptions = this.recommendedOptions;
            res.executiveSummary = this.executiveSummary;
            res.computationTimeMs = this.computationTimeMs;
            res.solverEngine = this.solverEngine;
            return res;
        }
    }
}
