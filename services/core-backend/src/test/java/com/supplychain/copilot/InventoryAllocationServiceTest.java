package com.supplychain.copilot;

import com.supplychain.copilot.domain.model.InventoryStock;
import com.supplychain.copilot.domain.model.Warehouse;
import com.supplychain.copilot.repository.InventoryStockRepository;
import com.supplychain.copilot.repository.TransactionalOutboxRepository;
import com.supplychain.copilot.repository.WarehouseRepository;
import com.supplychain.copilot.service.InventoryAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class InventoryAllocationServiceTest {

    @Autowired
    private InventoryAllocationService allocationService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryStockRepository stockRepository;

    @Autowired
    private TransactionalOutboxRepository outboxRepository;

    private Warehouse testWarehouse;

    @BeforeEach
    void setup() {
        testWarehouse = warehouseRepository.save(Warehouse.builder()
                .name("Test Marine Terminal")
                .code("TEST-" + UUID.randomUUID().toString().substring(0, 6))
                .city("Rotterdam")
                .country("Netherlands")
                .maxCapacityTeu(1000)
                .currentOccupancyTeu(500)
                .build());

        stockRepository.save(InventoryStock.builder()
                .warehouse(testWarehouse)
                .sku("TEST-SKU-100")
                .totalQuantity(100)
                .allocatedQuantity(0)
                .availableQuantity(100)
                .build());
    }

    @Test
    @DisplayName("Should successfully allocate inventory under pessimistic lock and queue outbox event")
    void testSuccessfulAllocation() {
        InventoryStock allocated = allocationService.allocateStock(
                testWarehouse.getId(), "TEST-SKU-100", 30, "SIM-TEST-001");

        assertNotNull(allocated);
        assertEquals(30, allocated.getAllocatedQuantity());
        assertEquals(70, allocated.getAvailableQuantity());

        // Verify outbox entry
        assertFalse(outboxRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Should reject allocation when requested quantity exceeds available stock")
    void testOverAllocationRejection() {
        assertThrows(IllegalStateException.class, () -> {
            allocationService.allocateStock(
                    testWarehouse.getId(), "TEST-SKU-100", 150, "SIM-TEST-FAIL");
        });
    }
}

