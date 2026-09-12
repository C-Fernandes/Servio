package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class InteractionEventDTO {

    private String type;
    private LocalDateTime timestamp;
    private String description;
    private Long referenceId;

    public InteractionEventDTO(String type, LocalDateTime timestamp, String description, Long referenceId) {
        this.type = type;
        this.timestamp = timestamp;
        this.description = description;
        this.referenceId = referenceId;
    }

    public String getType() {
        return this.type;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getDescription() {
        return this.description;
    }

    public Long getReferenceId() {
        return this.referenceId;
    }
}
