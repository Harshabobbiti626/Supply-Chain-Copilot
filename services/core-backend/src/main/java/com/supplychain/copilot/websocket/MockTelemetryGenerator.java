package com.supplychain.copilot.websocket;

import com.supplychain.copilot.domain.model.Shipment;
import com.supplychain.copilot.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MockTelemetryGenerator {

    private static final Logger log = LoggerFactory.getLogger(MockTelemetryGenerator.class);

    private final ShipmentRepository shipmentRepository;
    private final TelemetryBroadcastService broadcastService;

    public MockTelemetryGenerator(
            ShipmentRepository shipmentRepository,
            TelemetryBroadcastService broadcastService) {
        this.shipmentRepository = shipmentRepository;
        this.broadcastService = broadcastService;
    }

    /**
     * Periodically advances vessels by tiny geospatial offsets to simulate live AIS telemetry.
     */
    @Scheduled(fixedRate = 4000)
    @org.springframework.transaction.annotation.Transactional
    public void advanceTelemetryPositions() {
        List<Shipment> activeShipments = shipmentRepository.findAllWithDetails();
        for (Shipment s : activeShipments) {
            if (s.getCurrentLatitude() == null || s.getCurrentLongitude() == null) {
                continue;
            }

            // Gently increment latitude and longitude towards northern European waters
            BigDecimal latDelta = BigDecimal.valueOf((Math.random() - 0.3) * 0.008).setScale(6, RoundingMode.HALF_UP);
            BigDecimal lonDelta = BigDecimal.valueOf((Math.random() + 0.1) * 0.012).setScale(6, RoundingMode.HALF_UP);

            BigDecimal newLat = s.getCurrentLatitude().add(latDelta).setScale(6, RoundingMode.HALF_UP);
            BigDecimal newLon = s.getCurrentLongitude().add(lonDelta).setScale(6, RoundingMode.HALF_UP);

            s.setCurrentLatitude(newLat);
            s.setCurrentLongitude(newLon);
            shipmentRepository.save(s);

            // Broadcast live update over WebSocket STOMP
            broadcastService.broadcastShipmentUpdate(s);
        }
    }
}

