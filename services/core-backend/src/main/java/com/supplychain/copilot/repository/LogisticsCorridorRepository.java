package com.supplychain.copilot.repository;

import com.supplychain.copilot.domain.model.LogisticsCorridor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogisticsCorridorRepository extends JpaRepository<LogisticsCorridor, UUID> {
    List<LogisticsCorridor> findByTenantId(UUID tenantId);
    List<LogisticsCorridor> findByActiveTrue();
}

