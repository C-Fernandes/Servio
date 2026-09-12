package com.ufrn.ppgti.servio.dto.request;

import com.ufrn.ppgti.servio.model.enums.ReportReason;
import com.ufrn.ppgti.servio.model.enums.ReportTargetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateReportRequestDTO {

    @NotNull(message = "O tipo de denúncia é obrigatório.")
    private ReportTargetType targetType;

    @NotNull(message = "O alvo da denúncia é obrigatório.")
    private Long targetId;

    @NotNull(message = "O motivo é obrigatório.")
    private ReportReason reason;

    @NotBlank(message = "Descreva o motivo da denúncia.")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String description;

    public ReportTargetType getTargetType() {
        return this.targetType;
    }

    public void setTargetType(ReportTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return this.targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public ReportReason getReason() {
        return this.reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
