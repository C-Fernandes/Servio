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
    active: boolean;
    category: string;
    tags: string[];
    availableSlots?: AvailableSlot[];

}