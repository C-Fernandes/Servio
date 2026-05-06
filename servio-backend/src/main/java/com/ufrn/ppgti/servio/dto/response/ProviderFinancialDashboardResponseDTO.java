package com.ufrn.ppgti.servio.dto.response;

import java.math.BigDecimal;

public class ProviderFinancialDashboardResponseDTO {

    private BigDecimal totalEarnings;
    private Long totalCompletedOrders;
    private BigDecimal averageTicket;

    public ProviderFinancialDashboardResponseDTO() {
    }

    public ProviderFinancialDashboardResponseDTO(
            BigDecimal totalEarnings,
            Long totalCompletedOrders,
            BigDecimal averageTicket
    ) {
        this.totalEarnings = totalEarnings;
        this.totalCompletedOrders = totalCompletedOrders;
        this.averageTicket = averageTicket;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public Long getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public void setTotalCompletedOrders(Long totalCompletedOrders) {
        this.totalCompletedOrders = totalCompletedOrders;
    }

    public BigDecimal getAverageTicket() {
        return averageTicket;
    }

    public void setAverageTicket(BigDecimal averageTicket) {
        this.averageTicket = averageTicket;
    }
}