package com.supplychain.copilot.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactional_outbox",
       indexes = @Index(name = "idx_outbox_pending", columnList = "status, created_at"))
public class TransactionalOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    public TransactionalOutbox() {}

    public TransactionalOutbox(UUID id, String aggregateType, String aggregateId, String eventType,
                               String payload, String status, Instant createdAt, Instant processedAt) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public static TransactionalOutboxBuilder builder() {
        return new TransactionalOutboxBuilder();
    }

    public static class TransactionalOutboxBuilder {
        private UUID id;
        private String aggregateType;
        private String aggregateId;
        private String eventType;
        private String payload;
        private String status = "PENDING";
        private Instant createdAt;
        private Instant processedAt;

        public TransactionalOutboxBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TransactionalOutboxBuilder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public TransactionalOutboxBuilder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public TransactionalOutboxBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public TransactionalOutboxBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public TransactionalOutboxBuilder status(String status) {
            this.status = status;
            return this;
        }

        public TransactionalOutboxBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransactionalOutboxBuilder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public TransactionalOutbox build() {
            return new TransactionalOutbox(id, aggregateType, aggregateId, eventType, payload, status, createdAt, processedAt);
        }
    }
}
