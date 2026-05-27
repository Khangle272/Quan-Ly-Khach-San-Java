package com.example.quanlykhachsanjava.controller;

import com.example.quanlykhachsanjava.model.Room;
import com.example.quanlykhachsanjava.service.CategoryService;
import com.example.quanlykhachsanjava.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/")
    public String home(Model model) {
        List<Room> featuredRooms = roomService.findAll()
                .stream()
                .limit(6)
                .toList();

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("featuredRooms", featuredRooms);

        return "home";
    }
}