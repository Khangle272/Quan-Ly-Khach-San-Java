package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            Model model
    ) {
        fullName = clean(fullName);
        username = clean(username);
        email = clean(email);
        phone = clean(phone);
        password = clean(password);
        confirmPassword = clean(confirmPassword);

        keepFormData(model, fullName, username, email, phone);

        if (isBlank(fullName) || isBlank(username) || isBlank(password) || isBlank(confirmPassword)) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin bắt buộc.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            return "register";
        }

        if (!isBlank(email) && !email.contains("@")) {
            model.addAttribute("errorMessage", "Email không hợp lệ.");
            return "register";
        }

        if (!isBlank(phone) && !phone.matches("^\\+?[0-9]{10,15}$")) {
            model.addAttribute("errorMessage", "Số điện thoại không hợp lệ.");
            return "register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("errorMessage", "Tên đăng nhập đã tồn tại.");
            return "register";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(isBlank(email) ? null : email);
        user.setPhone(isBlank(phone) ? null : phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        user.setIsActive(true);

        userRepository.save(user);

        return "redirect:/login?registered";
    }

    private void keepFormData(Model model,
                              String fullName,
                              String username,
                              String email,
                              String phone) {
        model.addAttribute("fullName", fullName);
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}