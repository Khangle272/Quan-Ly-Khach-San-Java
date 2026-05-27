package com.example.quanlykhachsanjava.service;

import com.example.quanlykhachsanjava.model.Review;
import com.example.quanlykhachsanjava.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> findVisibleByRoomId(Long roomId) {
        return reviewRepository.findByRoomIdAndIsVisibleTrueOrderByCreatedAtDesc(roomId);
    }

    public Optional<Review> findByBookingId(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId).stream().findFirst();
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}

