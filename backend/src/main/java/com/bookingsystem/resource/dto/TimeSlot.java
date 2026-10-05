package com.bookingsystem.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Represents a time interval slot")
public record TimeSlot(
    @Schema(description = "Slot start time in UTC", example = "2026-10-10T09:00:00Z")
    Instant startTime,

    @Schema(description = "Slot end time in UTC", example = "2026-10-10T09:30:00Z")
    Instant endTime
) {}
