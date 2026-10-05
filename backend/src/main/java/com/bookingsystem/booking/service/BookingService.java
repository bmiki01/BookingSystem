package com.bookingsystem.booking.service;

import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.model.User;
import com.bookingsystem.auth.repository.UserRepository;
import com.bookingsystem.booking.dto.BookingResponse;
import com.bookingsystem.booking.dto.CreateBookingRequest;
import com.bookingsystem.booking.model.Booking;
import com.bookingsystem.booking.model.BookingStatus;
import com.bookingsystem.booking.repository.BookingRepository;
import com.bookingsystem.common.exception.AppException;
import com.bookingsystem.common.exception.ResourceNotFoundException;
import com.bookingsystem.resource.model.OpeningHours;
import com.bookingsystem.resource.model.Resource;
import com.bookingsystem.resource.repository.ResourceRepository;
import com.bookingsystem.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public BookingService(
        BookingRepository bookingRepository,
        ResourceRepository resourceRepository,
        UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Resource resource = resourceRepository.findByIdForUpdate(request.resourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.resourceId()));

        if (!resource.isActive()) {
            throw new AppException("Cannot book an inactive resource", HttpStatus.BAD_REQUEST, "RESOURCE_INACTIVE");
        }

        validateBookingTimeAndRules(request, resource);

        // Layer 1 Check: Application-level overlap verification inside current transaction
        boolean isOverlapping = bookingRepository.existsOverlappingBooking(
            request.resourceId(), request.startTime(), request.endTime()
        );

        if (isOverlapping) {
            throw new AppException(
                "The requested time slot overlaps with an existing confirmed booking",
                HttpStatus.CONFLICT,
                "BOOKING_OVERLAP_CONFLICT"
            );
        }

        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setUser(user);
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setNotes(request.notes());

        Booking saved = bookingRepository.save(booking);
        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(UserPrincipal currentUser, Pageable pageable) {
        return bookingRepository.findByUserId(currentUser.getId(), pageable)
            .map(BookingResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsFiltered(
        Long resourceId,
        Long userId,
        Instant startFrom,
        Instant endTo,
        BookingStatus status,
        Pageable pageable
    ) {
        Specification<Booking> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (resourceId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("resource").get("id"), resourceId));
            }
            if (userId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("user").get("id"), userId));
            }
            if (startFrom != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("startTime"), startFrom));
            }
            if (endTo != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("endTime"), endTo));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            return predicates;
        };

        return bookingRepository.findAll(spec, pageable).map(BookingResponse::fromEntity);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, UserPrincipal currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals(Role.ADMIN.name()));

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        return BookingResponse.fromEntity(updated);
    }

    private void validateBookingTimeAndRules(CreateBookingRequest request, Resource resource) {
        Instant now = Instant.now();
        if (request.startTime().isBefore(now)) {
            throw new AppException("Booking start time must be in the future", HttpStatus.BAD_REQUEST, "PAST_BOOKING_NOT_ALLOWED");
        }

        if (!request.startTime().isBefore(request.endTime())) {
            throw new AppException("Booking start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE");
        }

        long durationMinutes = Duration.between(request.startTime(), request.endTime()).toMinutes();

        if (durationMinutes < resource.getMinBookingDurationMinutes()) {
            throw new AppException(
                "Booking duration of " + durationMinutes + " minutes is less than minimum allowed duration of " + resource.getMinBookingDurationMinutes() + " minutes",
                HttpStatus.BAD_REQUEST,
                "DURATION_TOO_SHORT"
            );
        }

        if (durationMinutes > resource.getMaxBookingDurationMinutes()) {
            throw new AppException(
                "Booking duration of " + durationMinutes + " minutes exceeds maximum allowed duration of " + resource.getMaxBookingDurationMinutes() + " minutes",
                HttpStatus.BAD_REQUEST,
                "DURATION_TOO_LONG"
            );
        }

        int granularity = resource.getSlotGranularityMinutes();
        ZonedDateTime zdtStart = request.startTime().atZone(ZoneId.of("UTC"));
        if (zdtStart.getMinute() % granularity != 0 || zdtStart.getSecond() != 0 || zdtStart.getNano() != 0) {
            throw new AppException(
                "Booking start time must align to slot granularity of " + granularity + " minutes",
                HttpStatus.BAD_REQUEST,
                "SLOT_UNALIGNED"
            );
        }

        if (durationMinutes % granularity != 0) {
            throw new AppException(
                "Booking duration must be a multiple of slot granularity (" + granularity + " minutes)",
                HttpStatus.BAD_REQUEST,
                "DURATION_UNALIGNED"
            );
        }

        // Validate Opening Hours for target day
        DayOfWeek dayOfWeek = zdtStart.getDayOfWeek();
        OpeningHours openingHours = resource.getOpeningHours().stream()
            .filter(oh -> oh.getDayOfWeek() == dayOfWeek)
            .findFirst()
            .orElse(null);

        if (openingHours == null || openingHours.isClosed()) {
            throw new AppException("Resource is closed on " + dayOfWeek, HttpStatus.BAD_REQUEST, "RESOURCE_CLOSED");
        }

        LocalTime bookingStartLocal = zdtStart.toLocalTime();
        ZonedDateTime zdtEnd = request.endTime().atZone(ZoneId.of("UTC"));
        LocalTime bookingEndLocal = zdtEnd.toLocalTime();

        if (bookingStartLocal.isBefore(openingHours.getOpenTime())) {
            throw new AppException("Booking start time is before resource opening time (" + openingHours.getOpenTime() + ")", HttpStatus.BAD_REQUEST, "OUTSIDE_OPENING_HOURS");
        }

        // If booking ends on same day, check close time
        if (zdtStart.toLocalDate().equals(zdtEnd.toLocalDate())) {
            if (bookingEndLocal.isAfter(openingHours.getCloseTime())) {
                throw new AppException("Booking end time is after resource closing time (" + openingHours.getCloseTime() + ")", HttpStatus.BAD_REQUEST, "OUTSIDE_OPENING_HOURS");
            }
        } else {
            throw new AppException("Bookings spanning across multiple days are not allowed", HttpStatus.BAD_REQUEST, "MULTI_DAY_BOOKING_NOT_ALLOWED");
        }
    }
}
