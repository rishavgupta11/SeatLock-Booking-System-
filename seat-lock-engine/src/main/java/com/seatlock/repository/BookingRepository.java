package com.seatlock.repository;

import com.seatlock.entity.Booking;
import com.seatlock.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Expiry time nikal chuki hai aur status abhi bhi PENDING_PAYMENT hai, unhe dhundne ke liye
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.expiresAt < :currentTime")
    List<Booking> findExpiredBookings(
            @Param("status") BookingStatus status,
            @Param("currentTime") LocalDateTime currentTime
    );
}
