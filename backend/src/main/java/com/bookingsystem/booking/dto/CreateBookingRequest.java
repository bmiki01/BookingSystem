package com.bookingsystem.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "Payload for creating a new booking reservation")
public record CreateBookingRequest(
    @Schema(description = "ID of the target resource", example = "1")
    @NotNull(message = "Resource ID is required")
    Long resourceId,

    @Schema(description = "Booking start timestamp in UTC (ISO 8601 format)", example = "2026-10-10T10:00:00Z")
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    Instant startTime,

    @Schema(description = "Booking end timestamp in UTC (ISO 8601 format)", example = "2026-10-10T11:00:00Z")
    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    Instant endTime,

    @Schema(description = "Optional notes or purpose of reservation", example = "Team Sync Meeting")
    String notes
) {}
