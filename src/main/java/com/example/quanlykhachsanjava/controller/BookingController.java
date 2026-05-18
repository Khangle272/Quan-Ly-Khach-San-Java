package com.example.quanlykhachsanjava.controller;
import java.util.List;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.repository.UserRepository;
import com.example.quanlykhachsanjava.service.BookingService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService,
                             RoomService roomService,
                             UserRepository userRepository) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userRepository = userRepository;
    }

    @GetMapping("/booking/create")
    public String showBookingForm(
            @RequestParam Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Model model
    ) {
        Optional<Room> roomOptional = roomService.findById(roomId);

        if (roomOptional.isEmpty()) {
            return "redirect:/rooms";
        }

        Room room = roomOptional.get();

        model.addAttribute("room", room);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("numberOfGuests", room.getCategory().getCapacity());
        model.addAttribute("paymentMethod", "");
        model.addAttribute("specialRequests", "");

        return "booking-form";
    }

    @PostMapping("/booking/create")
    public String createBooking(
            @RequestParam Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer numberOfGuests,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String specialRequests,
            Principal principal,
            Model model
    ) {
        Optional<Room> roomOptional = roomService.findById(roomId);

        if (roomOptional.isEmpty()) {
            return "redirect:/rooms";
        }

        Room room = roomOptional.get();

        String validationError = validateBookingForm(room, checkIn, checkOut, numberOfGuests, paymentMethod);
        if (validationError != null) {
            prepareBookingForm(model, room, checkIn, checkOut, numberOfGuests, paymentMethod, specialRequests, validationError);
            return "booking-form";
        }

        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        Booking booking = new Booking();
        booking.setUser(userOptional.get());
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumberOfGuests(numberOfGuests);
        booking.setPaymentMethod(clean(paymentMethod));
        booking.setSpecialRequests(clean(specialRequests));
        booking.setBookingStatus("PENDING");
        booking.setPaymentStatus("UNPAID");

        try {
            Booking savedBooking = bookingService.createBooking(booking);
            return "redirect:/booking/success/" + savedBooking.getId();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            prepareBookingForm(
                    model,
                    room,
                    checkIn,
                    checkOut,
                    numberOfGuests,
                    paymentMethod,
                    specialRequests,
                    toVietnameseBookingError(exception.getMessage())
            );
            return "booking-form";
        }
    }

    @GetMapping("/booking/success/{id}")
    public String bookingSuccess(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {
        Optional<Booking> bookingOptional = bookingService.findById(id);

        if (bookingOptional.isEmpty()) {
            return "redirect:/rooms";
        }

        Booking booking = bookingOptional.get();

        if (!booking.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/rooms";
        }

        model.addAttribute("booking", booking);

        return "booking-success";
    }
    @GetMapping("/booking/history")
    public String bookingHistory(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.findByUsername(principal.getName());

        model.addAttribute("bookings", bookings);

        return "booking-history";
    }

    @GetMapping("/booking/detail/{id}")
    public String bookingDetail(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOptional = bookingService.findByIdAndUsername(id, principal.getName());

        if (bookingOptional.isEmpty()) {
            return "redirect:/booking/history";
        }

        model.addAttribute("booking", bookingOptional.get());

        return "booking-detail";
    }

    private String validateBookingForm(Room room,
                                       LocalDate checkIn,
                                       LocalDate checkOut,
                                       Integer numberOfGuests,
                                       String paymentMethod) {
        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            return "Phòng này hiện không khả dụng để đặt.";
        }

        if (checkIn == null) {
            return "Vui lòng chọn ngày nhận phòng.";
        }

        if (checkOut == null) {
            return "Vui lòng chọn ngày trả phòng.";
        }

        if (!checkOut.isAfter(checkIn)) {
            return "Ngày trả phòng phải sau ngày nhận phòng.";
        }

        if (numberOfGuests == null || numberOfGuests < 1) {
            return "Số khách không hợp lệ.";
        }

        Integer capacity = room.getCategory().getCapacity();
        if (capacity != null && numberOfGuests > capacity) {
            return "Số khách vượt quá sức chứa của phòng.";
        }

        if (isBlank(paymentMethod)) {
            return "Vui lòng chọn phương thức thanh toán.";
        }

        return null;
    }

    private void prepareBookingForm(Model model,
                                    Room room,
                                    LocalDate checkIn,
                                    LocalDate checkOut,
                                    Integer numberOfGuests,
                                    String paymentMethod,
                                    String specialRequests,
                                    String errorMessage) {
        model.addAttribute("room", room);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("numberOfGuests", numberOfGuests);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("specialRequests", specialRequests);
        model.addAttribute("errorMessage", errorMessage);
    }

    private String toVietnameseBookingError(String message) {
        if (message == null) {
            return "Không thể tạo đặt phòng. Vui lòng thử lại.";
        }

        if (message.contains("Dates cannot be null")) {
            return "Vui lòng chọn đầy đủ ngày nhận phòng và ngày trả phòng.";
        }

        if (message.contains("Check-out date must be greater")) {
            return "Ngày trả phòng phải sau ngày nhận phòng.";
        }

        if (message.contains("Room not found")) {
            return "Phòng không tồn tại.";
        }

        if (message.contains("Room is already booked")) {
            return "Phòng này đã có người đặt trong khoảng thời gian bạn chọn.";
        }

        return message;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}