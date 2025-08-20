package ru.yandex.practicum.intershop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${storage.image.path}")
    private String imageDir;

    public Mono<String> saveImage(FilePart filePart) {
        if (filePart == null || filePart.filename() == null || filePart.filename().isEmpty()) {
            return Mono.just("");
        }

        String filename = generateFilename(filePart.filename());

        return Mono.fromCallable(() -> {
                    Path uploadPath = Paths.get(imageDir);
                    Path filePath = uploadPath.resolve(filename);

                    try {
                        Files.createDirectories(uploadPath);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create upload directory", e);
                    }

                    return filePath;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(filePath ->
                        filePart.transferTo(filePath)
                                .thenReturn(filename)
                )
                .onErrorMap(throwable -> new RuntimeException("Failed to save file", throwable));
    }

    public Mono<Resource> getImageResource(String filename) {
        return Mono.fromCallable(() -> {
                    Path uploadDir = Paths.get(imageDir).toAbsolutePath().normalize();
                    Path filePath = uploadDir.resolve(filename).normalize();

                    Resource resource = new UrlResource(filePath.toUri());

                    if (!resource.exists() || !resource.isReadable()) {
                        return null;
                    }

                    return resource;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorComplete();
    }

    public Mono<String> getImageContentType(String filename) {
        return Mono.fromCallable(() -> {
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
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String generateFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID() + ".jpg";
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        String fileExtension = lastDotIndex > 0 ?
                originalFilename.substring(lastDotIndex) : ".jpg";

        return UUID.randomUUID() + fileExtension;
    }
}