export interface FavoriteResponseDTO {
    favoriteId: number;
    favoritedAt: string;

    serviceId: number;
    title: string;
    description: string;
    price: number;
    durationInMinutes: number;
    active: boolean;

    category: string | null;
    providerId: number | null;
    provider: string | null;
    image: string | null;

    averageRating: number;
    reviewCount: number;
}
