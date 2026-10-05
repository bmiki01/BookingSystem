package com.bookingsystem.booking.repository;

import com.bookingsystem.booking.model.Booking;
import com.bookingsystem.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.resource.id = :resourceId " +
           "AND b.status = com.bookingsystem.booking.model.BookingStatus.CONFIRMED " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
        @Param("resourceId") Long resourceId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.resource.id = :resourceId " +
           "AND b.status = com.bookingsystem.booking.model.BookingStatus.CONFIRMED " +
           "AND b.startTime < :end " +
           "AND b.endTime > :start")
    List<Booking> findActiveBookingsInDateRange(
        @Param("resourceId") Long resourceId,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
}
