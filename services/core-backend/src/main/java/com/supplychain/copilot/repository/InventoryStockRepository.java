package com.supplychain.copilot.repository;

import com.supplychain.copilot.domain.model.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, UUID> {

    @EntityGraph(attributePaths = {"warehouse"})
    List<InventoryStock> findAll();

    @EntityGraph(attributePaths = {"warehouse"})
    List<InventoryStock> findByWarehouseId(UUID warehouseId);

    Optional<InventoryStock> findByWarehouseIdAndSku(UUID warehouseId, String sku);

    /**
     * Acquires a pessimistic write lock (SELECT ... FOR UPDATE) to ensure zero race conditions
     * during concurrent inventory reallocations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InventoryStock s WHERE s.warehouse.id = :warehouseId AND s.sku = :sku")
    Optional<InventoryStock> findForUpdateByWarehouseIdAndSku(@Param("warehouseId") UUID warehouseId, @Param("sku") String sku);
}

