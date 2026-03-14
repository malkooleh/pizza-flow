import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import type { BookingRequest, BookingResponse, RestaurantTable } from './model';

export const createBooking = async (data: BookingRequest): Promise<BookingResponse> => {
    const response = await axios.post('/api/v1/bookings', data);
    return response.data;
};

export const getCustomerBookings = async (customerId: string): Promise<BookingResponse[]> => {
    const response = await axios.get(`/api/v1/bookings/customer/${customerId}`);
    return response.data;
};

export const getAllTables = async (): Promise<RestaurantTable[]> => {
    const response = await axios.get('/api/v1/bookings/tables');
    return response.data;
};

export const useCreateBooking = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: createBooking,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['bookings'] });
        },
    });
};

export const useGetCustomerBookings = (customerId: string) => {
    return useQuery({
        queryKey: ['bookings', customerId],
        queryFn: () => getCustomerBookings(customerId),
        enabled: !!customerId,
    });
};

export const useGetAllTables = () => {
    return useQuery({
        queryKey: ['tables'],
        queryFn: getAllTables,
    });
};
