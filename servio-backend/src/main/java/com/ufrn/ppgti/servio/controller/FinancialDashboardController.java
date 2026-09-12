package com.ufrn.ppgti.servio.controller;

import java.util.List;

import com.ufrn.ppgti.servio.annotations.Client;
import com.ufrn.ppgti.servio.annotations.Provider;
import com.ufrn.ppgti.servio.dto.response.ClientFinancialDashboardResponseDTO;
import com.ufrn.ppgti.servio.dto.response.ProviderFinancialDashboardResponseDTO;
import com.ufrn.ppgti.servio.dto.response.TopServiceResponseDTO;
import com.ufrn.ppgti.servio.service.FinancialDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financial-dashboard")
public class FinancialDashboardController {

    private final FinancialDashboardService financialDashboardService;

    public FinancialDashboardController(FinancialDashboardService financialDashboardService) {
        this.financialDashboardService = financialDashboardService;
    }

    @Provider
    @GetMapping("/provider")
    public ResponseEntity<ProviderFinancialDashboardResponseDTO> getProviderDashboard() {
        return ResponseEntity.ok(financialDashboardService.getProviderDashboard());
    }

    @Provider
    @GetMapping("/provider/top-services")
    public ResponseEntity<List<TopServiceResponseDTO>> getTopServices() {
        return ResponseEntity.ok(financialDashboardService.getTopServices());
    }

    @Client
    @GetMapping("/client")
    public ResponseEntity<ClientFinancialDashboardResponseDTO> getClientDashboard() {
        return ResponseEntity.ok(financialDashboardService.getClientDashboard());
    }
}