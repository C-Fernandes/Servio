package com.ufrn.ppgti.servio.dto.response;

import java.time.LocalDateTime;

import com.ufrn.ppgti.servio.model.enums.ReportReason;
import com.ufrn.ppgti.servio.model.enums.ReportStatus;
import com.ufrn.ppgti.servio.model.enums.ReportTargetType;

public class ReportResponseDTO {

    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private String targetLabel;
    private Long reporterId;
    private String reporterName;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public ReportResponseDTO(
            Long id,
            ReportTargetType targetType,
            Long targetId,
            String targetLabel,
            Long reporterId,
            String reporterName,
            ReportReason reason,
            String description,
            ReportStatus status,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetLabel = targetLabel;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return this.id;
    }

    public ReportTargetType getTargetType() {
        return this.targetType;
    }

    public Long getTargetId() {
        return this.targetId;
    }

    public String getTargetLabel() {
        return this.targetLabel;
    }

    public Long getReporterId() {
        return this.reporterId;
    }

    public String getReporterName() {
        return this.reporterName;
    }

    public ReportReason getReason() {
        return this.reason;
    }

    public String getDescription() {
        return this.description;
    }

    public ReportStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return this.reviewedAt;
    }
}
