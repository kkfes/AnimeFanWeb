
package com.animefan.controller.api.v1;

import com.animefan.model.Genre;
import com.animefan.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Genres
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Genre management API")
public class GenreApiController {

    private final GenreService genreService;

    @GetMapping
    @Operation(summary = "Get all genres", description = "Get all active genres")
    public ResponseEntity<List<Genre>> getAllGenres() {
        log.info("GET /api/v1/genres");
        return ResponseEntity.ok(genreService.getActiveGenres());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get genre by ID", description = "Get genre details by ID")
    public ResponseEntity<Genre> getGenreById(@PathVariable String id) {
        log.info("GET /api/v1/genres/{}", id);
        return genreService.getGenreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get genre by name", description = "Get genre details by name")
    public ResponseEntity<Genre> getGenreByName(@PathVariable String name) {
        log.info("GET /api/v1/genres/name/{}", name);
        return genreService.getGenreByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
