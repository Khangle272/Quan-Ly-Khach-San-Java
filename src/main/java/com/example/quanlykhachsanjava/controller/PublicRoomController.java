package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.model.Review;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.ReviewService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PublicRoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/rooms")
    public String rooms(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String sort,
            Model model
    ) {
        List<Room> rooms = roomService.findAll();
        boolean isFiltered = false;
        String selectedCategoryName = null;
        String errorMessage = null;

        boolean hasAnyFilter = categoryId != null || checkIn != null || checkOut != null;

        if (hasAnyFilter) {
            if (categoryId == null) {
                errorMessage = "Vui lòng chọn loại phòng để tìm phòng trống theo ngày.";
            } else if (checkIn == null || checkOut == null) {
                errorMessage = "Vui lòng chọn đầy đủ ngày nhận phòng và ngày trả phòng.";
            } else {
                Optional<RoomCategory> category = categoryService.findById(categoryId);
                if (category.isEmpty()) {
                    errorMessage = "Loại phòng không tồn tại.";
                } else {
                    try {
                        rooms = roomService.getAvailableRooms(categoryId, checkIn, checkOut);
                        isFiltered = true;
                        selectedCategoryName = category.get().getName();
                    } catch (IllegalArgumentException exception) {
                        errorMessage = exception.getMessage();
                    }
                }
            }
        }

        if (errorMessage == null) {
            if (minPrice != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getCategory().getPrice() >= minPrice)
                        .collect(Collectors.toList());
                isFiltered = true;
            }

            if (maxPrice != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getCategory().getPrice() <= maxPrice)
                        .collect(Collectors.toList());
                isFiltered = true;
            }

            if (capacity != null) {
                rooms = rooms.stream()
                        .filter(room -> room.getCategory().getCapacity() >= capacity)
                        .collect(Collectors.toList());
                isFiltered = true;
            }

            if ("priceAsc".equals(sort)) {
                rooms.sort(Comparator.comparing(room -> room.getCategory().getPrice()));
                isFiltered = true;
            } else if ("priceDesc".equals(sort)) {
                rooms.sort(Comparator.comparing((Room room) -> room.getCategory().getPrice()).reversed());
                isFiltered = true;
            }
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCategoryName", selectedCategoryName);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);

        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedCapacity", capacity);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("roomCount", rooms.size());

        model.addAttribute("isFiltered", isFiltered);
        model.addAttribute("errorMessage", errorMessage);
        return "rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Model model
    ) {
        Optional<Room> room = roomService.findById(id);
        if (room.isEmpty()) {
            return "redirect:/rooms";
        }

        model.addAttribute("room", room.get());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        List<Review> reviews = reviewService.findVisibleByRoomId(id);
        double ratingAverage = reviews.isEmpty()
                ? 0.0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("ratingAverage", ratingAverage);
        return "room-detail";
    }
}