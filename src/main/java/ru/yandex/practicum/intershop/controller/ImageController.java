package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.service.FileService;

@Controller
@RequiredArgsConstructor
public class ImageController {
    private final FileService fileService;

    @GetMapping("/images/{filename}")
    @ResponseBody
    public Mono<ResponseEntity<Resource>> getImage(@PathVariable(name = "filename") String filename) {
        return fileService.getImageResource(filename)
                .filter(resource -> resource != null)
                .zipWith(fileService.getImageContentType(filename))
                .map(tuple -> {
                    Resource resource = tuple.getT1();
                    String contentType = tuple.getT2();

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(resource);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
}