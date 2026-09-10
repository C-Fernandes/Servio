import { AvailableSlot } from "./Availability";

export interface Service {
    id: number;
    title: string;
    price: number;
    provider: string;
    durationInMinutes: number;
    description: string;
    image: string; averageRating?: number;
    reviewCount?: number;
    favorite?: boolean;
    active: boolean;
    category: string;
    categoryId?: number;
    city?: string;
    state?: string;
    tags: string[];
    availableSlots?: AvailableSlot[];

}

export type ServiceSortOption =
    | 'recent'
    | 'price_asc'
    | 'price_desc'
    | 'rating_desc'
    | 'title_asc';

export interface ServiceSearchFilters {
    term?: string | null;
    categoryId?: number | null;
    minPrice?: number | null;
    maxPrice?: number | null;
    minRating?: number | null;
    city?: string | null;
    state?: string | null;
    sortBy?: ServiceSortOption;
}
