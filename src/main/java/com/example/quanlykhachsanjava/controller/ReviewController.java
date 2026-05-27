package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Review;
import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.repository.UserRepository;
import com.example.quanlykhachsanjava.service.BookingService;
import com.example.quanlykhachsanjava.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ReviewController {

    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(BookingService bookingService,
                            ReviewService reviewService,
                            UserRepository userRepository) {
        this.bookingService = bookingService;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @PostMapping("/reviews/create")
    public String createReview(@RequestParam Long bookingId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOptional = bookingService.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/booking/history";
        }

        Booking booking = bookingOptional.get();
        if (!booking.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền đánh giá đơn này.");
            return "redirect:/booking/history";
        }

        if (!"COMPLETED".equalsIgnoreCase(booking.getBookingStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ được đánh giá sau khi hoàn tất lưu trú.");
            return "redirect:/booking/detail/" + bookingId;
        }

        if (reviewService.findByBookingId(bookingId).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã gửi đánh giá cho đơn này.");
            return "redirect:/booking/detail/" + bookingId;
        }

        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Điểm đánh giá không hợp lệ.");
            return "redirect:/booking/detail/" + bookingId;
        }

        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setRoom(booking.getRoom());
        review.setUser(userOptional.get());
        review.setRating(rating);
        review.setComment(clean(comment));
        review.setIsVisible(false);

        reviewService.save(review);

        redirectAttributes.addFlashAttribute("successMessage", "Đã gửi đánh giá. Vui lòng chờ duyệt.");
        return "redirect:/booking/detail/" + bookingId;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

