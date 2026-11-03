package com.supplychain.copilot.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disruption_advisories")
public class DisruptionAdvisory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // e.g. LABOR_STRIKE, WEATHER_ANOMALY, CANAL_BLOCKAGE, GEOPOLITICAL

    @Column(name = "severity_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal severityScore;

    @Column(name = "affected_port_code", length = 20)
    private String affectedPortCode;

    @CreationTimestamp
    @Column(name = "published_at", updatable = false)
    private Instant publishedAt;

    public DisruptionAdvisory() {}

    public DisruptionAdvisory(UUID id, String title, String content, String sourceUrl, String eventType, BigDecimal severityScore, String affectedPortCode, Instant publishedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.eventType = eventType;
        this.severityScore = severityScore;
        this.affectedPortCode = affectedPortCode;
        this.publishedAt = publishedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public BigDecimal getSeverityScore() { return severityScore; }
    public void setSeverityScore(BigDecimal severityScore) { this.severityScore = severityScore; }

    public String getAffectedPortCode() { return affectedPortCode; }
    public void setAffectedPortCode(String affectedPortCode) { this.affectedPortCode = affectedPortCode; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public static DisruptionAdvisoryBuilder builder() {
        return new DisruptionAdvisoryBuilder();
    }

    public static class DisruptionAdvisoryBuilder {
        private UUID id;
        private String title;
        private String content;
        private String sourceUrl;
        private String eventType;
        private BigDecimal severityScore;
        private String affectedPortCode;
        private Instant publishedAt;

        public DisruptionAdvisoryBuilder id(UUID id) { this.id = id; return this; }
        public DisruptionAdvisoryBuilder title(String title) { this.title = title; return this; }
        public DisruptionAdvisoryBuilder content(String content) { this.content = content; return this; }
        public DisruptionAdvisoryBuilder sourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; return this; }
        public DisruptionAdvisoryBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public DisruptionAdvisoryBuilder severityScore(BigDecimal severityScore) { this.severityScore = severityScore; return this; }
        public DisruptionAdvisoryBuilder affectedPortCode(String affectedPortCode) { this.affectedPortCode = affectedPortCode; return this; }
        public DisruptionAdvisoryBuilder publishedAt(Instant publishedAt) { this.publishedAt = publishedAt; return this; }

        public DisruptionAdvisory build() {
            return new DisruptionAdvisory(id, title, content, sourceUrl, eventType, severityScore, affectedPortCode, publishedAt);
        }
    }
}
