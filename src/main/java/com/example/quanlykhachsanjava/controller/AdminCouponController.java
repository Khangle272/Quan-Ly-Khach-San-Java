package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Coupon;
import com.example.quanlykhachsanjava.repository.CouponRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

@Controller
public class AdminCouponController {

    private static final String DISCOUNT_TYPE_PERCENT = "PERCENT";
    private static final String DISCOUNT_TYPE_FIXED = "FIXED";

    private final CouponRepository couponRepository;

    public AdminCouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping("/admin/coupons")
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponRepository.findAll());
        return "admin/coupons";
    }

    @GetMapping("/admin/coupons/create")
    public String createCouponForm(Model model) {
        Coupon coupon = new Coupon();
        coupon.setDiscountType(DISCOUNT_TYPE_PERCENT);
        coupon.setIsActive(true);

        prepareForm(model, coupon, "Thêm mã giảm giá", "Tạo mã giảm giá");
        return "admin/coupon-form";
    }

    @GetMapping("/admin/coupons/edit/{id}")
    public String editCouponForm(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Optional<Coupon> couponOptional = couponRepository.findById(id);
        if (couponOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã giảm giá cần chỉnh sửa.");
            return "redirect:/admin/coupons";
        }

        prepareForm(model, couponOptional.get(), "Cập nhật mã giảm giá", "Lưu thay đổi");
        return "admin/coupon-form";
    }

    @PostMapping("/admin/coupons/save")
    public String saveCoupon(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Double discountValue,
            @RequestParam(required = false) Double maxDiscountAmount,
            @RequestParam(required = false) Double minOrderAmount,
            @RequestParam(required = false) Integer minNights,
            @RequestParam(required = false) String validFrom,
            @RequestParam(required = false) String validUntil,
            @RequestParam(required = false) Boolean isActive,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String safeCode = clean(code).toUpperCase(Locale.ROOT);
        String safeType = clean(discountType).toUpperCase(Locale.ROOT);

        Coupon coupon = id != null
                ? couponRepository.findById(id).orElse(new Coupon())
                : new Coupon();

        coupon.setId(id);
        coupon.setCode(safeCode);
        coupon.setDiscountType(safeType);
        coupon.setDiscountValue(discountValue);
        coupon.setMaxDiscountAmount(maxDiscountAmount);
        coupon.setMinOrderAmount(minOrderAmount);
        coupon.setMinNights(minNights);
        coupon.setIsActive(isActive != null && isActive);

        LocalDateTime parsedValidFrom = parseDateTime(validFrom, "Ngày bắt đầu", model);
        LocalDateTime parsedValidUntil = parseDateTime(validUntil, "Ngày kết thúc", model);
        coupon.setValidFrom(parsedValidFrom);
        coupon.setValidUntil(parsedValidUntil);

        String pageTitle = id == null ? "Thêm mã giảm giá" : "Cập nhật mã giảm giá";
        String submitText = id == null ? "Tạo mã giảm giá" : "Lưu thay đổi";

        if (model.containsAttribute("errorMessage")) {
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (safeCode.isEmpty()) {
            model.addAttribute("errorMessage", "Mã giảm giá không được để trống.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (safeCode.length() > 32) {
            model.addAttribute("errorMessage", "Mã giảm giá tối đa 32 ký tự.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        Optional<Coupon> existingByCode = couponRepository.findByCode(safeCode);
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
            model.addAttribute("errorMessage", "Mã giảm giá đã tồn tại.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (!DISCOUNT_TYPE_PERCENT.equals(safeType) && !DISCOUNT_TYPE_FIXED.equals(safeType)) {
            model.addAttribute("errorMessage", "Loại giảm giá không hợp lệ.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (discountValue == null) {
            model.addAttribute("errorMessage", "Vui lòng nhập giá trị giảm.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (DISCOUNT_TYPE_PERCENT.equals(safeType)) {
            if (discountValue <= 0 || discountValue > 100) {
                model.addAttribute("errorMessage", "Giảm theo % phải trong khoảng 1-100.");
                prepareForm(model, coupon, pageTitle, submitText);
                return "admin/coupon-form";
            }
            if (maxDiscountAmount != null && maxDiscountAmount < 0) {
                model.addAttribute("errorMessage", "Giảm tối đa không hợp lệ.");
                prepareForm(model, coupon, pageTitle, submitText);
                return "admin/coupon-form";
            }
        } else {
            if (discountValue <= 0) {
                model.addAttribute("errorMessage", "Giảm cố định phải lớn hơn 0.");
                prepareForm(model, coupon, pageTitle, submitText);
                return "admin/coupon-form";
            }
            coupon.setMaxDiscountAmount(null);
        }

        if (minOrderAmount != null && minOrderAmount < 0) {
            model.addAttribute("errorMessage", "Đơn tối thiểu không hợp lệ.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (minNights != null && minNights < 1) {
            model.addAttribute("errorMessage", "Số đêm tối thiểu không hợp lệ.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        if (parsedValidFrom != null && parsedValidUntil != null && parsedValidFrom.isAfter(parsedValidUntil)) {
            model.addAttribute("errorMessage", "Ngày bắt đầu phải trước ngày kết thúc.");
            prepareForm(model, coupon, pageTitle, submitText);
            return "admin/coupon-form";
        }

        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu mã giảm giá thành công.");

        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            couponRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa mã giảm giá này vì đang được sử dụng."
            );
        }

        return "redirect:/admin/coupons";
    }

    private void prepareForm(Model model, Coupon coupon, String pageTitle, String submitText) {
        model.addAttribute("coupon", coupon);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("submitText", submitText);
    }

    private LocalDateTime parseDateTime(String value, String label, Model model) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            model.addAttribute("errorMessage", label + " không hợp lệ.");
            return null;
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

