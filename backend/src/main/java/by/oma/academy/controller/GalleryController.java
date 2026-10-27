package by.oma.academy.controller;

import by.oma.academy.dto.CreateGalleryPostRequest;
import by.oma.academy.dto.GalleryPostDto;
import by.oma.academy.service.AuthHelper;
import by.oma.academy.service.GalleryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final GalleryService galleryService;
    private final AuthHelper authHelper;

    public GalleryController(GalleryService galleryService, AuthHelper authHelper) {
        this.galleryService = galleryService;
        this.authHelper = authHelper;
    }

    @GetMapping
    public List<GalleryPostDto> listPosts() {
        return galleryService.listPosts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GalleryPostDto createPost(
            @Valid @RequestBody CreateGalleryPostRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authHelper.requireUserId(authHeader);
        return galleryService.createPost(userId, request);
    }
}
