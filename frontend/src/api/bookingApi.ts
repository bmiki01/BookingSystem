import apiClient from './client';
import { Booking, BookingStatus, PageResponse } from '../types';

export interface CreateBookingPayload {
  resourceId: number;
  startTime: string;
  endTime: string;
  notes?: string;
}

export const bookingApi = {
  createBooking: async (payload: CreateBookingPayload): Promise<Booking> => {
    const res = await apiClient.post<Booking>('/bookings', payload);
    return res.data;
  },

  getMyBookings: async (page = 0, size = 20): Promise<PageResponse<Booking>> => {
    const res = await apiClient.get<PageResponse<Booking>>('/bookings/my', {
      params: { page, size },
    });
    return res.data;
  },

  getAllBookings: async (
    params?: { resourceId?: number; userId?: number; startFrom?: string; endTo?: string; status?: BookingStatus; page?: number; size?: number }
  ): Promise<PageResponse<Booking>> => {
    const res = await apiClient.get<PageResponse<Booking>>('/bookings', { params });
    return res.data;
  },

  cancelBooking: async (id: number): Promise<Booking> => {
    const res = await apiClient.patch<Booking>(`/bookings/${id}/cancel`);
    return res.data;
  },
};
