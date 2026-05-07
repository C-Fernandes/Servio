package com.ufrn.ppgti.servio.service;

import com.ufrn.ppgti.servio.dto.response.ClientFinancialDashboardResponseDTO;
import com.ufrn.ppgti.servio.dto.response.ProviderFinancialDashboardResponseDTO;
import com.ufrn.ppgti.servio.exceptions.BusinessException;
import com.ufrn.ppgti.servio.model.Order;
import com.ufrn.ppgti.servio.model.User;
import com.ufrn.ppgti.servio.model.enums.OrderStatus;
import com.ufrn.ppgti.servio.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
                    RoundingMode.HALF_UP);
        }

        return new ProviderFinancialDashboardResponseDTO(
                totalEarnings,
                totalCompletedOrders,
                averageTicket);
    }

    @Transactional(readOnly = true)
    public ClientFinancialDashboardResponseDTO getClientDashboard() {
        User user = authService.getAuthenticadUser();
        Long clientId = user.getId();

        List<Order> clientOrders = orderRepository.findByClient_IdOrderByCreatedAtDesc(clientId);

        long activeOrders = 0;
        long completedOrders = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Order order : clientOrders) {
            if (order.getStatus() == OrderStatus.PENDING ||
                    order.getStatus() == OrderStatus.CONFIRMED ||
                    order.getStatus() == OrderStatus.IN_PROGRESS) {
                activeOrders++;
            } else if (order.getStatus() == OrderStatus.COMPLETED) {
                completedOrders++;
                totalSpent = totalSpent.add(BigDecimal.valueOf(order.getService().getPrice()));
            }
        }

        return new ClientFinancialDashboardResponseDTO(totalSpent, completedOrders, activeOrders);
    }
}