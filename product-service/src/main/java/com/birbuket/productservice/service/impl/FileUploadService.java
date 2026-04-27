package com.birbuket.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.region:eu-north-1}")
    private String region;

    /** false olanda və ya bucket boş/placeholder olanda fayllar lokal `uploads/` altına yazılır */
    @Value("${aws.s3.enabled:true}")
    private boolean awsS3Enabled;

    /**
     * S3 "Block public access" aktiv olanda ACL ilə upload tez-tez uğursuz olur.
     * Default: ACL yoxdur; bucket policy ilə public read verə bilərsiniz.
     */
    @Value("${aws.s3.use-public-acl:false}")
    private boolean usePublicAcl;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** Boş deyilsə, lokal fayl üçün bu host ilə tam URL qaytarılır (məs: http://localhost:8083/uploads/...) */
    @Value("${app.media.public-base-url:}")
    private String publicBaseUrl;

    public String uploadFile(MultipartFile file, String folder) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("File boş ola bilməz");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Yalnız şəkil upload etmək olar");
        }

        if (!useS3()) {
            return uploadLocal(file, folder);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + "." + extension;

        PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType());
        if (usePublicAcl) {
            putBuilder.acl(ObjectCannedACL.PUBLIC_READ);
        }
        PutObjectRequest putObjectRequest = putBuilder.build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    private boolean useS3() {
        if (!awsS3Enabled) {
            return false;
        }
        if (bucketName == null || bucketName.isBlank()) {
            return false;
        }
        return !"your-bucket-name".equalsIgnoreCase(bucketName.trim());
    }

    private String uploadLocal(MultipartFile file, String folder) throws IOException {
        Path uploadPath = Paths.get(uploadDir, folder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        String relative = "/uploads/" + folder + "/" + fileName;
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + relative;
        }
        return relative;
    }

    private String getFileExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public List<String> uploadMultipartFiles(List<MultipartFile> files, String folder) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null) {
            return urls;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            urls.add(uploadFile(file, folder));
        }
        return urls;
    }
}