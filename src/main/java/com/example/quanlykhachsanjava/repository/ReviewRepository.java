package com.example.quanlykhachsanjava.repository;

import com.example.quanlykhachsanjava.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRoomId(Long roomId);

    List<Review> findByRoomIdAndIsVisibleTrueOrderByCreatedAtDesc(Long roomId);

    List<Review> findByBookingId(Long bookingId);
}

