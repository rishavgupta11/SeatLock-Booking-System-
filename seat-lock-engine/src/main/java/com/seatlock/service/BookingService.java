package com.seatlock.service;

import com.seatlock.entity.*;
import com.seatlock.repository.BookingRepository;
import com.seatlock.repository.SeatRepository;
import com.seatlock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * 1. LOCK SEAT LOGIC (Pessimistic Locking ke sath)
     * Jab user seat hold karne ki request bhejega.
     */
    @Transactional
    public Booking lockSeat(Long userId, Long seatId) {
        // User check karo
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Seat ko Pessimistic Lock ke sath fetch karo (Race condition prevent karne ke liye)
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        // Check karo ki seat AVAILABLE hai ya nahi
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Seat is already locked or booked!");
        }

        // Seat ka status LOCKED karo
        seat.setStatus(SeatStatus.LOCKED);
        seatRepository.save(seat);

        // Nayi booking entry banao (5 minutes expiry time ke sath)
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        return bookingRepository.save(booking);
    }

    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check karein ki booking PENDING_PAYMENT state mein hai ya nahi
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking cannot be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        // Seat status ko permanent BOOKED kar sakte hain agar zaroori ho
        booking.getSeat().setStatus(SeatStatus.BOOKED);

        return bookingRepository.save(booking);
    }

    /**
     * 2. SCHEDULED EXPIRY CLEANUP TASK
     * Har 1 minute mein background mein run hoga aur expired locks ko release karega.
     */
    @Scheduled(fixedRate = 60000) // 60000 ms = 1 Minute
    @Transactional
    public void releaseExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Sabhi expired bookings nikalo jinka payment nahi hua
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.PENDING_PAYMENT, now);

        for (Booking booking : expiredBookings) {
            // Booking status CANCELLED karo
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // Seat status wapas AVAILABLE karo taaki doosra user book kar sake
            Seat seat = booking.getSeat();
            if (seat.getStatus() == SeatStatus.LOCKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
        }

        if (!expiredBookings.isEmpty()) {
            System.out.println("Released " + expiredBookings.size() + " expired seat locks.");
        }
    }
}