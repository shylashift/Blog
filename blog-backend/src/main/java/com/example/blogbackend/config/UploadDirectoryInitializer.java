package com.example.blogbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadDirectoryInitializer implements CommandLineRunner {

    @Autowired
    private UploadConfig uploadConfig;

    @Override
    public void run(String... args) throws Exception {
        Path uploadPath = Paths.get(uploadConfig.getUploadDir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created upload directory: " + uploadPath);
        }
    }
} 