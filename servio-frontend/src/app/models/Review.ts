export interface ReviewResponseDTO {
    id: number;
    rating: number;
    comment: string | null;
    createdAt: string;

    orderId: number;

    clientId: number;
    clientName: string;

    providerId: number;
    providerName: string;

    serviceId: number;
    serviceTitle: string;
}

export interface ReviewCreateRequestDTO {
    orderId: number;
    rating: number;
    comment?: string;
}

export interface ReviewUpdateRequestDTO {
    rating: number;
    comment?: string;
}