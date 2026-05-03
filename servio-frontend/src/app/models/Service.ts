export interface Service {
    id: number;
    title: string;
    price: number;
    provider: string;
    durationInMinutes: number;
    description: string;
    image: string;
    rating: number;
    active: boolean;
    category: string;
    tags: string[];

}export interface Tag {
    id: number;
    name: string;
}export interface Category {
    id: number;
    name: string;
}