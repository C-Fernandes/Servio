export interface CouponResponseDTO {
    id: number;
    code: string;
    discountPercentage: number;
    serviceId: number;
    serviceTitle: string;
    active: boolean;
    expiresAt: string | null;
    createdAt: string;
}

export interface CouponCreateRequestDTO {
    serviceId: number;
    code: string;
    discountPercentage: number;
    expiresAt?: string | null;
}

export interface CouponValidationResponseDTO {
    code: string;
    discountPercentage: number;
    originalPrice: number;
    finalPrice: number;
}
