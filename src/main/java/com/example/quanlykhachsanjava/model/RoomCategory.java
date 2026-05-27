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
@Table(name = "room_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Bed type cannot be blank")
    private String bedType;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String amenities;

    private String imagePath;

    @Column(columnDefinition = "TEXT")
    private String imagePaths;

    @Min(value = 0, message = "Price must be positive")
    @Column(nullable = false)
    private Double price;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Room> rooms;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
