package com.example.quanlykhachsanjava.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    @Column(nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Min(value = 0, message = "Total amount cannot be negative")
    private Double totalAmount;

    @Min(value = 1, message = "Number of guests must be at least 1")
    private Integer numberOfGuests;

    @Min(value = 1, message = "Room quantity must be at least 1")
    @Column(nullable = false)
    private Integer roomQuantity = 1;

    @Column(nullable = false)
    private String bookingStatus = "PENDING";

    @Column(nullable = false)
    private String paymentStatus = "UNPAID";

    private String paymentMethod;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    @ToString.Exclude
    private Coupon coupon;

    @Column(length = 32)
    private String bookingGroupCode;
}
