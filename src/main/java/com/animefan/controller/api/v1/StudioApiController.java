package com.animefan.controller.api.v1;

import com.animefan.model.Studio;
import com.animefan.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Studio operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/studios")
@RequiredArgsConstructor
@Tag(name = "Studios", description = "Studio management API")
public class StudioApiController {

    private final StudioService studioService;

    @GetMapping
    @Operation(summary = "Get all studios", description = "Get paginated list of studios")
    public ResponseEntity<Page<Studio>> getAllStudios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/studios - page: {}, size: {}", page, size);
        Page<Studio> studios = studioService.getAllStudios(page, size);
        return ResponseEntity.ok(studios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get studio by ID", description = "Get studio details by ID")
    public ResponseEntity<Studio> getStudioById(@PathVariable String id) {
        log.info("GET /api/v1/studios/{}", id);
        Studio studio = studioService.getStudioById(id);
        return ResponseEntity.ok(studio);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create studio", description = "Create new studio (Admin only)")
    public ResponseEntity<Studio> createStudio(@Valid @RequestBody Studio studio) {
        log.info("POST /api/v1/studios - name: {}", studio.getName());
        Studio created = studioService.createStudio(studio);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update studio", description = "Update studio (Admin only)")
    public ResponseEntity<Studio> updateStudio(
            @PathVariable String id,
            @Valid @RequestBody Studio studio) {

        log.info("PUT /api/v1/studios/{}", id);
        Studio updated = studioService.updateStudio(id, studio);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete studio", description = "Delete studio (Admin only)")
    public ResponseEntity<Void> deleteStudio(@PathVariable String id) {
        log.info("DELETE /api/v1/studios/{}", id);
        studioService.deleteStudio(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search studios", description = "Search studios by name")
    public ResponseEntity<Page<Studio>> searchStudios(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/studios/search - query: {}", q);
        Page<Studio> studios = studioService.searchStudios(q, page, size);
        return ResponseEntity.ok(studios);
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Get studios by country", description = "Get studios from specific country")
    public ResponseEntity<Page<Studio>> getStudiosByCountry(
            @PathVariable String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/studios/country/{}", country);
        Page<Studio> studios = studioService.getStudiosByCountry(country, page, size);
        return ResponseEntity.ok(studios);
    }

    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Get list of all countries with studios")
    public ResponseEntity<List<String>> getAllCountries() {
        log.info("GET /api/v1/studios/countries");
        List<String> countries = studioService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/top")
    @Operation(summary = "Get top studios", description = "Get studios with most anime")
    public ResponseEntity<List<Studio>> getTopStudios(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/v1/studios/top - limit: {}", limit);
        List<Studio> studios = studioService.getTopStudios(limit);
        return ResponseEntity.ok(studios);
    }
}
