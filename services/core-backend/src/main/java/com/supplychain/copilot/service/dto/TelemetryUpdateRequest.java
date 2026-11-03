package com.supplychain.copilot.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TelemetryUpdateRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private BigDecimal longitude;

    public TelemetryUpdateRequest() {}

    public TelemetryUpdateRequest(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public static TelemetryUpdateRequestBuilder builder() {
        return new TelemetryUpdateRequestBuilder();
    }

    public static class TelemetryUpdateRequestBuilder {
        private BigDecimal latitude;
        private BigDecimal longitude;

        public TelemetryUpdateRequestBuilder latitude(BigDecimal latitude) {
            this.latitude = latitude;
            return this;
        }

        public TelemetryUpdateRequestBuilder longitude(BigDecimal longitude) {
            this.longitude = longitude;
            return this;
        }

        public TelemetryUpdateRequest build() {
            TelemetryUpdateRequest req = new TelemetryUpdateRequest();
            req.latitude = this.latitude;
            req.longitude = this.longitude;
            return req;
        }
    }
}
