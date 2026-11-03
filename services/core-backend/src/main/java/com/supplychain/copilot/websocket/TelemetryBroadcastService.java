package com.supplychain.copilot.websocket;

import com.supplychain.copilot.domain.model.DisruptionAdvisory;
import com.supplychain.copilot.domain.model.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TelemetryBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryBroadcastService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastShipmentUpdate(Shipment shipment) {
        String destination = "/topic/telemetry";
        messagingTemplate.convertAndSend(destination, shipment);
        
        // Also broadcast to corridor-specific channel for tenant isolation
        if (shipment.getCorridor() != null) {
            String corridorDest = "/topic/corridor/" + shipment.getCorridor().getId() + "/telemetry";
            messagingTemplate.convertAndSend(corridorDest, shipment);
        }
    }

    public void broadcastDisruptionAlert(DisruptionAdvisory advisory) {
        log.info("Broadcasting disruption alert: {}", advisory.getTitle());
        messagingTemplate.convertAndSend("/topic/disruptions", advisory);
    }
}

