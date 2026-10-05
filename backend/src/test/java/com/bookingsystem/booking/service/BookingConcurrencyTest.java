package com.bookingsystem.booking.service;

import com.bookingsystem.auth.model.Role;
import com.bookingsystem.auth.model.User;
import com.bookingsystem.auth.repository.UserRepository;
import com.bookingsystem.booking.dto.BookingResponse;
import com.bookingsystem.booking.dto.CreateBookingRequest;
import com.bookingsystem.booking.model.BookingStatus;
import com.bookingsystem.booking.repository.BookingRepository;
import com.bookingsystem.common.exception.AppException;
import com.bookingsystem.resource.model.OpeningHours;
import com.bookingsystem.resource.model.Resource;
import com.bookingsystem.resource.repository.ResourceRepository;
import com.bookingsystem.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private UserPrincipal testUserPrincipal;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("concurrent.user@example.com");
        testUser.setPasswordHash("hashed_pass");
        testUser.setFullName("Concurrent Tester");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        testUserPrincipal = new UserPrincipal(
            testUser.getId(), testUser.getEmail(), testUser.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        testResource = new Resource();
        testResource.setName("High Demand Resource");
        testResource.setCapacity(1);
        testResource.setType("ROOM");
        testResource.setActive(true);
        testResource.setSlotGranularityMinutes(30);
        testResource.setMinBookingDurationMinutes(30);
        testResource.setMaxBookingDurationMinutes(120);

        for (DayOfWeek day : DayOfWeek.values()) {
            OpeningHours oh = new OpeningHours(day, LocalTime.of(8, 0), LocalTime.of(18, 0), false);
            testResource.addOpeningHours(oh);
        }
        testResource = resourceRepository.save(testResource);
    }

    @Test
    @DisplayName("Concurrency Test: 10 simultaneous requests for identical slot -> Exactly 1 succeeds, 9 get 409 Conflict")
    void concurrentBookingRequests_OnlyOneSucceeds() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);

        // Next Monday 10:00 to 11:00 UTC
        ZonedDateTime nextMonday10am = ZonedDateTime.now(ZoneId.of("UTC"))
            .plusDays(7)
            .with(DayOfWeek.MONDAY)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);

        Instant startTime = nextMonday10am.toInstant();
        Instant endTime = nextMonday10am.plusHours(1).toInstant();

        CreateBookingRequest request = new CreateBookingRequest(
            testResource.getId(), startTime, endTime, "Simultaneous booking test"
        );

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Synchronize all 10 threads to fire simultaneously
                    BookingResponse response = bookingService.createBooking(request, testUserPrincipal);
                    if (response != null && response.status() == BookingStatus.CONFIRMED) {
                        successCount.incrementAndGet();
                    }
                } catch (AppException ex) {
                    if (ex.getStatus() == HttpStatus.CONFLICT) {
                        conflictCount.incrementAndGet();
                    } else {
                        exceptions.add(ex);
                    }
                } catch (DataIntegrityViolationException ex) {
                    // PostgreSQL Exclusion constraint mapped conflict
                    conflictCount.incrementAndGet();
                } catch (Throwable t) {
                    exceptions.add(t);
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Release the gate!

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(finished).isTrue();
        assertThat(exceptions).isEmpty();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(numberOfThreads - 1);

        // Verify database state: exactly 1 active confirmed booking exists
        long dbBookingCount = bookingRepository.count();
        assertThat(dbBookingCount).isEqualTo(1);
    }
}
