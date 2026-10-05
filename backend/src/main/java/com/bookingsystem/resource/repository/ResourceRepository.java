package com.bookingsystem.resource.repository;

import com.bookingsystem.resource.model.Resource;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Page<Resource> findAllByActiveTrue(Pageable pageable);

    @Query("SELECT r FROM Resource r LEFT JOIN FETCH r.openingHours WHERE r.id = :id")
    Optional<Resource> findByIdWithOpeningHours(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resource r LEFT JOIN FETCH r.openingHours WHERE r.id = :id")
    Optional<Resource> findByIdForUpdate(@Param("id") Long id);
}
