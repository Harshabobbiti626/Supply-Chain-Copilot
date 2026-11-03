package com.supplychain.copilot.repository;

import com.supplychain.copilot.domain.model.DisruptionAdvisory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisruptionAdvisoryRepository extends JpaRepository<DisruptionAdvisory, UUID> {
    List<DisruptionAdvisory> findByAffectedPortCode(String portCode);
    List<DisruptionAdvisory> findTop10ByOrderByPublishedAtDesc();
}

