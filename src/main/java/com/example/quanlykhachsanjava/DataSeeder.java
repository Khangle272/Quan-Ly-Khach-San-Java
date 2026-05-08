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
            User user = new User();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setFullName("Test User");
            user.setEmail("user@example.com");
            user.setPhone("0123456789");
            user.setAddress("Hanoi, Vietnam");
            user.setIsActive(true);
            user.setRole("ROLE_USER");
            userService.save(user);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Admin User");
            admin.setEmail("admin@example.com");
            admin.setPhone("0987654321");
            admin.setAddress("Ho Chi Minh City, Vietnam");
            admin.setIsActive(true);
            admin.setRole("ROLE_ADMIN");
            userService.save(admin);

            RoomCategory cat1 = new RoomCategory();
            cat1.setName("Standard");
            cat1.setPrice(500000.0);
            cat1.setBedType("Single");
            cat1.setCapacity(2);
            cat1.setAmenities("WiFi, AC, TV");
            cat1.setDescription("Phòng tiêu chuẩn cơ bản");

            RoomCategory cat2 = new RoomCategory();
            cat2.setName("VIP");
            cat2.setPrice(1500000.0);
            cat2.setBedType("King");
            cat2.setCapacity(4);
            cat2.setAmenities("WiFi, AC, TV, Minibar, Balcony");
            cat2.setDescription("Phòng VIP cao cấp");

            cat1 = categoryService.save(cat1);
            cat2 = categoryService.save(cat2);

            Room room1 = new Room();
            room1.setRoomNumber("101");
            room1.setFloor(1);
            room1.setStatus("AVAILABLE");
            room1.setCategory(cat1);

            Room room2 = new Room();
            room2.setRoomNumber("102");
            room2.setFloor(1);
            room2.setStatus("AVAILABLE");
            room2.setCategory(cat1);

            Room room3 = new Room();
            room3.setRoomNumber("103");
            room3.setFloor(1);
            room3.setStatus("AVAILABLE");
            room3.setCategory(cat1);

            Room room4 = new Room();
            room4.setRoomNumber("201");
            room4.setFloor(2);
            room4.setStatus("AVAILABLE");
            room4.setCategory(cat2);

            Room room5 = new Room();
            room5.setRoomNumber("202");
            room5.setFloor(2);
            room5.setStatus("AVAILABLE");
            room5.setCategory(cat2);

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
            booking.setNumberOfGuests(2);
            booking.setBookingStatus("CONFIRMED");
            booking.setPaymentStatus("COMPLETED");
            booking.setPaymentMethod("CREDIT_CARD");
            bookingService.createBooking(booking);

            System.out.println("========== TEST ĐẶT TRÙNG NGÀY ==========");
            try {
                Booking overlapping = new Booking();
                overlapping.setUser(user);
                overlapping.setRoom(room1);
                overlapping.setCheckInDate(LocalDate.now().plusDays(2));
                overlapping.setCheckOutDate(LocalDate.now().plusDays(4));
                overlapping.setNumberOfGuests(1);
                overlapping.setBookingStatus("PENDING");
                overlapping.setPaymentStatus("UNPAID");
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
