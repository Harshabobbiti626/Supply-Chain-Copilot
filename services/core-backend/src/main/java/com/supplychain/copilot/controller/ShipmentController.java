package com.supplychain.copilot.controller;

import com.supplychain.copilot.domain.model.Shipment;
import com.supplychain.copilot.service.ShipmentService;
import com.supplychain.copilot.service.dto.TelemetryUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@CrossOrigin(origins = "*")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getShipments() {
        return ResponseEntity.ok(shipmentService.getAuthorizedShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable UUID id) {
        return shipmentService.getShipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{trackingNumber}/telemetry")
    public ResponseEntity<Shipment> updateTelemetry(
            @PathVariable String trackingNumber,
            @Valid @RequestBody TelemetryUpdateRequest coords) {
        return ResponseEntity.ok(shipmentService.updateTelemetryPosition(
                trackingNumber, coords.getLatitude(), coords.getLongitude()
        ));
    }
}
