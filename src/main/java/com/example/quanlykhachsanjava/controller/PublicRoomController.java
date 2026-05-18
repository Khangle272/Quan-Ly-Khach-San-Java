package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class PublicRoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/rooms")
    public String rooms(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
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

        model.addAttribute("rooms", rooms);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCategoryName", selectedCategoryName);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
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
        return "room-detail";
    }
}
