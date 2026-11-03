package com.supplychain.copilot.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_stock",
       uniqueConstraints = @UniqueConstraint(name = "uk_warehouse_sku", columnNames = {"warehouse_id", "sku"}))
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public InventoryStock() {}

    public InventoryStock(UUID id, Warehouse warehouse, String sku, Integer totalQuantity, Integer allocatedQuantity, Integer availableQuantity, Integer version, Instant updatedAt) {
        this.id = id;
        this.warehouse = warehouse;
        this.sku = sku;
        this.totalQuantity = totalQuantity;
        this.allocatedQuantity = allocatedQuantity != null ? allocatedQuantity : 0;
        this.availableQuantity = availableQuantity;
        this.version = version != null ? version : 0;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(Integer allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public void allocate(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Allocation quantity must be greater than zero");
        }
        if (this.availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient inventory. Available: " + availableQuantity + ", Requested: " + quantity);
        }
        this.allocatedQuantity += quantity;
        this.availableQuantity -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be greater than zero");
        }
        if (this.allocatedQuantity < quantity) {
            throw new IllegalStateException("Cannot release more than allocated. Allocated: " + allocatedQuantity);
        }
        this.allocatedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    public static InventoryStockBuilder builder() {
        return new InventoryStockBuilder();
    }

    public static class InventoryStockBuilder {
        private UUID id;
        private Warehouse warehouse;
        private String sku;
        private Integer totalQuantity;
        private Integer allocatedQuantity = 0;
        private Integer availableQuantity;
        private Integer version = 0;
        private Instant updatedAt;

        public InventoryStockBuilder id(UUID id) { this.id = id; return this; }
        public InventoryStockBuilder warehouse(Warehouse warehouse) { this.warehouse = warehouse; return this; }
        public InventoryStockBuilder sku(String sku) { this.sku = sku; return this; }
        public InventoryStockBuilder totalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; return this; }
        public InventoryStockBuilder allocatedQuantity(Integer allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; return this; }
        public InventoryStockBuilder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public InventoryStockBuilder version(Integer version) { this.version = version; return this; }
        public InventoryStockBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public InventoryStock build() {
            return new InventoryStock(id, warehouse, sku, totalQuantity, allocatedQuantity, availableQuantity, version, updatedAt);
        }
    }
}
