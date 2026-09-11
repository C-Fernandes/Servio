package com.ufrn.ppgti.servio.dto.request;

import com.ufrn.ppgti.servio.model.enums.ReportStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateReportStatusRequestDTO {

    @NotNull(message = "O status é obrigatório.")
    private ReportStatus status;

    public ReportStatus getStatus() {
        return this.status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}
