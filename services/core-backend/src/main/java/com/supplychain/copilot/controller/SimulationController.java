package com.supplychain.copilot.controller;

import com.supplychain.copilot.service.SimulationService;
import com.supplychain.copilot.service.dto.CommitSimulationRequest;
import com.supplychain.copilot.service.dto.SimulationRequest;
import com.supplychain.copilot.service.dto.SimulationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulations")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/run")
    public ResponseEntity<SimulationResponse> runSimulation(@Valid @RequestBody SimulationRequest request) {
        return ResponseEntity.ok(simulationService.runSimulation(request));
    }

    @PostMapping("/commit")
    public ResponseEntity<Map<String, Object>> commitSimulation(@Valid @RequestBody CommitSimulationRequest request) {
        simulationService.commitSimulation(
                request.getShipmentId(),
                request.getChosenOption(),
                request.getSimulationId()
        );

        return ResponseEntity.ok(Map.of(
                "status", "COMMITTED",
                "simulationId", request.getSimulationId(),
                "shipmentId", request.getShipmentId(),
                "destinationPort", request.getChosenOption().getDestinationPort()
        ));
    }
}
