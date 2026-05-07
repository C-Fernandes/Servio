
export type OrderStatusEnum = 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export interface OrderResponseDTO {
    id: number;
    status: OrderStatusEnum;
    createdAt: string;
    date: string;
    startTime: string;
    endTime: string;
    clientId: number;
    clientName: string;
    providerId: number;
    providerName: string;
    serviceId: number;
    serviceTitle: string;
    servicePrice: number;
}

export interface OrderStatusUpdateRequestDTO {
    status: OrderStatusEnum;
}export interface OrderCreateRequestDTO {
    serviceId: number;
    date: string;
    startTime: string;
}