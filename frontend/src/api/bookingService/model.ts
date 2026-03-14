export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface RestaurantTable {
    id: number;
    tableNumber: number;
    capacity: number;
}

export interface BookingRequest {
    customerId: string;
    tableNumber?: number;
    partySize: number;
    bookingTime: string; // ISO string
}

export interface BookingResponse {
    id: number;
    customerId: string;
    tableNumber: number;
    bookingTime: string; // ISO string
    partySize: number;
    status: BookingStatus;
}
