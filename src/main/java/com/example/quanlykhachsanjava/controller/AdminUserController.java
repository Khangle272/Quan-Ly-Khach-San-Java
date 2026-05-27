package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll().stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản khách.");
            return "redirect:/admin/users";
        }

        User user = userOptional.get();
        if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản quản trị.");
            return "redirect:/admin/users";
        }

        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        userService.save(user);

        String message = Boolean.TRUE.equals(user.getIsActive())
                ? "Đã mở khóa tài khoản khách."
                : "Đã khóa tài khoản khách.";
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/admin/users";
    }
}

