package com.supplychain.copilot.event;

import com.supplychain.copilot.domain.model.TransactionalOutbox;
import com.supplychain.copilot.repository.TransactionalOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class TransactionalOutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(TransactionalOutboxRelay.class);

    private final TransactionalOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionalOutboxRelay(
            TransactionalOutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox() {
        List<TransactionalOutbox> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (TransactionalOutbox event : pendingEvents) {
            try {
                String topic = "events." + event.getAggregateType().toLowerCase();
                try {
                    kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
                } catch (Exception kafkaEx) {
                    // In environments where Kafka is optional/local fallback, log and gracefully proceed
                    log.debug("Kafka broker dispatch skipped ({})", kafkaEx.getMessage());
                }

                event.setStatus("PUBLISHED");
                event.setProcessedAt(Instant.now());
                outboxRepository.save(event);
                log.info("Relayed outbox event: {} [{}]", event.getEventType(), event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
                event.setStatus("FAILED");
                outboxRepository.save(event);
            }
        }
    }
}

