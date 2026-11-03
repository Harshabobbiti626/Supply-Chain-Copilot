package com.supplychain.copilot.service;

import com.supplychain.copilot.domain.model.Shipment;
import com.supplychain.copilot.domain.model.ShipmentStatus;
import com.supplychain.copilot.domain.model.ShipmentWaypoint;
import com.supplychain.copilot.repository.ShipmentRepository;
import com.supplychain.copilot.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Lists active shipments, applying 3PL corridor authorization boundaries.
     */
    @Transactional(readOnly = true)
    public List<Shipment> getAuthorizedShipments() {
        Set<UUID> authorizedCorridors = TenantContext.getAuthorizedCorridors();
        List<Shipment> allShipments = shipmentRepository.findAllWithDetails();

        if (authorizedCorridors == null || authorizedCorridors.isEmpty()) {
            return allShipments;
        }

        return allShipments.stream()
                .filter(s -> s.getCorridor() != null && authorizedCorridors.contains(s.getCorridor().getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Shipment> getShipmentById(UUID id) {
        return shipmentRepository.findByIdWithDetails(id)
                .filter(s -> s.getCorridor() == null || TenantContext.isCorridorAuthorized(s.getCorridor().getId()));
    }

    @Transactional
    public Shipment updateTelemetryPosition(String trackingNumber, BigDecimal lat, BigDecimal lon) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + trackingNumber));

        shipment.setCurrentLatitude(lat);
        shipment.setCurrentLongitude(lon);
        log.debug("Updated telemetry for shipment {}: [lat={}, lon={}]", trackingNumber, lat, lon);
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment rerouteShipment(UUID shipmentId, String newDestinationPort, List<ShipmentWaypoint> newWaypoints) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

        if (!TenantContext.isCorridorAuthorized(shipment.getCorridor().getId())) {
            throw new SecurityException("Tenant not authorized to reroute shipment in corridor: " + shipment.getCorridor().getId());
        }

        shipment.setDestinationPort(newDestinationPort);
        shipment.setCurrentStatus(ShipmentStatus.REROUTED);

        if (newWaypoints != null && !newWaypoints.isEmpty()) {
            shipment.getWaypoints().clear();
            for (int i = 0; i < newWaypoints.size(); i++) {
                ShipmentWaypoint wp = newWaypoints.get(i);
                wp.setShipment(shipment);
                wp.setSequenceOrder(i + 1);
                shipment.getWaypoints().add(wp);
            }
        }

        return shipmentRepository.save(shipment);
    }
}

