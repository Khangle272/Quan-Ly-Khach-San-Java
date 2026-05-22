package com.example.quanlykhachsanjava.service;

import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> getAvailableRooms(Long categoryId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        return roomRepository.findAvailableRoomsByCategoryAndDates(categoryId, checkIn, checkOut);
    }

    public int countAvailableRooms(Long categoryId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return Math.toIntExact(roomRepository.countByCategoryIdAndStatus(categoryId, "AVAILABLE"));
        }
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        return Math.toIntExact(roomRepository.countAvailableRoomsByCategoryAndDates(categoryId, checkIn, checkOut));
    }
}
