package com.example.quanlykhachsanjava.service;

import com.example.quanlykhachsanjava.model.RoomCategory;
import com.example.quanlykhachsanjava.repository.RoomCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;

    public List<RoomCategory> findAll() {
        return roomCategoryRepository.findAll();
    }

    public Optional<RoomCategory> findById(Long id) {
        return roomCategoryRepository.findById(id);
    }

    public RoomCategory save(RoomCategory category) {
        return roomCategoryRepository.save(category);
    }

    public void delete(Long id) {
        roomCategoryRepository.deleteById(id);
    }
}

