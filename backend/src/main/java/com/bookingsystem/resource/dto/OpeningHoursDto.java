package com.bookingsystem.resource.dto;

import com.bookingsystem.resource.model.OpeningHours;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "Opening hours configuration for a specific weekday")
public record OpeningHoursDto(
    @Schema(description = "Day of week", example = "MONDAY")
    @NotNull(message = "Day of week is required")
    DayOfWeek dayOfWeek,

    @Schema(description = "Opening time in HH:mm", example = "08:00")
    @NotNull(message = "Open time is required")
    LocalTime openTime,

    @Schema(description = "Closing time in HH:mm", example = "18:00")
    @NotNull(message = "Close time is required")
    LocalTime closeTime,

    @Schema(description = "Whether the resource is closed on this day", example = "false")
    boolean isClosed
) {
    public static OpeningHoursDto fromEntity(OpeningHours hours) {
        return new OpeningHoursDto(
            hours.getDayOfWeek(),
            hours.getOpenTime(),
            hours.getCloseTime(),
            hours.isClosed()
        );
    }

    public OpeningHours toEntity() {
        return new OpeningHours(dayOfWeek, openTime, closeTime, isClosed);
    }
}
