package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.FileStorageService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminRoomController {

    private final RoomService roomService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public AdminRoomController(RoomService roomService,
                               CategoryService categoryService,
                               FileStorageService fileStorageService) {
        this.roomService = roomService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/admin/rooms")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/rooms";
    }

    @GetMapping("/admin/rooms/create")
    public String createRoomForm(Model model) {
        Room room = new Room();

        model.addAttribute("room", room);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Thêm phòng");
        model.addAttribute("submitText", "Thêm phòng");

        return "admin/room-form";
    }

    @GetMapping("/admin/rooms/edit/{id}")
    public String editRoomForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Room> roomOptional = roomService.findById(id);

        if (roomOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng cần chỉnh sửa.");
            return "redirect:/admin/rooms";
        }

        model.addAttribute("room", roomOptional.get());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Cập nhật phòng");
        model.addAttribute("submitText", "Lưu thay đổi");

        return "admin/room-form";
    }

    @PostMapping("/admin/rooms/save")
    public String saveRoom(
            @RequestParam(required = false) Long id,
            @RequestParam String roomNumber,
            @RequestParam Integer floor,
            @RequestParam String status,
            @RequestParam Long categoryId,
            @RequestParam(required = false) MultipartFile[] imageFiles,
            RedirectAttributes redirectAttributes
    ) {
        String safeRoomNumber = roomNumber == null ? "" : roomNumber.trim();
        if (safeRoomNumber.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số phòng không được để trống.");
            return "redirect:/admin/rooms";
        }
        if (floor == null || floor < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tầng phòng không hợp lệ.");
            return "redirect:/admin/rooms";
        }

        Optional<RoomCategory> categoryOptional = categoryService.findById(categoryId);

        if (categoryOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Loại phòng không hợp lệ.");
            return "redirect:/admin/rooms";
        }

        Room room;

        if (id != null) {
            room = roomService.findById(id).orElse(new Room());
        } else {
            room = new Room();
        }

        room.setRoomNumber(safeRoomNumber);
        room.setFloor(floor);
        room.setStatus(status);
        room.setCategory(categoryOptional.get());

        try {
            List<String> imagePaths = fileStorageService.storeAll(imageFiles);
            if (!imagePaths.isEmpty()) {
                room.setImagePaths(String.join(",", imagePaths));
            }
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu ảnh phòng.");
            return "redirect:/admin/rooms";
        }

        roomService.save(room);

        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin phòng thành công.");

        return "redirect:/admin/rooms";
    }

    @GetMapping("/admin/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            roomService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phòng thành công.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa phòng này vì có thể đang liên kết với dữ liệu đặt phòng."
            );
        }

        return "redirect:/admin/rooms";
    }
}