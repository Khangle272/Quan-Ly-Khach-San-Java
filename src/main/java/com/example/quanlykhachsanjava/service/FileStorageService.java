package com.example.quanlykhachsanjava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();
        String safeName = originalName == null ? "file" : originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = UUID.randomUUID().toString().replace("-", "") + "_" + safeName;

        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(directory);

        Path targetPath = directory.resolve(filename);
        Files.copy(file.getInputStream(), targetPath);

        return "/uploads/" + filename;
    }
}

