package com.supplychain.copilot.repository;

import com.supplychain.copilot.domain.model.TransactionalOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionalOutboxRepository extends JpaRepository<TransactionalOutbox, UUID> {
    List<TransactionalOutbox> findTop50ByStatusOrderByCreatedAtAsc(String status);
}

