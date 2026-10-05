export type Role = 'USER' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}

export interface OpeningHours {
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  openTime: string; // "08:00"
  closeTime: string; // "18:00"
  isClosed: boolean;
}

export interface Resource {
  id: number;
  name: string;
  description: string | null;
  capacity: number;
  type: string;
  active: boolean;
  slotGranularityMinutes: number;
  minBookingDurationMinutes: number;
  maxBookingDurationMinutes: number;
  version: number;
  openingHours: OpeningHours[];
  createdAt: string;
  updatedAt: string;
}

export interface ResourceRequest {
  name: string;
  description: string;
  capacity: number;
  type: string;
  active: boolean;
  slotGranularityMinutes: number;
  minBookingDurationMinutes: number;
  maxBookingDurationMinutes: number;
  openingHours: OpeningHours[];
}

export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface Booking {
  id: number;
  resourceId: number;
  resourceName: string;
  userId: number;
  userFullName: string;
  userEmail: string;
  startTime: string;
  endTime: string;
  status: BookingStatus;
  notes: string | null;
  createdAt: string;
}

export interface TimeSlot {
  startTime: string;
  endTime: string;
}

export interface ResourceAvailability {
  resourceId: number;
  resourceName: string;
  start: string;
  end: string;
  slotGranularityMinutes: number;
  bookedSlots: TimeSlot[];
  freeSlots: TimeSlot[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  errorCode: string;
  message: string;
  fieldErrors?: FieldError[];
  path: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
