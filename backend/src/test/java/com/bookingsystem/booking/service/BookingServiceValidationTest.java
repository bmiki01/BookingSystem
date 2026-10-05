package com.bookingsystem.booking.service;

import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.model.User;
import com.bookingsystem.auth.repository.UserRepository;
import com.bookingsystem.booking.dto.CreateBookingRequest;
import com.bookingsystem.booking.repository.BookingRepository;
import com.bookingsystem.common.exception.AppException;
import com.bookingsystem.resource.model.OpeningHours;
import com.bookingsystem.resource.model.Resource;
import com.bookingsystem.resource.repository.ResourceRepository;
import com.bookingsystem.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceValidationTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private User sampleUser;
    private UserPrincipal userPrincipal;
    private Resource sampleResource;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "user@example.com", "pass", "User", Role.USER, Instant.now(), Instant.now());
        userPrincipal = new UserPrincipal(1L, "user@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        sampleResource = new Resource();
        sampleResource.setId(10L);
        sampleResource.setName("Conference Room A");
        sampleResource.setActive(true);
        sampleResource.setSlotGranularityMinutes(30);
        sampleResource.setMinBookingDurationMinutes(30);
        sampleResource.setMaxBookingDurationMinutes(120);

        // Standard 08:00 - 18:00 Monday to Friday opening hours
        for (DayOfWeek day : DayOfWeek.values()) {
            boolean weekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
            OpeningHours oh = new OpeningHours(day, LocalTime.of(8, 0), LocalTime.of(18, 0), weekend);
            sampleResource.addOpeningHours(oh);
        }
    }

    @Test
    void createBooking_Fails_WhenStartInPast() {
        Instant pastStart = Instant.now().minusSeconds(3600);
        Instant pastEnd = pastStart.plusSeconds(1800);
        CreateBookingRequest request = new CreateBookingRequest(10L, pastStart, pastEnd, "Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(resourceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleResource));

        assertThatThrownBy(() -> bookingService.createBooking(request, userPrincipal))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("future");
    }

    @Test
    void createBooking_Fails_WhenDurationBelowMinimum() {
        // Next Monday 10:00 to 10:15 (15 mins duration vs 30 min minimum)
        ZonedDateTime nextMonday = ZonedDateTime.now(ZoneId.of("UTC"))
            .plusDays(7)
            .with(DayOfWeek.MONDAY)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);

        Instant start = nextMonday.toInstant();
        Instant end = nextMonday.plusMinutes(15).toInstant();
        CreateBookingRequest request = new CreateBookingRequest(10L, start, end, "Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(resourceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleResource));

        assertThatThrownBy(() -> bookingService.createBooking(request, userPrincipal))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("less than minimum allowed duration");
    }

    @Test
    void createBooking_Fails_WhenSlotUnaligned() {
        // Next Monday 10:17 (not aligned to 30 min granularity)
        ZonedDateTime nextMonday = ZonedDateTime.now(ZoneId.of("UTC"))
            .plusDays(7)
            .with(DayOfWeek.MONDAY)
            .withHour(10).withMinute(17).withSecond(0).withNano(0);

        Instant start = nextMonday.toInstant();
        Instant end = nextMonday.plusMinutes(30).toInstant();
        CreateBookingRequest request = new CreateBookingRequest(10L, start, end, "Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(resourceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleResource));

        assertThatThrownBy(() -> bookingService.createBooking(request, userPrincipal))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("align to slot granularity");
    }

    @Test
    void createBooking_Fails_WhenOutsideOpeningHours() {
        // Next Monday 07:00 (opens at 08:00)
        ZonedDateTime nextMonday = ZonedDateTime.now(ZoneId.of("UTC"))
            .plusDays(7)
            .with(DayOfWeek.MONDAY)
            .withHour(7).withMinute(0).withSecond(0).withNano(0);

        Instant start = nextMonday.toInstant();
        Instant end = nextMonday.plusMinutes(30).toInstant();
        CreateBookingRequest request = new CreateBookingRequest(10L, start, end, "Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(resourceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleResource));

        assertThatThrownBy(() -> bookingService.createBooking(request, userPrincipal))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("before resource opening time");
    }

    @Test
    void createBooking_Fails_WhenOverlappingExistingBooking() {
        ZonedDateTime nextMonday = ZonedDateTime.now(ZoneId.of("UTC"))
            .plusDays(7)
            .with(DayOfWeek.MONDAY)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);

        Instant start = nextMonday.toInstant();
        Instant end = nextMonday.plusMinutes(60).toInstant();
        CreateBookingRequest request = new CreateBookingRequest(10L, start, end, "Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(resourceRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleResource));
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request, userPrincipal))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("overlaps with an existing confirmed booking");
    }
}
