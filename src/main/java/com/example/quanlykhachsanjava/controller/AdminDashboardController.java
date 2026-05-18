package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.service.BookingService;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class AdminDashboardController {

    private final RoomService roomService;
    private final CategoryService categoryService;
    private final BookingService bookingService;

    public AdminDashboardController(RoomService roomService,
                                    CategoryService categoryService,
                                    BookingService bookingService) {
        this.roomService = roomService;
        this.categoryService = categoryService;
        this.bookingService = bookingService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<Room> rooms = roomService.findAll();
        List<RoomCategory> categories = categoryService.findAll();
        List<Booking> bookings = bookingService.findAll();

        long totalRooms = rooms.size();
        long totalCategories = categories.size();
        long totalBookings = bookings.size();

        double totalRevenue = bookings.stream()
                .filter(booking -> booking.getTotalAmount() != null)
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        long pendingBookings = countBookingByStatus(bookings, "PENDING");
        long confirmedBookings = countBookingByStatus(bookings, "CONFIRMED");
        long cancelledBookings = countBookingByStatus(bookings, "CANCELLED");
        long completedBookings = countBookingByStatus(bookings, "COMPLETED");

        List<Booking> latestBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .limit(5)
                .toList();

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("completedBookings", completedBookings);

        model.addAttribute("latestBookings", latestBookings);

        return "admin/dashboard";
    }

    private long countBookingByStatus(List<Booking> bookings, String status) {
        return bookings.stream()
                .filter(booking -> status.equalsIgnoreCase(booking.getBookingStatus()))
                .count();
    }
}