package com.example.quanlykhachsanjava;

import com.example.quanlykhachsanjava.model.Booking;
import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.service.BookingService;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.RoomService;
import com.example.quanlykhachsanjava.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userService.findAll().isEmpty()) {
            User user = new User(null, "testuser", passwordEncoder.encode("password123"), "ROLE_USER", null);
            userService.save(user);

            User admin = new User(null, "admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN", null);
            userService.save(admin);

            RoomCategory cat1 = new RoomCategory(null, "Standard", 500000.0, null);
            RoomCategory cat2 = new RoomCategory(null, "VIP", 1500000.0, null);
            cat1 = categoryService.save(cat1);
            cat2 = categoryService.save(cat2);

            Room room1 = new Room(null, "101", "AVAILABLE", cat1, null);
            Room room2 = new Room(null, "102", "AVAILABLE", cat1, null);
            Room room3 = new Room(null, "103", "AVAILABLE", cat1, null);
            Room room4 = new Room(null, "201", "AVAILABLE", cat2, null);
            Room room5 = new Room(null, "202", "AVAILABLE", cat2, null);

            room1 = roomService.save(room1);
            room2 = roomService.save(room2);
            roomService.save(room3);
            roomService.save(room4);
            roomService.save(room5);

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRoom(room1);
            booking.setCheckInDate(LocalDate.now().plusDays(1));
            booking.setCheckOutDate(LocalDate.now().plusDays(3));
            bookingService.createBooking(booking);

            System.out.println("========== TEST ĐẶT TRÙNG NGÀY ==========");
            try {
                Booking overlapping = new Booking();
                overlapping.setUser(user);
                overlapping.setRoom(room1);
                overlapping.setCheckInDate(LocalDate.now().plusDays(2));
                overlapping.setCheckOutDate(LocalDate.now().plusDays(4));
                bookingService.createBooking(overlapping);
            } catch (Exception e) {
                System.out.println("Lỗi dự kiến đã bị bắt: " + e.getMessage());
            }

            System.out.println("========== TEST TÌM PHÒNG TRỐNG ==========");
            LocalDate checkIn = LocalDate.now().plusDays(1);
            LocalDate checkOut = LocalDate.now().plusDays(3);
            List<Room> availableCat1 = roomService.getAvailableRooms(cat1.getId(), checkIn, checkOut);
            System.out.println("Phòng Standard trống (checkin " + checkIn + " to " + checkOut + "): ");
            for (Room r : availableCat1) {
                System.out.println("- Phòng: " + r.getRoomNumber()); // Nên in ra 102 và 103, không in 101
            }
        }
    }
}
