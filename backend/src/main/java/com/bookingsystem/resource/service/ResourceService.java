package com.bookingsystem.resource.service;

import com.bookingsystem.booking.model.Booking;
import com.bookingsystem.booking.repository.BookingRepository;
import com.bookingsystem.common.exception.AppException;
import com.bookingsystem.common.exception.ResourceNotFoundException;
import com.bookingsystem.resource.dto.*;
import com.bookingsystem.resource.model.OpeningHours;
import com.bookingsystem.resource.model.Resource;
import com.bookingsystem.resource.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;

    public ResourceService(ResourceRepository resourceRepository, BookingRepository bookingRepository) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> getAllResources(Pageable pageable, boolean includeInactive) {
        Page<Resource> page = includeInactive
            ? resourceRepository.findAll(pageable)
            : resourceRepository.findAllByActiveTrue(pageable);

        return page.map(ResourceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findByIdWithOpeningHours(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
        return ResourceResponse.fromEntity(resource);
    }

    @Transactional(readOnly = true)
    public ResourceAvailabilityResponse getAvailability(Long resourceId, Instant start, Instant end) {
        if (!start.isBefore(end)) {
            throw new AppException("Start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE");
        }

        Resource resource = resourceRepository.findByIdWithOpeningHours(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        List<Booking> activeBookings = bookingRepository.findActiveBookingsInDateRange(resourceId, start, end);

        List<TimeSlot> bookedSlots = activeBookings.stream()
            .map(b -> new TimeSlot(b.getStartTime(), b.getEndTime()))
            .toList();

        List<TimeSlot> freeSlots = new ArrayList<>();
        ZoneId utc = ZoneId.of("UTC");
        LocalDate startDate = start.atZone(utc).toLocalDate();
        LocalDate endDate = end.atZone(utc).toLocalDate();
        int granularity = resource.getSlotGranularityMinutes();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            OpeningHours oh = resource.getOpeningHours().stream()
                .filter(h -> h.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);

            if (oh == null || oh.isClosed()) {
                continue;
            }

            ZonedDateTime openZdt = ZonedDateTime.of(date, oh.getOpenTime(), utc);
            ZonedDateTime closeZdt = ZonedDateTime.of(date, oh.getCloseTime(), utc);

            ZonedDateTime slotStart = openZdt;
            while (slotStart.plusMinutes(granularity).isBefore(closeZdt) || slotStart.plusMinutes(granularity).isEqual(closeZdt)) {
                ZonedDateTime slotEnd = slotStart.plusMinutes(granularity);
                Instant slotStartInst = slotStart.toInstant();
                Instant slotEndInst = slotEnd.toInstant();

                // Slot must fall within requested query window
                if (!slotStartInst.isBefore(start) && !slotEndInst.isAfter(end)) {
                    // Check if slot overlaps any confirmed booking
                    boolean overlaps = bookedSlots.stream().anyMatch(b ->
                        slotStartInst.isBefore(b.endTime()) && slotEndInst.isAfter(b.startTime())
                    );

                    if (!overlaps) {
                        freeSlots.add(new TimeSlot(slotStartInst, slotEndInst));
                    }
                }

                slotStart = slotEnd;
            }
        }

        return new ResourceAvailabilityResponse(
            resource.getId(),
            resource.getName(),
            start,
            end,
            granularity,
            bookedSlots,
            freeSlots
        );
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        validateResourceDurations(request);

        Resource resource = new Resource();
        mapRequestToEntity(request, resource);

        Resource saved = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(saved);
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        validateResourceDurations(request);

        Resource resource = resourceRepository.findByIdWithOpeningHours(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        mapRequestToEntity(request, resource);

        Resource updated = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
        resourceRepository.delete(resource);
    }

    private void validateResourceDurations(ResourceRequest request) {
        if (request.minBookingDurationMinutes() > request.maxBookingDurationMinutes()) {
            throw new AppException(
                "Minimum booking duration cannot exceed maximum booking duration",
                HttpStatus.BAD_REQUEST,
                "INVALID_DURATION_RANGE"
            );
        }

        if (request.minBookingDurationMinutes() % request.slotGranularityMinutes() != 0) {
            throw new AppException(
                "Minimum booking duration must be a multiple of slot granularity",
                HttpStatus.BAD_REQUEST,
                "INVALID_DURATION_GRANULARITY"
            );
        }
    }

    private void mapRequestToEntity(ResourceRequest request, Resource resource) {
        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setCapacity(request.capacity());
        resource.setType(request.type());
        resource.setActive(request.active());
        resource.setSlotGranularityMinutes(request.slotGranularityMinutes());
        resource.setMinBookingDurationMinutes(request.minBookingDurationMinutes());
        resource.setMaxBookingDurationMinutes(request.maxBookingDurationMinutes());

        resource.clearOpeningHours();
        if (request.openingHours() != null && !request.openingHours().isEmpty()) {
            for (OpeningHoursDto dto : request.openingHours()) {
                OpeningHours oh = dto.toEntity();
                resource.addOpeningHours(oh);
            }
        } else {
            // Default 08:00 - 18:00 Monday to Friday default opening hours
            for (DayOfWeek day : DayOfWeek.values()) {
                boolean weekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
                OpeningHours oh = new OpeningHours(day, LocalTime.of(8, 0), LocalTime.of(18, 0), weekend);
                resource.addOpeningHours(oh);
            }
        }
    }
}
