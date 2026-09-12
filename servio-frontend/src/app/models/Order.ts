
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
    originalPrice: number | null;
    discountPercentage: number | null;
    finalPrice: number | null;
    couponCode: string | null;
}

export interface OrderStatusUpdateRequestDTO {
    status: OrderStatusEnum;
}export interface OrderCreateRequestDTO {
    serviceId: number;
    date: string;
    startTime: string;
    couponCode?: string | null;
}

export type OrderJourneyStepState = 'DONE' | 'CURRENT' | 'PENDING' | 'SKIPPED';

export interface OrderJourneyStepDTO {
    key: string;
    label: string;
    state: OrderJourneyStepState;
    reachedAt: string | null;
}

export interface OrderJourneyResponseDTO {
    orderId: number;
    currentStatus: OrderStatusEnum;
    steps: OrderJourneyStepDTO[];
    cancelledAt: string | null;
}