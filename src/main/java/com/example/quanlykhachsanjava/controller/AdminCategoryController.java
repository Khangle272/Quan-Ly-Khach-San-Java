package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.RoomService;
import com.example.quanlykhachsanjava.service.FileStorageService;
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
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final RoomService roomService;
    private final FileStorageService fileStorageService;

    public AdminCategoryController(CategoryService categoryService,
                                   RoomService roomService,
                                   FileStorageService fileStorageService) {
        this.categoryService = categoryService;
        this.roomService = roomService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/admin/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/admin/categories/create")
    public String createCategoryForm(Model model) {
        RoomCategory category = new RoomCategory();

        model.addAttribute("category", category);
        model.addAttribute("pageTitle", "Thêm loại phòng");
        model.addAttribute("submitText", "Thêm loại phòng");

        return "admin/category-form";
    }

    @GetMapping("/admin/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Optional<RoomCategory> categoryOptional = categoryService.findById(id);

        if (categoryOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy loại phòng cần chỉnh sửa.");
            return "redirect:/admin/categories";
        }

        model.addAttribute("category", categoryOptional.get());
        model.addAttribute("pageTitle", "Cập nhật loại phòng");
        model.addAttribute("submitText", "Lưu thay đổi");

        return "admin/category-form";
    }

    @PostMapping("/admin/categories/save")
    public String saveCategory(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Double price,
            @RequestParam Integer capacity,
            @RequestParam String bedType,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) MultipartFile[] imageFiles,
            RedirectAttributes redirectAttributes
    ) {
        String safeName = name == null ? "" : name.trim();
        String safeBedType = bedType == null ? "" : bedType.trim();
        if (safeName.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên loại phòng không được để trống.");
            return "redirect:/admin/categories";
        }
        if (price == null || price < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giá phòng không hợp lệ.");
            return "redirect:/admin/categories";
        }

        RoomCategory category;

        if (id != null) {
            category = categoryService.findById(id).orElse(new RoomCategory());
        } else {
            category = new RoomCategory();
        }

        category.setName(safeName);
        category.setDescription(clean(description));
        category.setPrice(price);
        category.setCapacity(capacity);
        category.setBedType(safeBedType);
        category.setAmenities(clean(amenities));

        try {
            List<String> imagePaths = fileStorageService.storeAll(imageFiles);
            if (!imagePaths.isEmpty()) {
                category.setImagePath(imagePaths.get(0));
                category.setImagePaths(String.join(",", imagePaths));
            }
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu ảnh phòng.");
            return "redirect:/admin/categories";
        }

        categoryService.save(category);

        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin loại phòng thành công.");

        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        boolean isUsedByRoom = roomService.findAll().stream()
                .map(Room::getCategory)
                .filter(category -> category != null && category.getId() != null)
                .anyMatch(category -> category.getId().equals(id));

        if (isUsedByRoom) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa loại phòng này vì đang có phòng sử dụng."
            );
            return "redirect:/admin/categories";
        }

        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại phòng thành công.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa loại phòng này vì đang liên kết với dữ liệu khác."
            );
        }

        return "redirect:/admin/categories";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}