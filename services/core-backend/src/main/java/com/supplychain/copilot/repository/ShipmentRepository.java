package com.supplychain.copilot.repository;

import com.supplychain.copilot.domain.model.Shipment;
import com.supplychain.copilot.domain.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByCorridorId(UUID corridorId);

    List<Shipment> findByCorridorTenantId(UUID tenantId);

    List<Shipment> findByCurrentStatus(ShipmentStatus status);

    @Query("SELECT DISTINCT s FROM Shipment s LEFT JOIN FETCH s.corridor LEFT JOIN FETCH s.waypoints")
    List<Shipment> findAllWithDetails();

    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.corridor LEFT JOIN FETCH s.waypoints WHERE s.id = :id")
    Optional<Shipment> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT s FROM Shipment s WHERE s.destinationPort = :portCode OR s.originPort = :portCode")
    List<Shipment> findByPort(@Param("portCode") String portCode);
}
