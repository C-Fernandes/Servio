package com.ufrn.ppgti.servio.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public class OrderCreateRequestDTO {

    @NotNull(message = "O serviço é obrigatório")
    private Long serviceId;

    @NotNull(message = "A data é obrigatória")
    @FutureOrPresent(message = "A data do pedido deve ser hoje ou futura")
    private LocalDate date;

    @NotNull(message = "O horário de início é obrigatório")
    private LocalTime startTime;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
