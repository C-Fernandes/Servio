package com.ufrn.ppgti.servio.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CouponCreateRequestDTO {

    @NotNull(message = "O serviço é obrigatório")
    private Long serviceId;

    @NotBlank(message = "O código do cupom é obrigatório")
    private String code;

    @NotNull(message = "O percentual de desconto é obrigatório")
    @DecimalMin(value = "1", message = "O desconto deve ser de no mínimo 1%")
    @DecimalMax(value = "100", message = "O desconto não pode ultrapassar 100%")
    private Double discountPercentage;

    private LocalDateTime expiresAt;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
