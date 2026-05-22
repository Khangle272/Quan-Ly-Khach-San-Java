package com.example.quanlykhachsanjava.service;

import com.example.quanlykhachsanjava.dto.DiscountResult;
import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.repository.BookingRepository;
import com.example.quanlykhachsanjava.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_STAYING = "STAYING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = new HashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(STATUS_PENDING, Set.of(STATUS_CONFIRMED));
        ALLOWED_TRANSITIONS.put(STATUS_CONFIRMED, Set.of(STATUS_STAYING));
        ALLOWED_TRANSITIONS.put(STATUS_STAYING, Set.of(STATUS_COMPLETED));
        ALLOWED_TRANSITIONS.put(STATUS_COMPLETED, new HashSet<>());
        ALLOWED_TRANSITIONS.put(STATUS_CANCELLED, new HashSet<>());
    }

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CouponService couponService;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(Booking booking, int roomQuantity) {
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (!booking.getCheckOutDate().isAfter(booking.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be greater than check-in date");
        }
        if (roomQuantity < 1) {
            throw new IllegalArgumentException("Room quantity must be at least 1");
        }

        Room baseRoom = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        List<Room> availableRooms = roomRepository.findAvailableRoomsByCategoryAndDates(
                baseRoom.getCategory().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );

        if (availableRooms.size() < roomQuantity) {
            throw new IllegalStateException("Not enough rooms available for the selected dates");
        }

        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        Double price = baseRoom.getCategory().getPrice();
        Double totalAmountPerRoom = days * price;
        Double originalGroupTotal = totalAmountPerRoom * roomQuantity;

        DiscountResult discountResult = couponService.applyCoupon(booking.getCoupon(), originalGroupTotal, days);
        Double finalGroupTotal = discountResult.getFinalAmount();
        Double finalAmountPerRoom = finalGroupTotal / roomQuantity;
        String groupCode = "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Booking firstBooking = null;
        for (int i = 0; i < roomQuantity; i++) {
            Room assignedRoom = availableRooms.get(i);
            Booking newBooking = new Booking();
            newBooking.setUser(booking.getUser());
            newBooking.setRoom(assignedRoom);
            newBooking.setCheckInDate(booking.getCheckInDate());
            newBooking.setCheckOutDate(booking.getCheckOutDate());
            newBooking.setNumberOfGuests(booking.getNumberOfGuests());
            newBooking.setPaymentMethod(booking.getPaymentMethod());
            newBooking.setSpecialRequests(booking.getSpecialRequests());
            newBooking.setBookingStatus(booking.getBookingStatus());
            newBooking.setPaymentStatus(booking.getPaymentStatus());
            newBooking.setCoupon(booking.getCoupon());
            newBooking.setRoomQuantity(roomQuantity);
            newBooking.setBookingGroupCode(groupCode);
            newBooking.setTotalAmount(finalAmountPerRoom);

            Booking saved = bookingRepository.save(newBooking);
            if (firstBooking == null) {
                firstBooking = saved;
            }
        }

        return firstBooking;
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

        String currentStatus = booking.getBookingStatus();
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();
        if (!isTransitionAllowed(currentStatus, normalizedStatus)) {
            throw new IllegalStateException("Invalid booking status transition");
        }

        booking.setBookingStatus(normalizedStatus);
        return bookingRepository.save(booking);
    }

    public Booking cancelBookingByUser(Long id, String username) {
        Booking booking = bookingRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!STATUS_PENDING.equalsIgnoreCase(booking.getBookingStatus())) {
            throw new IllegalStateException("Only pending bookings can be cancelled by customer");
        }

        booking.setBookingStatus(STATUS_CANCELLED);
        return bookingRepository.save(booking);
    }

    public DiscountResult calculatePriceSummary(Booking booking) {
        if (booking.getRoom() == null || booking.getRoom().getCategory() == null) {
            return new DiscountResult(0.0, 0.0, 0.0, false, "Missing room info");
        }

        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        int quantity = booking.getRoomQuantity() == null ? 1 : booking.getRoomQuantity();
        double originalTotal = days * booking.getRoom().getCategory().getPrice() * quantity;
        return couponService.applyCoupon(booking.getCoupon(), originalTotal, days);
    }

    public Booking updatePaymentStatus(Long id, String paymentStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setPaymentStatus(paymentStatus);
        return bookingRepository.save(booking);
    }

    private boolean isTransitionAllowed(String currentStatus, String nextStatus) {
        if (currentStatus == null || nextStatus == null) {
            return false;
        }
        String current = currentStatus.toUpperCase();
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, new HashSet<>());
        return allowed.contains(nextStatus);
    }
}
