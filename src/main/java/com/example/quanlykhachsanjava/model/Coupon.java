package com.example.quanlykhachsanjava.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code cannot be blank")
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @NotBlank(message = "Discount type is required")
    @Column(nullable = false, length = 16)
    private String discountType;

    @Min(value = 0, message = "Discount value must be non-negative")
    @Column(nullable = false)
    private Double discountValue;

    private Double maxDiscountAmount;

    private Double minOrderAmount;

    private Integer minNights;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "coupon")
    @ToString.Exclude
    private List<Booking> bookings;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
