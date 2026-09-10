export interface NotificationResponseDTO {
    id: number;
    message: string;
    orderId: number | null;
    read: boolean;
    createdAt: string;
}
