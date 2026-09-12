package com.ufrn.ppgti.servio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ufrn.ppgti.servio.dto.request.CreateReportRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ReportResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Report;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.ReportStatus;
import com.ufrn.ppgti.servio.model.enums.ReportTargetType;
import com.ufrn.ppgti.servio.repository.ReportRepository;
import com.ufrn.ppgti.servio.repository.ServiceRepository;
import com.ufrn.ppgti.servio.repository.UserRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public ReportService(
            ReportRepository reportRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            AuthService authService,
            NotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReportResponseDTO createReport(CreateReportRequestDTO dto) {
        User reporter = authService.getAuthenticadUser();

        Report report = dto.getTargetType() == ReportTargetType.SERVICE
                ? buildServiceReport(dto, reporter)
                : buildUserReport(dto, reporter);

        if (report == null) {
            return toResponseDTO(existingPending(dto, reporter));
        }

        return toResponseDTO(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> listMine() {
        User me = authService.getAuthenticadUser();
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(me.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> listAll(ReportStatus status) {
        List<Report> reports = status != null
                ? reportRepository.findAllByStatusOrderByCreatedAtDesc(status)
                : reportRepository.findAllByOrderByCreatedAtDesc();

        return reports.stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public ReportResponseDTO updateStatus(Long id, ReportStatus newStatus) {
        User admin = authService.getAuthenticadUser();

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Denúncia não encontrada."));

        report.setStatus(newStatus);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(admin);

        Report saved = reportRepository.save(report);

        if (newStatus == ReportStatus.REVIEWED) {
            notifyReportedParty(saved);
        }

        return toResponseDTO(saved);
    }

    private void notifyReportedParty(Report report) {
        User recipient;
        String message;

        if (report.getTargetType() == ReportTargetType.SERVICE) {
            recipient = report.getReportedService().getProvider() != null
                    ? report.getReportedService().getProvider().getUser()
                    : null;
            message = "Seu serviço \"" + report.getReportedService().getTitle()
                    + "\" foi analisado pela nossa equipe de moderação após uma denúncia.";
        } else {
            recipient = report.getReportedUser();
            message = "Sua conta foi analisada pela nossa equipe de moderação após uma denúncia.";
        }

        if (recipient != null) {
            notificationService.notifyGeneric(recipient, message);
        }
    }

    private Report buildServiceReport(CreateReportRequestDTO dto, User reporter) {
        com.ufrn.ppgti.servio.model.Service service = serviceRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado."));

        if (service.getProvider() != null && service.getProvider().getUser() != null
                && service.getProvider().getUser().getId().equals(reporter.getId())) {
            throw new BusinessException("Você não pode denunciar o próprio serviço.");
        }

        boolean alreadyPending = reportRepository
                .findByReporterIdAndReportedServiceIdAndStatus(reporter.getId(), service.getId(), ReportStatus.PENDING)
                .isPresent();

        if (alreadyPending) {
            return null;
        }

        return new Report(reporter, service, dto.getReason(), dto.getDescription().trim());
    }

    private Report buildUserReport(CreateReportRequestDTO dto, User reporter) {
        User reportedUser = userRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (reportedUser.getId().equals(reporter.getId())) {
            throw new BusinessException("Você não pode denunciar a si mesmo.");
        }

        boolean alreadyPending = reportRepository
                .findByReporterIdAndReportedUserIdAndStatus(reporter.getId(), reportedUser.getId(), ReportStatus.PENDING)
                .isPresent();

        if (alreadyPending) {
            return null;
        }

        return new Report(reporter, reportedUser, dto.getReason(), dto.getDescription().trim());
    }

    private Report existingPending(CreateReportRequestDTO dto, User reporter) {
        return dto.getTargetType() == ReportTargetType.SERVICE
                ? reportRepository
                        .findByReporterIdAndReportedServiceIdAndStatus(reporter.getId(), dto.getTargetId(),
                                ReportStatus.PENDING)
                        .orElseThrow()
                : reportRepository
                        .findByReporterIdAndReportedUserIdAndStatus(reporter.getId(), dto.getTargetId(),
                                ReportStatus.PENDING)
                        .orElseThrow();
    }

    private ReportResponseDTO toResponseDTO(Report report) {
        Long targetId;
        String targetLabel;

        if (report.getTargetType() == ReportTargetType.SERVICE) {
            targetId = report.getReportedService().getId();
            targetLabel = report.getReportedService().getTitle();
        } else {
            targetId = report.getReportedUser().getId();
            targetLabel = report.getReportedUser().getName();
        }

        return new ReportResponseDTO(
                report.getId(),
                report.getTargetType(),
                targetId,
                targetLabel,
                report.getReporter().getId(),
                report.getReporter().getName(),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt());
    }
}
