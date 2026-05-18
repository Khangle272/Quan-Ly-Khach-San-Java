package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/admin/bookings")
    public String listBookings(Model model) {
        List<Booking> bookings = bookingService.findAll().stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .toList();

        model.addAttribute("bookings", bookings);

        return "admin/bookings";
    }

    @GetMapping("/admin/bookings/{id}")
    public String bookingDetail(@PathVariable Long id,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Optional<Booking> bookingOptional = bookingService.findById(id);

        if (bookingOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/admin/bookings";
        }

        model.addAttribute("booking", bookingOptional.get());

        return "admin/booking-detail";
    }

    @PostMapping("/admin/bookings/{id}/confirm")
    public String confirmBooking(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(id, "CONFIRMED");
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đơn đặt phòng.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể duyệt đơn đặt phòng.");
        }

        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/admin/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(id, "CANCELLED");
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt phòng.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn đặt phòng.");
        }

        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/admin/bookings/{id}/payment")
    public String updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.updatePaymentStatus(id, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái thanh toán.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái thanh toán.");
        }

        return "redirect:/admin/bookings/" + id;
    }
}