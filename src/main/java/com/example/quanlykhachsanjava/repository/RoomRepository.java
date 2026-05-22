package com.example.quanlykhachsanjava.repository;

import com.example.quanlykhachsanjava.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r WHERE r.category.id = :categoryId AND r.status = 'AVAILABLE' AND r.id NOT IN (" +
           "SELECT b.room.id FROM Booking b WHERE b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)")
    List<Room> findAvailableRoomsByCategoryAndDates(@Param("categoryId") Long categoryId,
                                                    @Param("checkIn") LocalDate checkIn,
                                                    @Param("checkOut") LocalDate checkOut);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.category.id = :categoryId AND r.status = 'AVAILABLE' AND r.id NOT IN (" +
           "SELECT b.room.id FROM Booking b WHERE b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)")
    long countAvailableRoomsByCategoryAndDates(@Param("categoryId") Long categoryId,
                                               @Param("checkIn") LocalDate checkIn,
                                               @Param("checkOut") LocalDate checkOut);

    long countByCategoryIdAndStatus(Long categoryId, String status);
}
