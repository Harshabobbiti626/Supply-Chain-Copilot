package com.supplychain.copilot.repository;

import com.supplychain.copilot.domain.model.Port;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortRepository extends JpaRepository<Port, UUID> {
    Optional<Port> findByCode(String code);
    Optional<Port> findByCodeIgnoreCase(String code);
    List<Port> findByActiveTrue();
}

