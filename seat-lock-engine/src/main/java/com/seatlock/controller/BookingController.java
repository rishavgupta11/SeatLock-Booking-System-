package com.seatlock.controller;

import com.seatlock.entity.Booking;
import com.seatlock.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // 1. Seat Lock Endpoint (Jo aapka pehle se chal raha hai)
    @PostMapping("/lock")
    public ResponseEntity<Booking> lockSeat(@RequestParam Long userId, @RequestParam Long seatId) {
        Booking booking = bookingService.lockSeat(userId, seatId);
        return ResponseEntity.ok(booking);
    }

    // 2. Booking Confirm Endpoint (Payment completion ke liye)
    @PostMapping("/confirm")
    public ResponseEntity<Booking> confirmBooking(@RequestParam Long bookingId) {
        Booking confirmedBooking = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(confirmedBooking);
    }
}