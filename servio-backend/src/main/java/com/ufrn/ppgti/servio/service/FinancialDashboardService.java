package com.ufrn.ppgti.servio.service;

import com.ufrn.ppgti.servio.dto.response.ProviderFinancialDashboardResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FinancialDashboardService {

    private final OrderRepository orderRepository;
    private final AuthService authService;

    public FinancialDashboardService(OrderRepository orderRepository, AuthService authService) {
        this.orderRepository = orderRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public ProviderFinancialDashboardResponseDTO getProviderDashboard() {
        User user = authService.getAuthenticadUser();

        if (user.getProviderProfile() == null) {
            throw new BusinessException("Apenas prestadores podem visualizar o dashboard financeiro.");
        }

        Long providerId = user.getProviderProfile().getId();

        BigDecimal totalEarnings = orderRepository.sumCompletedEarningsByProviderId(providerId);
        Long totalCompletedOrders = orderRepository.countCompletedOrdersByProviderId(providerId);

        BigDecimal averageTicket = BigDecimal.ZERO;
        if (totalCompletedOrders != null && totalCompletedOrders > 0) {
            averageTicket = totalEarnings.divide(
                    BigDecimal.valueOf(totalCompletedOrders),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return new ProviderFinancialDashboardResponseDTO(
                totalEarnings,
                totalCompletedOrders,
                averageTicket
        );
    }
}