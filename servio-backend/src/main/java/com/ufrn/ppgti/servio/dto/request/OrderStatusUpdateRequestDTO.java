package com.ufrn.ppgti.servio.dto.request;

import com.ufrn.ppgti.servio.model.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateRequestDTO {

    @NotNull(message = "O status é obrigatório")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
