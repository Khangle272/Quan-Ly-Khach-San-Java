package com.example.quanlykhachsanjava.service;

import com.example.quanlykhachsanjava.dto.DiscountResult;
import com.example.quanlykhachsanjava.model.Coupon;
import com.example.quanlykhachsanjava.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
public class CouponService {

    private static final String DISCOUNT_TYPE_PERCENT = "PERCENT";
    private static final String DISCOUNT_TYPE_FIXED = "FIXED";
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final CouponRepository couponRepository;
    private final Random random = new Random();

    @Autowired
    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon createPercentCoupon(double percent,
                                      Double minOrderAmount,
                                      Integer minNights,
                                      LocalDateTime validFrom,
                                      LocalDateTime validUntil,
                                      Double maxDiscountAmount) {
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("Percent discount must be between 0 and 100");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(generateCode(10));
        coupon.setDiscountType(DISCOUNT_TYPE_PERCENT);
        coupon.setDiscountValue(percent);
        coupon.setMinOrderAmount(minOrderAmount);
        coupon.setMinNights(minNights);
        coupon.setValidFrom(validFrom);
        coupon.setValidUntil(validUntil);
        coupon.setMaxDiscountAmount(maxDiscountAmount);
        coupon.setIsActive(true);
        return couponRepository.save(coupon);
    }

    public Coupon createFixedAmountCoupon(double amount,
                                          Double minOrderAmount,
                                          Integer minNights,
                                          LocalDateTime validFrom,
                                          LocalDateTime validUntil) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Discount amount must be greater than 0");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(generateCode(10));
        coupon.setDiscountType(DISCOUNT_TYPE_FIXED);
        coupon.setDiscountValue(amount);
        coupon.setMinOrderAmount(minOrderAmount);
        coupon.setMinNights(minNights);
        coupon.setValidFrom(validFrom);
        coupon.setValidUntil(validUntil);
        coupon.setIsActive(true);
        return couponRepository.save(coupon);
    }

    public Optional<Coupon> findActiveByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return couponRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .filter(Coupon::getIsActive);
    }

    public DiscountResult applyCoupon(Coupon coupon, double originalAmount, long nights) {
        if (originalAmount < 0) {
            throw new IllegalArgumentException("Original amount cannot be negative");
        }

        if (coupon == null) {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "No coupon applied");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "Coupon not started");
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "Coupon expired");
        }
        if (coupon.getIsActive() == null || !coupon.getIsActive()) {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "Coupon inactive");
        }
        if (coupon.getMinOrderAmount() != null && originalAmount < coupon.getMinOrderAmount()) {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "Order amount too low");
        }
        if (coupon.getMinNights() != null && nights < coupon.getMinNights()) {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "Not enough nights");
        }

        double discountAmount;
        if (DISCOUNT_TYPE_PERCENT.equalsIgnoreCase(coupon.getDiscountType())) {
            discountAmount = originalAmount * (coupon.getDiscountValue() / 100.0);
            if (coupon.getMaxDiscountAmount() != null && discountAmount > coupon.getMaxDiscountAmount()) {
                discountAmount = coupon.getMaxDiscountAmount();
            }
        } else if (DISCOUNT_TYPE_FIXED.equalsIgnoreCase(coupon.getDiscountType())) {
            discountAmount = coupon.getDiscountValue();
        } else {
            return new DiscountResult(originalAmount, 0.0, originalAmount, false, "Unknown discount type");
        }

        if (discountAmount < 0) {
            discountAmount = 0;
        }

        double finalAmount = Math.max(0, originalAmount - discountAmount);
        return new DiscountResult(originalAmount, discountAmount, finalAmount, true, "Applied");
    }

    private String generateCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CODE_CHARS.length());
            builder.append(CODE_CHARS.charAt(index));
        }
        return builder.toString();
    }
}

