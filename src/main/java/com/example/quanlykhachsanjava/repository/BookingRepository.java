package com.example.quanlykhachsanjava.repository;

import java.util.List;
import java.util.Optional;
import com.example.quanlykhachsanjava.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    long countOverlappingBookings(@Param("roomId") Long roomId, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);
    List<Booking> findByUserUsernameOrderByIdDesc(String username);

    Optional<Booking> findByIdAndUserUsername(Long id, String username);
}
