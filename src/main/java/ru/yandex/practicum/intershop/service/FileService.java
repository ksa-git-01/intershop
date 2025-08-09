package ru.yandex.practicum.intershop.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    @Value("${storage.image.path}")
    private String imageDir;

    public String saveImage(MultipartFile image) {
        String filename = null;
        if (image != null && !image.isEmpty()) {
            filename = getFilename(image);
            Path uploadPath = Paths.get(imageDir);
            Path filePath = uploadPath.resolve(filename);
            try {
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return filename;
    }

    public Resource getImageResource(String filename) throws IOException {
        Path uploadDir = Paths.get(imageDir).toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(filename).normalize();

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return null;
        }

        return resource;
    }

    public String getImageContentType(String filename) {
        try {
            Path uploadDir = Paths.get(imageDir).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve(filename).normalize();

            String contentType = Files.probeContentType(filePath);
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg";
            }

            return contentType;
        } catch (Exception e) {
            return "image/jpeg";
        }
    }

    private String getFilename(MultipartFile image) {
        String fileExtension = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
        return UUID.randomUUID() + fileExtension;
    }
}
