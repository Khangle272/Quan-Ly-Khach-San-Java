package com.example.quanlykhachsanjava.controller;
import java.util.List;

import com.example.quanlykhachsanjava.dto.DiscountResult;
import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Coupon;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.repository.UserRepository;
import com.example.quanlykhachsanjava.service.BookingService;
import com.example.quanlykhachsanjava.service.CouponService;
import com.example.quanlykhachsanjava.service.ReviewService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final ReviewService reviewService;

    public BookingController(BookingService bookingService,
                             RoomService roomService,
                             UserRepository userRepository,
                             CouponService couponService,
                             ReviewService reviewService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userRepository = userRepository;
        this.couponService = couponService;
        this.reviewService = reviewService;
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
        int availableRooms = resolveAvailableRooms(room, checkIn, checkOut);

        model.addAttribute("room", room);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("numberOfGuests", room.getCategory().getCapacity());
        model.addAttribute("roomQuantity", 1);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("paymentMethod", "");
        model.addAttribute("specialRequests", "");
        model.addAttribute("couponCode", "");

        return "booking-form";
    }

    @PostMapping("/booking/create")
    public String createBooking(
            @RequestParam Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer numberOfGuests,
            @RequestParam(required = false) Integer roomQuantity,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String specialRequests,
            @RequestParam(required = false) String couponCode,
            Principal principal,
            Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Room> roomOptional = roomService.findById(roomId);

        if (roomOptional.isEmpty()) {
            return "redirect:/rooms";
        }

        Room room = roomOptional.get();
        int availableRooms = resolveAvailableRooms(room, checkIn, checkOut);

        String validationError = validateBookingForm(room, checkIn, checkOut, numberOfGuests, roomQuantity, paymentMethod, availableRooms);
        if (validationError != null) {
            prepareBookingForm(model, room, checkIn, checkOut, numberOfGuests, roomQuantity, availableRooms, paymentMethod, specialRequests, couponCode, validationError);
            return "booking-form";
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

        String normalizedCoupon = clean(couponCode).toUpperCase();
        if (!normalizedCoupon.isEmpty()) {
            Coupon coupon = couponService.findActiveByCode(normalizedCoupon)
                    .orElse(null);
            if (coupon == null) {
                prepareBookingForm(model, room, checkIn, checkOut, numberOfGuests, roomQuantity, availableRooms, paymentMethod, specialRequests, couponCode, "Mã giảm giá không hợp lệ.");
                return "booking-form";
            }

            long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            double originalTotal = nights * room.getCategory().getPrice() * (roomQuantity == null ? 1 : roomQuantity);
            DiscountResult discountResult = couponService.applyCoupon(coupon, originalTotal, nights);
            if (!discountResult.isApplied()) {
                prepareBookingForm(model, room, checkIn, checkOut, numberOfGuests, roomQuantity, availableRooms, paymentMethod, specialRequests, couponCode, "Mã giảm giá chưa đủ điều kiện áp dụng.");
                return "booking-form";
            }
            booking.setCoupon(coupon);
        }

        try {
            Booking savedBooking = bookingService.createBooking(booking, roomQuantity == null ? 1 : roomQuantity);
            return "redirect:/booking/success/" + savedBooking.getId();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            prepareBookingForm(
                    model,
                    room,
                    checkIn,
                    checkOut,
                    numberOfGuests,
                    roomQuantity,
                    availableRooms,
                    paymentMethod,
                    specialRequests,
                    couponCode,
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
        model.addAttribute("bookingGroupTotal", calculateGroupTotal(booking));
        model.addAttribute("priceSummary", bookingService.calculatePriceSummary(booking));

        return "booking-success";
    }

    private Double calculateGroupTotal(Booking booking) {
        if (booking.getTotalAmount() == null) {
            return 0.0;
        }
        int rooms = booking.getRoomQuantity() == null ? 1 : booking.getRoomQuantity();
        return booking.getTotalAmount() * rooms;
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

        Booking booking = bookingOptional.get();
        var existingReview = reviewService.findByBookingId(id).orElse(null);

        model.addAttribute("booking", booking);
        model.addAttribute("priceSummary", bookingService.calculatePriceSummary(booking));
        model.addAttribute("review", existingReview);
        model.addAttribute(
                "canReview",
                "COMPLETED".equalsIgnoreCase(booking.getBookingStatus()) && existingReview == null
        );

        return "booking-detail";
    }

    @PostMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            bookingService.cancelBookingByUser(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt phòng.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn đặt phòng.");
        }

        return "redirect:/booking/detail/" + id;
    }

    private String validateBookingForm(Room room,
                                       LocalDate checkIn,
                                       LocalDate checkOut,
                                       Integer numberOfGuests,
                                       Integer roomQuantity,
                                       String paymentMethod,
                                       int availableRooms) {
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

        int safeRoomQuantity = roomQuantity == null ? 1 : roomQuantity;
        if (safeRoomQuantity < 1) {
            return "Số lượng phòng không hợp lệ.";
        }

        if (safeRoomQuantity > availableRooms) {
            return "Số lượng phòng vượt quá số phòng còn trống trong khoảng thời gian đã chọn.";
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
                                    Integer roomQuantity,
                                    int availableRooms,
                                    String paymentMethod,
                                    String specialRequests,
                                    String couponCode,
                                    String errorMessage) {
        model.addAttribute("room", room);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("numberOfGuests", numberOfGuests);
        model.addAttribute("roomQuantity", roomQuantity == null ? 1 : roomQuantity);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("specialRequests", specialRequests);
        model.addAttribute("couponCode", couponCode);
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

        if (message.contains("Not enough rooms available")) {
            return "Số lượng phòng trống không đủ cho khoảng thời gian bạn chọn.";
        }

        return message;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int resolveAvailableRooms(Room room, LocalDate checkIn, LocalDate checkOut) {
        try {
            return roomService.countAvailableRooms(room.getCategory().getId(), checkIn, checkOut);
        } catch (IllegalArgumentException ex) {
            return 0;
        }
    }
}