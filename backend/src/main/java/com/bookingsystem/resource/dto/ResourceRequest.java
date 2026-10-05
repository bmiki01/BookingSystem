package com.bookingsystem.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request payload for creating or updating a Resource")
public record ResourceRequest(
    @Schema(description = "Name of the resource", example = "Conference Room A")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    String name,

    @Schema(description = "Detailed description", example = "Spacious meeting room equipped with 4K projector and whiteboard.")
    String description,

    @Schema(description = "Resource capacity", example = "10")
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    Integer capacity,

    @Schema(description = "Resource type", example = "MEETING_ROOM")
    @NotBlank(message = "Type is required")
    String type,

    @Schema(description = "Whether the resource is active for booking", example = "true")
    boolean active,

    @Schema(description = "Slot granularity in minutes (e.g. 15, 30, 60)", example = "30")
    @NotNull(message = "Slot granularity is required")
    @Min(value = 5, message = "Slot granularity must be at least 5 minutes")
    Integer slotGranularityMinutes,

    @Schema(description = "Minimum booking duration in minutes", example = "30")
    @NotNull(message = "Minimum booking duration is required")
    @Min(value = 5, message = "Minimum duration must be at least 5 minutes")
    Integer minBookingDurationMinutes,

    @Schema(description = "Maximum booking duration in minutes", example = "480")
    @NotNull(message = "Maximum booking duration is required")
    @Min(value = 15, message = "Maximum duration must be at least 15 minutes")
    Integer maxBookingDurationMinutes,

    @Schema(description = "Weekly opening hours list")
    @Valid
    List<OpeningHoursDto> openingHours
) {}
