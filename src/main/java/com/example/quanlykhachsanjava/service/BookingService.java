package com.example.quanlykhachsanjava.service;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.repository.BookingRepository;
import com.example.quanlykhachsanjava.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(Booking booking) {
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (!booking.getCheckOutDate().isAfter(booking.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be greater than check-in date");
        }

        Room room = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        long overlapping = bookingRepository.countOverlappingBookings(room.getId(), booking.getCheckInDate(), booking.getCheckOutDate());
        if (overlapping > 0) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        Double price = room.getCategory().getPrice();
        booking.setTotalAmount(days * price);

        return bookingRepository.save(booking);
    }

    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }
    
    public List<Booking> findByUsername(String username) {
        return bookingRepository.findByUserUsernameOrderByIdDesc(username);
    }

    public Optional<Booking> findByIdAndUsername(Long id, String username) {
        return bookingRepository.findByIdAndUserUsername(id, username);
    }

    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setBookingStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking updatePaymentStatus(Long id, String paymentStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setPaymentStatus(paymentStatus);
        return bookingRepository.save(booking);
    }
}
