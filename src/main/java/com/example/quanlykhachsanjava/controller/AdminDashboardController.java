package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.service.BookingService;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.RoomService;
import com.example.quanlykhachsanjava.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminDashboardController {

    private final RoomService roomService;
    private final CategoryService categoryService;
    private final BookingService bookingService;
    private final UserService userService;

    public AdminDashboardController(RoomService roomService,
                                    CategoryService categoryService,
                                    BookingService bookingService,
                                    UserService userService) {
        this.roomService = roomService;
        this.categoryService = categoryService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<Room> rooms = roomService.findAll();
        List<RoomCategory> categories = categoryService.findAll();
        List<Booking> bookings = bookingService.findAll();
        List<User> users = userService.findAll();

        long totalRooms = rooms.size();
        long totalCategories = categories.size();
        long totalBookings = bookings.size();
        long totalCustomers = users.stream()
                .filter(user -> "ROLE_USER".equalsIgnoreCase(user.getRole()))
                .count();

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

        List<String> lowAvailabilityAlerts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        for (RoomCategory category : categories) {
            int availableRooms = roomService.countAvailableRooms(category.getId(), today, tomorrow);
            if (availableRooms < 5) {
                lowAvailabilityAlerts.add(category.getName() + ": còn " + availableRooms + " phòng trống");
            }
        }

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("completedBookings", completedBookings);

        model.addAttribute("latestBookings", latestBookings);
        model.addAttribute("lowAvailabilityAlerts", lowAvailabilityAlerts);

        Map<YearMonth, List<Booking>> bookingsByMonth = new HashMap<>();
        for (Booking booking : bookings) {
            if (booking.getCreatedAt() == null) {
                continue;
            }
            YearMonth month = YearMonth.from(booking.getCreatedAt());
            bookingsByMonth.computeIfAbsent(month, key -> new ArrayList<>()).add(booking);
        }

        Map<YearMonth, Long> customersByMonth = new HashMap<>();
        for (User user : users) {
            if (user.getCreatedAt() == null || !"ROLE_USER".equalsIgnoreCase(user.getRole())) {
                continue;
            }
            YearMonth month = YearMonth.from(user.getCreatedAt());
            customersByMonth.put(month, customersByMonth.getOrDefault(month, 0L) + 1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        List<String> monthLabels = new ArrayList<>();
        List<Double> revenueSeries = new ArrayList<>();
        List<Long> bookingSeries = new ArrayList<>();
        List<Long> customerSeries = new ArrayList<>();
        List<Map<String, Object>> monthSummaries = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            List<Booking> monthBookings = bookingsByMonth.getOrDefault(month, List.of());
            double monthRevenue = monthBookings.stream()
                    .filter(booking -> booking.getTotalAmount() != null)
                    .mapToDouble(Booking::getTotalAmount)
                    .sum();
            long monthBookingCount = monthBookings.size();
            long monthCustomerCount = customersByMonth.getOrDefault(month, 0L);

            monthLabels.add(month.format(formatter));
            revenueSeries.add(monthRevenue);
            bookingSeries.add(monthBookingCount);
            customerSeries.add(monthCustomerCount);

            Map<String, Object> summary = new HashMap<>();
            summary.put("label", month.format(formatter));
            summary.put("revenue", monthRevenue);
            summary.put("bookings", monthBookingCount);
            summary.put("customers", monthCustomerCount);
            monthSummaries.add(summary);
        }

        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("revenueSeries", revenueSeries);
        model.addAttribute("bookingSeries", bookingSeries);
        model.addAttribute("customerSeries", customerSeries);
        model.addAttribute("monthSummaries", monthSummaries);

        return "admin/dashboard";
    }

    private long countBookingByStatus(List<Booking> bookings, String status) {
        return bookings.stream()
                .filter(booking -> status.equalsIgnoreCase(booking.getBookingStatus()))
                .count();
    }
}