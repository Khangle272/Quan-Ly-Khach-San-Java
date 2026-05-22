package com.example.quanlykhachsanjava.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResult {
    private Double originalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private boolean applied;
    private String message;
}

