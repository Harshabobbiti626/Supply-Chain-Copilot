package com.supplychain.copilot.service;

import com.supplychain.copilot.domain.model.DisruptionAdvisory;
import com.supplychain.copilot.domain.model.Shipment;
import com.supplychain.copilot.domain.model.ShipmentStatus;
import com.supplychain.copilot.repository.DisruptionAdvisoryRepository;
import com.supplychain.copilot.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisruptionService {

    private static final Logger log = LoggerFactory.getLogger(DisruptionService.class);

    private final DisruptionAdvisoryRepository advisoryRepository;
    private final ShipmentRepository shipmentRepository;

    public DisruptionService(
            DisruptionAdvisoryRepository advisoryRepository,
            ShipmentRepository shipmentRepository) {
        this.advisoryRepository = advisoryRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DisruptionAdvisory> getLatestAdvisories() {
        return advisoryRepository.findTop10ByOrderByPublishedAtDesc();
    }

    @Transactional
    public DisruptionAdvisory recordAdvisory(DisruptionAdvisory advisory) {
        DisruptionAdvisory saved = advisoryRepository.save(advisory);
        log.warn("Registered disruption event: {} [Port: {}, Severity: {}]",
                saved.getTitle(), saved.getAffectedPortCode(), saved.getSeverityScore());

        // Flag affected shipments to AT_RISK status
        if (saved.getAffectedPortCode() != null && !saved.getAffectedPortCode().isBlank()) {
            List<Shipment> affected = shipmentRepository.findByPort(saved.getAffectedPortCode());
            for (Shipment s : affected) {
                if (s.getCurrentStatus() == ShipmentStatus.IN_TRANSIT) {
                    s.setCurrentStatus(ShipmentStatus.AT_RISK);
                    shipmentRepository.save(s);
                    log.info("Flagged shipment {} as AT_RISK due to advisory at {}", s.getTrackingNumber(), saved.getAffectedPortCode());
                }
            }
        }
        return saved;
    }
}

