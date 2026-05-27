package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Review;
import com.example.quanlykhachsanjava.service.ReviewService;
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
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/admin/reviews")
    public String listReviews(Model model) {
        List<Review> reviews = reviewService.findAll().stream()
                .sorted(Comparator.comparing(Review::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        model.addAttribute("reviews", reviews);
        return "admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        Optional<Review> reviewOptional = reviewService.findById(id);
        if (reviewOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đánh giá.");
            return "redirect:/admin/reviews";
        }

        Review review = reviewOptional.get();
        review.setIsVisible(true);
        reviewService.save(review);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đánh giá.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đánh giá.");
        }
        return "redirect:/admin/reviews";
    }
}

