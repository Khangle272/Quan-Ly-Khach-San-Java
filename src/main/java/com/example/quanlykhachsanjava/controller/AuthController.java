package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.User;
import com.example.quanlykhachsanjava.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

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

        if (password.length() < 6) {
            model.addAttribute("errorMessage", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "register";
        }

        if (!isBlank(email) && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
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

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam(required = false) String identifier,
            Model model
    ) {
        identifier = clean(identifier);

        if (isBlank(identifier)) {
            model.addAttribute("errorMessage", "Vui lòng nhập email hoặc tên đăng nhập.");
            return "forgot-password";
        }

        Optional<User> userOptional = userRepository.findByUsername(identifier);
        if (userOptional.isEmpty() && isEmail(identifier)) {
            userOptional = userRepository.findByEmail(identifier);
        }

        if (userOptional.isPresent()) {
            model.addAttribute("infoMessage", "Vui lòng chuyển sang trang đặt lại mật khẩu.");
            model.addAttribute("resetUsername", userOptional.get().getUsername());
        } else {
            model.addAttribute("infoMessage", "Nếu tài khoản tồn tại, bạn có thể đặt lại mật khẩu bằng tên đăng nhập.");
        }

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(required = false) String username,
            Model model
    ) {
        model.addAttribute("username", clean(username));
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            Model model
    ) {
        username = clean(username);
        password = clean(password);
        confirmPassword = clean(confirmPassword);

        if (isBlank(username)) {
            model.addAttribute("errorMessage", "Vui lòng nhập tên đăng nhập.");
            model.addAttribute("username", username);
            return "reset-password";
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy tài khoản với tên đăng nhập này.");
            model.addAttribute("username", username);
            return "reset-password";
        }

        if (isBlank(password) || isBlank(confirmPassword)) {
            model.addAttribute("errorMessage", "Vui lòng nhập mật khẩu mới.");
            model.addAttribute("username", username);
            return "reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("username", username);
            return "reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("errorMessage", "Mật khẩu phải có ít nhất 6 ký tự.");
            model.addAttribute("username", username);
            return "reset-password";
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return "redirect:/login?reset";
    }

    private boolean isEmail(String value) {
        return !isBlank(value) && value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}