package com.bookingsystem.booking.dto;

import com.bookingsystem.booking.model.Booking;
import com.bookingsystem.booking.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Details of a booking reservation")
public record BookingResponse(
    Long id,
    Long resourceId,
    String resourceName,
    Long userId,
    String userFullName,
    String userEmail,
    Instant startTime,
    Instant endTime,
    BookingStatus status,
    String notes,
    Instant createdAt
) {
    public static BookingResponse fromEntity(Booking booking) {
        return new BookingResponse(
            booking.getId(),
            booking.getResource().getId(),
            booking.getResource().getName(),
            booking.getUser().getId(),
            booking.getUser().getFullName(),
            booking.getUser().getEmail(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getStatus(),
            booking.getNotes(),
            booking.getCreatedAt()
        );
    }
}
