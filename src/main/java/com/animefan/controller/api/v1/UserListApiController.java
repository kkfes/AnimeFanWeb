package com.animefan.controller.api.v1;

import com.animefan.dto.UserAnimeRelationDTO;
import com.animefan.model.User;
import com.animefan.model.UserAnimeRelation;
import com.animefan.service.UserAnimeRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for User Anime List operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Tag(name = "User Lists", description = "User anime list management API")
public class UserListApiController {

    private final UserAnimeRelationService relationService;

    @GetMapping
    @Operation(summary = "Get my anime list", description = "Get current user's anime list")
    public ResponseEntity<Page<UserAnimeRelationDTO>> getMyList(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/lists - user: {}", user.getUsername());
        Page<UserAnimeRelation> relations = relationService.getUserAnimeList(user.getId(), page, size);
        Page<UserAnimeRelationDTO> dtos = relations.map(UserAnimeRelationDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get list by status", description = "Get anime by status")
    public ResponseEntity<Page<UserAnimeRelationDTO>> getListByStatus(
            @AuthenticationPrincipal User user,
            @PathVariable UserAnimeRelation.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/lists/status/{} - user: {}", status, user.getUsername());
        Page<UserAnimeRelation> relations = relationService.getUserAnimeByStatus(user.getId(), status, page, size);
        Page<UserAnimeRelationDTO> dtos = relations.map(UserAnimeRelationDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get favorites", description = "Get user's favorite anime")
    public ResponseEntity<Page<UserAnimeRelationDTO>> getFavorites(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/lists/favorites - user: {}", user.getUsername());
        Page<UserAnimeRelation> relations = relationService.getUserFavorites(user.getId(), page, size);
        Page<UserAnimeRelationDTO> dtos = relations.map(UserAnimeRelationDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's list", description = "Get specific user's anime list")
    public ResponseEntity<Page<UserAnimeRelationDTO>> getUserList(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/lists/user/{}", userId);
        Page<UserAnimeRelation> relations = relationService.getUserAnimeList(userId, page, size);
        Page<UserAnimeRelationDTO> dtos = relations.map(UserAnimeRelationDTO::fromEntity);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Add to list", description = "Add anime to user's list")
    public ResponseEntity<UserAnimeRelationDTO> addToList(
            @AuthenticationPrincipal User user,
            @RequestBody UserAnimeRelationDTO dto) {

        log.info("POST /api/v1/lists - user: {}, animeId: {}, status: {}",
                user != null ? user.getUsername() : "NULL",
                dto.getAnimeId(),
                dto.getStatus());

        if (user == null) {
            log.error("User is null - not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAnimeRelation relation = relationService.addAnimeToList(user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserAnimeRelationDTO.fromEntity(relation));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update list entry", description = "Update anime list entry")
    public ResponseEntity<UserAnimeRelationDTO> updateListEntry(
            @PathVariable String id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserAnimeRelationDTO dto) {

        log.info("PUT /api/v1/lists/{}", id);
        UserAnimeRelation relation = relationService.updateRelation(id, user.getId(), dto);
        return ResponseEntity.ok(UserAnimeRelationDTO.fromEntity(relation));
    }

    @PutMapping("/{id}/progress")
    @Operation(summary = "Update progress", description = "Update episodes watched")
    public ResponseEntity<Void> updateProgress(
            @PathVariable String id,
            @AuthenticationPrincipal User user,
            @RequestParam int episodesWatched) {

        log.info("PUT /api/v1/lists/{}/progress - episodes: {}", id, episodesWatched);
        relationService.updateProgress(id, user.getId(), episodesWatched);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/anime/{animeId}/favorite")
    @Operation(summary = "Toggle favorite", description = "Toggle anime favorite status")
    public ResponseEntity<Void> toggleFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable String animeId) {

        log.info("POST /api/v1/lists/anime/{}/favorite - user: {}", animeId, user != null ? user.getUsername() : "NULL");

        if (user == null) {
            log.error("User is null - not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        relationService.toggleFavorite(user.getId(), animeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove from list", description = "Remove anime from user's list")
    public ResponseEntity<Void> removeFromList(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {

        log.info("DELETE /api/v1/lists/{}", id);
        relationService.removeFromList(id, user.getId());
        return ResponseEntity.noContent().build();
    }

        @DeleteMapping("/anime/{animeId}")
    @Operation(summary = "Remove anime from list", description = "Remove anime from user's list by anime ID")
    public ResponseEntity<Void> removeAnimeFromList(
            @PathVariable String animeId,
            @AuthenticationPrincipal User user) {

        log.info("DELETE /api/v1/lists/anime/{}", animeId);
        relationService.removeAnimeFromUserList(user.getId(), animeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/anime/{animeId}")
    @Operation(summary = "Get relation for anime", description = "Get user's relation with specific anime")
    public ResponseEntity<UserAnimeRelationDTO> getRelationForAnime(
            @AuthenticationPrincipal User user,
            @PathVariable String animeId) {

        log.info("GET /api/v1/lists/anime/{}", animeId);
        return relationService.getRelation(user.getId(), animeId)
                .map(r -> ResponseEntity.ok(UserAnimeRelationDTO.fromEntity(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/anime/{animeId}/check")
    @Operation(summary = "Check if in list", description = "Check if anime is in user's list")
    public ResponseEntity<ListCheckResponse> checkInList(
            @AuthenticationPrincipal User user,
            @PathVariable String animeId) {

        log.info("GET /api/v1/lists/anime/{}/check", animeId);
        boolean inList = relationService.isInUserList(user.getId(), animeId);
        boolean inFavorites = relationService.isInFavorites(user.getId(), animeId);
        return ResponseEntity.ok(new ListCheckResponse(inList, inFavorites));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ListCheckResponse {
        private boolean inList;
        private boolean inFavorites;
    }
}
