package com.bookingsystem.resource.dto;

import com.bookingsystem.resource.model.Resource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Response details for a Resource")
public record ResourceResponse(
    Long id,
    String name,
    String description,
    Integer capacity,
    String type,
    boolean active,
    Integer slotGranularityMinutes,
    Integer minBookingDurationMinutes,
    Integer maxBookingDurationMinutes,
    Long version,
    List<OpeningHoursDto> openingHours,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResourceResponse fromEntity(Resource resource) {
        List<OpeningHoursDto> hours = resource.getOpeningHours() != null
            ? resource.getOpeningHours().stream().map(OpeningHoursDto::fromEntity).toList()
            : List.of();

        return new ResourceResponse(
            resource.getId(),
            resource.getName(),
            resource.getDescription(),
            resource.getCapacity(),
            resource.getType(),
            resource.isActive(),
            resource.getSlotGranularityMinutes(),
            resource.getMinBookingDurationMinutes(),
            resource.getMaxBookingDurationMinutes(),
            resource.getVersion(),
            hours,
            resource.getCreatedAt(),
            resource.getUpdatedAt()
        );
    }
}
