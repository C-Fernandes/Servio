package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class CouponResponseDTO {

    private Long id;
    private String code;
    private Double discountPercentage;
    private Long serviceId;
    private String serviceTitle;
    private boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public CouponResponseDTO(Long id, String code, Double discountPercentage, Long serviceId, String serviceTitle,
            boolean active, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.active = active;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
