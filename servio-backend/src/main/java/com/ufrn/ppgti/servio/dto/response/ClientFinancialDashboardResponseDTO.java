package com.ufrn.ppgti.servio.dto.response;

import java.math.BigDecimal;

public class ClientFinancialDashboardResponseDTO {
    private BigDecimal totalSpent;
    private Long completedOrders;
    private Long activeOrders;

    public ClientFinancialDashboardResponseDTO(BigDecimal totalSpent, Long completedOrders, Long activeOrders) {
        this.totalSpent = totalSpent;
        this.completedOrders = completedOrders;
        this.activeOrders = activeOrders;
    }

    // Getters e Setters
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Long getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(Long activeOrders) {
        this.activeOrders = activeOrders;
    }
}