package com.seatlock.repository;

import com.seatlock.entity.Seat;
import com.seatlock.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByStatus(SeatStatus status);

    /**
     * PESSIMISTIC_WRITE Lock:
     * Ye method database row par exclusive write lock laga deta hai.
     * Jab tak ye transaction chal raha hai, doosri thread is seat ko read/write nahi kar sakti.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);
}
