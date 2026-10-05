package com.bookingsystem.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookingsystem.booking.dto.BookingResponse;
import com.bookingsystem.booking.dto.CreateBookingRequest;
import com.bookingsystem.booking.model.BookingStatus;
import com.bookingsystem.booking.service.BookingService;
import com.bookingsystem.security.JwtTokenProvider;
import com.bookingsystem.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UsernamePasswordAuthenticationToken userAuth() {
        UserPrincipal principal = new UserPrincipal(1L, "john@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private BookingResponse sampleBookingResponse() {
        return new BookingResponse(
            100L, 1L, "Room A", 1L, "John Doe", "john@example.com",
            Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            BookingStatus.CONFIRMED, "Notes", Instant.now()
        );
    }

    @Test
    void createBooking_Returns401_WhenUnauthenticated() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
            1L, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS), "Notes"
        );

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createBooking_Returns201Created_WhenAuthenticated() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
            1L, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS), "Notes"
        );

        when(bookingService.createBooking(any(CreateBookingRequest.class), any()))
            .thenReturn(sampleBookingResponse());

        mockMvc.perform(post("/api/v1/bookings")
                .with(authentication(userAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.resourceName").value("Room A"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void getMyBookings_Returns200Ok() throws Exception {
        when(bookingService.getMyBookings(any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(sampleBookingResponse())));

        mockMvc.perform(get("/api/v1/bookings/my").with(authentication(userAuth())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(100));
    }

    @Test
    void cancelBooking_Returns200Ok() throws Exception {
        BookingResponse cancelledResponse = new BookingResponse(
            100L, 1L, "Room A", 1L, "John Doe", "john@example.com",
            Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            BookingStatus.CANCELLED, "Notes", Instant.now()
        );

        when(bookingService.cancelBooking(any(), any())).thenReturn(cancelledResponse);

        mockMvc.perform(patch("/api/v1/bookings/100/cancel").with(authentication(userAuth())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
