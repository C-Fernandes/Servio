package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class ReviewResponseDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private Long orderId;

    private Long clientId;
    private String clientName;

    private Long providerId;
    private String providerName;

    private Long serviceId;
    private String serviceTitle;

    public ReviewResponseDTO(
            Long id,
            Integer rating,
            String comment,
            LocalDateTime createdAt,
            Long orderId,
            Long clientId,
            String clientName,
            Long providerId,
            String providerName,
            Long serviceId,
            String serviceTitle) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.orderId = orderId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.providerId = providerId;
        this.providerName = providerName;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
    }

    public Long getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }
}
