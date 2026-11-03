package com.supplychain.copilot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychain.copilot.domain.model.InventoryStock;
import com.supplychain.copilot.domain.model.TransactionalOutbox;
import com.supplychain.copilot.repository.InventoryStockRepository;
import com.supplychain.copilot.repository.TransactionalOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class InventoryAllocationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryAllocationService.class);

    private final InventoryStockRepository inventoryStockRepository;
    private final TransactionalOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public InventoryAllocationService(
            InventoryStockRepository inventoryStockRepository,
            TransactionalOutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.inventoryStockRepository = inventoryStockRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes atomic inventory reallocation under a pessimistic write lock (SELECT ... FOR UPDATE).
     * Guarantees zero race conditions during concurrent re-routing reallocation requests.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryStock allocateStock(UUID warehouseId, String sku, int quantity, String simulationId) {
        log.info("Acquiring pessimistic write lock for warehouse: {} sku: {} qty: {}", warehouseId, sku, quantity);

        InventoryStock stock = inventoryStockRepository.findForUpdateByWarehouseIdAndSku(warehouseId, sku)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for warehouse " + warehouseId + " and SKU " + sku));

        // Enforce inventory decrement inside transaction
        stock.allocate(quantity);
        InventoryStock savedStock = inventoryStockRepository.save(stock);

        // Transactional Outbox Pattern: Insert event in the exact same local ACID transaction
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "warehouseId", warehouseId.toString(),
                    "sku", sku,
                    "allocatedQuantity", quantity,
                    "remainingAvailable", savedStock.getAvailableQuantity(),
                    "simulationId", simulationId != null ? simulationId : "MANUAL"
            ));

            TransactionalOutbox outboxEntry = TransactionalOutbox.builder()
                    .aggregateType("INVENTORY")
                    .aggregateId(savedStock.getId().toString())
                    .eventType("InventoryAllocatedEvent")
                    .payload(payload)
                    .status("PENDING")
                    .build();

            outboxRepository.save(outboxEntry);
            log.info("Inventory successfully allocated and outbox event queued for stock ID: {}", savedStock.getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }

        return savedStock;
    }
}

