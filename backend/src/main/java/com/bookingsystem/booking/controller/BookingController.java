package com.bookingsystem.booking.controller;

import com.bookingsystem.booking.dto.BookingResponse;
import com.bookingsystem.booking.dto.CreateBookingRequest;
import com.bookingsystem.booking.model.BookingStatus;
import com.bookingsystem.booking.service.BookingService;
import com.bookingsystem.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for creating, managing, and cancelling reservations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a new booking reservation", description = "Creates a new booking for an active resource. Validates future dates, slot alignment, opening hours, and double-booking overlap.")
    public ResponseEntity<BookingResponse> createBooking(
        @Valid @RequestBody CreateBookingRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        BookingResponse created = bookingService.createBooking(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's own bookings", description = "Returns a paginated list of reservations created by the authenticated user")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
        @AuthenticationPrincipal UserPrincipal currentUser,
        @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        Page<BookingResponse> page = bookingService.getMyBookings(currentUser, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings (ADMIN only)", description = "Allows administrators to query and filter all system bookings")
    public ResponseEntity<Page<BookingResponse>> getAllBookingsFiltered(
        @RequestParam(required = false) Long resourceId,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTo,
        @RequestParam(required = false) BookingStatus status,
        @PageableDefault(size = 20, sort = "startTime") Pageable pageable
    ) {
        Page<BookingResponse> page = bookingService.getAllBookingsFiltered(resourceId, userId, startFrom, endTo, status, pageable);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking", description = "Cancels a booking. Users can cancel their own bookings; ADMINs can cancel any booking.")
    public ResponseEntity<BookingResponse> cancelBooking(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        BookingResponse cancelled = bookingService.cancelBooking(id, currentUser);
        return ResponseEntity.ok(cancelled);
    }
}
