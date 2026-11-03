package com.supplychain.copilot.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AllocateStockRequest {

    @NotNull(message = "Warehouse ID is required")
    private UUID warehouseId;

    @NotBlank(message = "SKU code is required")
    private String sku;

    @NotNull(message = "Allocation quantity is required")
    @Min(value = 1, message = "Allocation quantity must be at least 1")
    private Integer quantity;

    private String simulationId;

    public AllocateStockRequest() {}

    public AllocateStockRequest(UUID warehouseId, String sku, Integer quantity, String simulationId) {
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.quantity = quantity;
        this.simulationId = simulationId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(UUID warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(String simulationId) {
        this.simulationId = simulationId;
    }

    public static AllocateStockRequestBuilder builder() {
        return new AllocateStockRequestBuilder();
    }

    public static class AllocateStockRequestBuilder {
        private UUID warehouseId;
        private String sku;
        private Integer quantity;
        private String simulationId;

        public AllocateStockRequestBuilder warehouseId(UUID warehouseId) {
            this.warehouseId = warehouseId;
            return this;
        }

        public AllocateStockRequestBuilder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public AllocateStockRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public AllocateStockRequestBuilder simulationId(String simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public AllocateStockRequest build() {
            AllocateStockRequest req = new AllocateStockRequest();
            req.warehouseId = this.warehouseId;
            req.sku = this.sku;
            req.quantity = this.quantity;
            req.simulationId = this.simulationId;
            return req;
        }
    }
}
