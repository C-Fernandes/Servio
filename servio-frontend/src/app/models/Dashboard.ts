export interface ProviderFinancialDashboardResponseDTO {
    totalEarnings: number;
    totalCompletedOrders: number;
    averageTicket: number;
}export interface ClientFinancialDashboardResponseDTO {
    totalSpent: number;
    completedOrders: number;
    activeOrders: number; cancelledOrders: number;
}