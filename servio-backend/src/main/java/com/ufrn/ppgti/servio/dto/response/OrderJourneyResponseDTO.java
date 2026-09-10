package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.ufrn.ppgti.servio.model.enums.OrderStatus;

public class OrderJourneyResponseDTO {

    private Long orderId;
    private OrderStatus currentStatus;
    private List<OrderJourneyStepDTO> steps;
    private LocalDateTime cancelledAt;

    public OrderJourneyResponseDTO(
            Long orderId,
            OrderStatus currentStatus,
            List<OrderJourneyStepDTO> steps,
            LocalDateTime cancelledAt) {
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.steps = steps;
        this.cancelledAt = cancelledAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public List<OrderJourneyStepDTO> getSteps() {
        return steps;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}
