package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

public class OrderJourneyStepDTO {

    private String key;
    private String label;
    private String state;
    private LocalDateTime reachedAt;

    public OrderJourneyStepDTO(String key, String label, String state, LocalDateTime reachedAt) {
        this.key = key;
        this.label = label;
        this.state = state;
        this.reachedAt = reachedAt;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getState() {
        return state;
    }

    public LocalDateTime getReachedAt() {
        return reachedAt;
    }
}
