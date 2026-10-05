package com.bookingsystem.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Availability details for a resource over a date range")
public record ResourceAvailabilityResponse(
    @Schema(description = "Resource ID", example = "1")
    Long resourceId,

    @Schema(description = "Resource Name", example = "Conference Room A")
    String resourceName,

    @Schema(description = "Query range start UTC", example = "2026-10-10T00:00:00Z")
    Instant start,

    @Schema(description = "Query range end UTC", example = "2026-10-17T23:59:59Z")
    Instant end,

    @Schema(description = "Slot granularity in minutes", example = "30")
    Integer slotGranularityMinutes,

    @Schema(description = "List of booked time slots")
    List<TimeSlot> bookedSlots,

    @Schema(description = "List of available free time slots")
    List<TimeSlot> freeSlots
) {}
