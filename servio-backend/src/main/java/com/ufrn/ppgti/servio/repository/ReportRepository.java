package com.ufrn.ppgti.servio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ufrn.ppgti.servio.model.Report;
import com.ufrn.ppgti.servio.model.enums.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByReporterIdAndReportedServiceIdAndStatus(Long reporterId, Long serviceId,
            ReportStatus status);

    Optional<Report> findByReporterIdAndReportedUserIdAndStatus(Long reporterId, Long userId, ReportStatus status);

    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);
}
