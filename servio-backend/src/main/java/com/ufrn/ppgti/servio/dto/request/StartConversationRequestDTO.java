package com.ufrn.ppgti.servio.dto.request;

import jakarta.validation.constraints.NotNull;

public class StartConversationRequestDTO {

    @NotNull(message = "O serviço é obrigatório.")
    private Long serviceId;

    public Long getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}
