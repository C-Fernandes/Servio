package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class NotificationResponseDTO {

    private Long id;
    private String message;
    private Long orderId;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponseDTO(Long id, String message, Long orderId, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.orderId = orderId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
