package com.ufrn.ppgti.servio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufrn.ppgti.servio.annotations.Admin;
import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.dto.request.CreateReportRequestDTO;
import com.ufrn.ppgti.servio.dto.request.UpdateReportStatusRequestDTO;
import com.ufrn.ppgti.servio.dto.response.ReportResponseDTO;
import com.ufrn.ppgti.servio.model.enums.ReportStatus;
import com.ufrn.ppgti.servio.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Client
    @PostMapping
    public ResponseEntity<ReportResponseDTO> create(@RequestBody @Valid CreateReportRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(dto));
    }

    @Client
    @GetMapping("/my")
    public ResponseEntity<List<ReportResponseDTO>> listMine() {
        return ResponseEntity.ok(reportService.listMine());
    }

    @Admin
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> listAll(@RequestParam(required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.listAll(status));
    }

    @Admin
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateReportStatusRequestDTO dto) {
        return ResponseEntity.ok(reportService.updateStatus(id, dto.getStatus()));
    }
}
