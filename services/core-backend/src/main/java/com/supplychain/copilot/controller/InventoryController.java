package com.supplychain.copilot.controller;

import com.supplychain.copilot.domain.model.InventoryStock;
import com.supplychain.copilot.repository.InventoryStockRepository;
import com.supplychain.copilot.service.InventoryAllocationService;
import com.supplychain.copilot.service.dto.AllocateStockRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryStockRepository stockRepository;
    private final InventoryAllocationService allocationService;

    public InventoryController(
            InventoryStockRepository stockRepository,
            InventoryAllocationService allocationService) {
        this.stockRepository = stockRepository;
        this.allocationService = allocationService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryStock>> getAllInventory() {
        return ResponseEntity.ok(stockRepository.findAll());
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryStock>> getWarehouseInventory(@PathVariable UUID warehouseId) {
        return ResponseEntity.ok(stockRepository.findByWarehouseId(warehouseId));
    }

    @PostMapping("/allocate")
    public ResponseEntity<InventoryStock> allocateStock(@Valid @RequestBody AllocateStockRequest request) {
        InventoryStock updated = allocationService.allocateStock(
                request.getWarehouseId(),
                request.getSku(),
                request.getQuantity(),
                request.getSimulationId()
        );
        return ResponseEntity.ok(updated);
    }
}
