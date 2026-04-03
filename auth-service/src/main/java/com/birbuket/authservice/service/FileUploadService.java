package com.birbuketuserservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private final String uploadDir = "uploads/";

    public String uploadFile(MultipartFile file, String folder) throws IOException {

        var uploadPath = Paths.get(uploadDir + folder);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        var fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        var filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + folder + "/" + fileName;
    }
}
