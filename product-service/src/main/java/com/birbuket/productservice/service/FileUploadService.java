package com.birbuket.productservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final String uploadDir = "uploads/";

    public String uploadFile(MultipartFile file, String folder) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("File boş ola bilməz");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            throw new RuntimeException("Yalnız şəkil upload etmək olar");
        }

        Path uploadPath = Paths.get(uploadDir, folder);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String extension = getFileExtension(file.getOriginalFilename());

        String fileName = UUID.randomUUID() + "." + extension;

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + folder + "/" + fileName;
    }

    private String getFileExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public List<String> uploadMultipartFiles(MultipartFile[] files, String folder) throws IOException {
        if (files == null || files.length == 0) {
            return List.of();
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                urls.add(uploadFile(file, folder));
            }
        }
        return urls;
    }
}