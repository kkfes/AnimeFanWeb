package com.animefan.controller.web;

import com.animefan.dto.AnimeDTO;
import com.animefan.dto.BulkImportResultDTO;
import com.animefan.model.*;
import com.animefan.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web Controller for Admin pages
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AnimeService animeService;
    private final StudioService studioService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final StatsService statsService;
    private final BannerService bannerService;
    private final GenreService genreService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String adminDashboard(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin - Dashboard");

        StatsService.PlatformStats stats = statsService.getPlatformStats();

        model.addAttribute("stats", stats);
        model.addAttribute("currentUser", user);

        return "admin/dashboard";
    }

    // Anime Management

    @GetMapping("/anime")
    public String animeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/anime");

        Page<Anime> animeList = animeService.getAllAnime(page, size, "createdAt", "desc");

        model.addAttribute("animeList", animeList);
        model.addAttribute("currentUser", user);

        return "admin/anime/list";
    }

    @GetMapping("/anime/new")
    public String newAnimeForm(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin/anime/new");

        List<Studio> studios = studioService.getAllStudios(0, 100).getContent();
        List<String> genres = animeService.getAllGenres();

        model.addAttribute("animeDTO", new AnimeDTO());
        model.addAttribute("studios", studios);
        model.addAttribute("genres", genres);
        model.addAttribute("currentUser", user);

        return "admin/anime/form";
    }

    @PostMapping("/anime/new")
    public String createAnime(
            @Valid @ModelAttribute("animeDTO") AnimeDTO animeDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("POST /admin/anime/new");

        if (bindingResult.hasErrors()) {
            List<Studio> studios = studioService.getAllStudios(0, 100).getContent();
            List<String> genres = animeService.getAllGenres();
            model.addAttribute("studios", studios);
            model.addAttribute("genres", genres);
            model.addAttribute("currentUser", user);
            return "admin/anime/form";
        }

        try {
            Anime anime = animeService.createAnime(animeDTO);
            // Update genre counts
            if (animeDTO.getGenres() != null) {
                genreService.updateGenreCounts(animeDTO.getGenres());
            }
            redirectAttributes.addFlashAttribute("success", "Аниме успешно создано");
            return "redirect:/admin/anime";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            List<Studio> studios = studioService.getAllStudios(0, 100).getContent();
            List<String> genres = animeService.getAllGenres();
            model.addAttribute("studios", studios);
            model.addAttribute("genres", genres);
            model.addAttribute("currentUser", user);
            return "admin/anime/form";
        }
    }

    // Bulk Import

    @GetMapping("/anime/bulk-import")
    public String bulkImportForm(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin/anime/bulk-import");
        model.addAttribute("currentUser", user);
        return "admin/anime/bulk-import";
    }

    @PostMapping("/anime/bulk-import/json")
    public String bulkImportJson(
            @RequestParam String jsonData,
            @RequestParam(required = false, defaultValue = "true") boolean skipDuplicates,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user) {

        log.info("POST /admin/anime/bulk-import/json");

        BulkImportResultDTO result = new BulkImportResultDTO();

        try {
            List<AnimeDTO> animeList = objectMapper.readValue(jsonData, new TypeReference<List<AnimeDTO>>() {});

            for (AnimeDTO animeDTO : animeList) {
                try {
                    // Validate required fields
                    if (animeDTO.getTitle() == null || animeDTO.getTitle().isBlank()) {
                        result.addError("Пропущено аниме без названия");
                        continue;
                    }
                    if (animeDTO.getDescription() == null || animeDTO.getDescription().length() < 10) {
                        result.addError("\"" + animeDTO.getTitle() + "\": описание должно быть не менее 10 символов");
                        continue;
                    }
                    if (animeDTO.getGenres() == null || animeDTO.getGenres().isEmpty()) {
                        result.addError("\"" + animeDTO.getTitle() + "\": укажите хотя бы один жанр");
                        continue;
                    }

                    // Check for duplicates
                    if (skipDuplicates && animeService.existsByTitle(animeDTO.getTitle())) {
                        result.incrementSkipped();
                        continue;
                    }

                    // Set defaults
                    if (animeDTO.getType() == null) animeDTO.setType("TV");
                    if (animeDTO.getStatus() == null) animeDTO.setStatus("ONGOING");

                    animeService.createAnime(animeDTO);
                    if (animeDTO.getGenres() != null) {
                        genreService.updateGenreCounts(animeDTO.getGenres());
                    }
                    result.addSuccess(animeDTO.getTitle());

                } catch (Exception e) {
                    result.addError("\"" + animeDTO.getTitle() + "\": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error parsing JSON for bulk import", e);
            result.addError("Ошибка парсинга JSON: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("importResults", result);
        return "redirect:/admin/anime/bulk-import";
    }

    @PostMapping("/anime/bulk-import/quick")
    public String bulkImportQuick(
            @RequestParam String quickData,
            @RequestParam(required = false, defaultValue = "true") boolean skipDuplicates,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user) {

        log.info("POST /admin/anime/bulk-import/quick");

        BulkImportResultDTO result = new BulkImportResultDTO();

        String[] lines = quickData.split("\\r?\\n");

        for (String line : lines) {
            if (line.isBlank()) continue;

            try {
                String[] parts = line.split("\\|");
                if (parts.length < 3) {
                    result.addError("Неверный формат строки: " + line.substring(0, Math.min(50, line.length())));
                    continue;
                }

                String title = parts[0].trim();
                String description = parts[1].trim();
                List<String> genres = Arrays.stream(parts[2].split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                if (title.isBlank()) {
                    result.addError("Пустое название в строке");
                    continue;
                }
                if (description.length() < 10) {
                    result.addError("\"" + title + "\": описание должно быть не менее 10 символов");
                    continue;
                }
                if (genres.isEmpty()) {
                    result.addError("\"" + title + "\": укажите хотя бы один жанр");
                    continue;
                }

                // Check for duplicates
                if (skipDuplicates && animeService.existsByTitle(title)) {
                    result.incrementSkipped();
                    continue;
                }

                AnimeDTO animeDTO = AnimeDTO.builder()
                        .title(title)
                        .description(description)
                        .genres(genres)
                        .releaseYear(parts.length > 3 ? parseIntSafe(parts[3].trim()) : null)
                        .type(parts.length > 4 ? parts[4].trim().toUpperCase() : "TV")
                        .status(parts.length > 5 ? parts[5].trim().toUpperCase() : "ONGOING")
                        .build();

                animeService.createAnime(animeDTO);
                if (animeDTO.getGenres() != null) {
                    genreService.updateGenreCounts(animeDTO.getGenres());
                }
                result.addSuccess(title);

            } catch (Exception e) {
                result.addError("Ошибка в строке: " + e.getMessage());
            }
        }

        redirectAttributes.addFlashAttribute("importResults", result);
        return "redirect:/admin/anime/bulk-import";
    }

    private Integer parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/anime/{id}/edit")
    public String editAnimeForm(
            @PathVariable String id,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/anime/{}/edit", id);

        Anime anime = animeService.getAnimeById(id);
        AnimeDTO animeDTO = AnimeDTO.fromEntity(anime);
        List<Studio> studios = studioService.getAllStudios(0, 100).getContent();
        List<String> genres = animeService.getAllGenres();

        model.addAttribute("animeDTO", animeDTO);
        model.addAttribute("anime", anime);
        model.addAttribute("studios", studios);
        model.addAttribute("genres", genres);
        model.addAttribute("currentUser", user);
        model.addAttribute("isEdit", true);

        return "admin/anime/form";
    }

    @PostMapping("/anime/{id}/edit")
    public String updateAnime(
            @PathVariable String id,
            @Valid @ModelAttribute("animeDTO") AnimeDTO animeDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("POST /admin/anime/{}/edit", id);

        if (bindingResult.hasErrors()) {
            List<Studio> studios = studioService.getAllStudios(0, 100).getContent();
            List<String> genres = animeService.getAllGenres();
            model.addAttribute("studios", studios);
            model.addAttribute("genres", genres);
            model.addAttribute("currentUser", user);
            model.addAttribute("isEdit", true);
            return "admin/anime/form";
        }

        try {
            animeService.updateAnime(id, animeDTO);
            // Update genre counts
            if (animeDTO.getGenres() != null) {
                genreService.updateGenreCounts(animeDTO.getGenres());
            }
            redirectAttributes.addFlashAttribute("success", "Аниме успешно обновлено");
            return "redirect:/admin/anime";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            List<Studio> studios = studioService.getAllStudios(0, 100).getContent();
            List<String> genres = animeService.getAllGenres();
            model.addAttribute("studios", studios);
            model.addAttribute("genres", genres);
            model.addAttribute("currentUser", user);
            model.addAttribute("isEdit", true);
            return "admin/anime/form";
        }
    }

    @PostMapping("/anime/{id}/delete")
    public String deleteAnime(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/anime/{}/delete", id);

        try {
            animeService.deleteAnime(id);
            redirectAttributes.addFlashAttribute("success", "Аниме успешно удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/anime";
    }

    // Studio Management

    @GetMapping("/studios")
    public String studioList(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/studios");

        Page<Studio> studios = studioService.getAllStudios(page, 20);

        model.addAttribute("studios", studios);
        model.addAttribute("currentUser", user);

        return "admin/studios/list";
    }

    @GetMapping("/studios/new")
    public String newStudioForm(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin/studios/new");

        model.addAttribute("studio", new Studio());
        model.addAttribute("currentUser", user);

        return "admin/studios/form";
    }

    @PostMapping("/studios/new")
    public String createStudio(
            @Valid @ModelAttribute("studio") Studio studio,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("POST /admin/studios/new");

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            return "admin/studios/form";
        }

        try {
            studioService.createStudio(studio);
            redirectAttributes.addFlashAttribute("success", "Студия успешно создана");
            return "redirect:/admin/studios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", user);
            return "admin/studios/form";
        }
    }

    // User Management

    @GetMapping("/users")
    public String userList(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/users");

        Page<User> users = userService.getAllUsers(page, 20);

        model.addAttribute("users", users);
        model.addAttribute("currentUser", user);

        return "admin/users/list";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(
            @PathVariable String id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/users/{}/role - {}", id, role);

        try {
            userService.updateUserRole(id, User.Role.valueOf(role));
            redirectAttributes.addFlashAttribute("success", "Роль пользователя обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserEnabled(
            @PathVariable String id,
            @RequestParam boolean enabled,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/users/{}/toggle - {}", id, enabled);

        try {
            userService.setUserEnabled(id, enabled);
            redirectAttributes.addFlashAttribute("success",
                    enabled ? "Пользователь активирован" : "Пользователь деактивирован");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Episode Management

    @GetMapping("/anime/{id}/episodes")
    public String episodeList(
            @PathVariable String id,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/anime/{}/episodes", id);

        Anime anime = animeService.getAnimeById(id);

        model.addAttribute("anime", anime);
        model.addAttribute("currentUser", user);

        return "admin/anime/episodes";
    }

    @PostMapping("/anime/{id}/episodes/add")
    public String addEpisode(
            @PathVariable String id,
            @RequestParam int number,
            @RequestParam String title,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String thumbnailUrl,
            @RequestParam(required = false, defaultValue = "24") int duration,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/anime/{}/episodes/add - Episode {}", id, number);

        try {
            Anime.Episode episode = Anime.Episode.builder()
                    .number(number)
                    .title(title)
                    .videoUrl(videoUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .duration(duration)
                    .build();

            animeService.addEpisode(id, episode);
            redirectAttributes.addFlashAttribute("success", "Эпизод " + number + " успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/anime/" + id + "/episodes";
    }

    @PostMapping("/anime/{id}/episodes/{episodeNumber}/update")
    public String updateEpisode(
            @PathVariable String id,
            @PathVariable int episodeNumber,
            @RequestParam String title,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String thumbnailUrl,
            @RequestParam(required = false, defaultValue = "24") int duration,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/anime/{}/episodes/{}/update", id, episodeNumber);

        try {
            animeService.updateEpisode(id, episodeNumber, title, videoUrl, thumbnailUrl, duration);
            redirectAttributes.addFlashAttribute("success", "Эпизод " + episodeNumber + " обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/anime/" + id + "/episodes";
    }

    @PostMapping("/anime/{id}/episodes/{episodeNumber}/delete")
    public String deleteEpisode(
            @PathVariable String id,
            @PathVariable int episodeNumber,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/anime/{}/episodes/{}/delete", id, episodeNumber);

        try {
            animeService.deleteEpisode(id, episodeNumber);
            redirectAttributes.addFlashAttribute("success", "Эпизод " + episodeNumber + " удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/anime/" + id + "/episodes";
    }

    // Related Anime Management

    @GetMapping("/anime/{id}/related")
    public String relatedAnimeList(
            @PathVariable String id,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/anime/{}/related", id);

        Anime anime = animeService.getAnimeById(id);

        model.addAttribute("anime", anime);
        model.addAttribute("relationTypes", Anime.RelatedAnime.RelationType.values());
        model.addAttribute("currentUser", user);

        return "admin/anime/related";
    }

    @GetMapping("/anime/search-for-relation")
    @ResponseBody
    public List<java.util.Map<String, String>> searchAnimeForRelation(
            @RequestParam String query,
            @RequestParam(required = false) String excludeId) {

        List<Anime> results = animeService.searchByTitle(query, 10);

        return results.stream()
                .filter(a -> excludeId == null || !a.getId().equals(excludeId))
                .map(a -> {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("id", a.getId());
                    map.put("title", a.getTitle());
                    map.put("posterUrl", a.getPosterUrl() != null ? a.getPosterUrl() : "");
                    map.put("year", a.getReleaseYear() != null ? a.getReleaseYear().toString() : "");
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/anime/{id}/related/add")
    public String addRelatedAnime(
            @PathVariable String id,
            @RequestParam String relatedAnimeId,
            @RequestParam String relationType,
            @RequestParam(required = false) Integer seasonNumber,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/anime/{}/related/add - {} as {}", id, relatedAnimeId, relationType);

        try {
            Anime.RelatedAnime.RelationType type = Anime.RelatedAnime.RelationType.valueOf(relationType);
            animeService.addRelatedAnime(id, relatedAnimeId, type, seasonNumber);
            redirectAttributes.addFlashAttribute("success", "Связь успешно добавлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/anime/" + id + "/related";
    }

    @PostMapping("/anime/{id}/related/{relatedId}/delete")
    public String deleteRelatedAnime(
            @PathVariable String id,
            @PathVariable String relatedId,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/anime/{}/related/{}/delete", id, relatedId);

        try {
            animeService.removeRelatedAnime(id, relatedId);
            redirectAttributes.addFlashAttribute("success", "Связь удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/anime/" + id + "/related";
    }

    // Reviews Management

    @GetMapping("/reviews")
    public String reviewList(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /admin/reviews");

        var reviews = reviewService.getAllReviews(page, 20);

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentUser", user);

        return "admin/reviews/list";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        log.info("POST /admin/reviews/{}/delete", id);

        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Отзыв удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews";
    }

    // Banner Management

    @GetMapping("/banners")
    public String bannerList(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin/banners");
        model.addAttribute("banners", bannerService.getAllBanners());
        model.addAttribute("currentUser", user);
        return "admin/banners/list";
    }

    @GetMapping("/banners/new")
    public String newBannerForm(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("banner", new Banner());
        model.addAttribute("currentUser", user);
        return "admin/banners/form";
    }

    @PostMapping("/banners/new")
    public String createBanner(@ModelAttribute Banner banner, RedirectAttributes redirectAttributes) {
        try {
            bannerService.createBanner(banner);
            redirectAttributes.addFlashAttribute("success", "Баннер успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @GetMapping("/banners/{id}/edit")
    public String editBannerForm(@PathVariable String id, Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("banner", bannerService.getBannerById(id).orElse(null));
        model.addAttribute("isEdit", true);
        model.addAttribute("currentUser", user);
        return "admin/banners/form";
    }

    @PostMapping("/banners/{id}/edit")
    public String updateBanner(@PathVariable String id, @ModelAttribute Banner banner, RedirectAttributes redirectAttributes) {
        try {
            bannerService.updateBanner(id, banner);
            redirectAttributes.addFlashAttribute("success", "Баннер обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @PostMapping("/banners/{id}/delete")
    public String deleteBanner(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.deleteBanner(id);
            redirectAttributes.addFlashAttribute("success", "Баннер удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @PostMapping("/banners/{id}/toggle")
    public String toggleBanner(@PathVariable String id, @RequestParam boolean active, RedirectAttributes redirectAttributes) {
        bannerService.toggleActive(id, active);
        return "redirect:/admin/banners";
    }

    // Genre Management

    @GetMapping("/genres")
    public String genreList(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin/genres");
        model.addAttribute("genresList", genreService.getAllGenres());
        model.addAttribute("currentUser", user);
        return "admin/genres/list";
    }

    @PostMapping("/genres/recalculate")
    public String recalculateGenreCounts(RedirectAttributes redirectAttributes) {
        log.info("POST /admin/genres/recalculate");
        try {
            genreService.recalculateAllGenreCounts();
            redirectAttributes.addFlashAttribute("success", "Счётчики жанров пересчитаны");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    @GetMapping("/genres/new")
    public String newGenreForm(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("genre", new Genre());
        model.addAttribute("currentUser", user);
        return "admin/genres/form";
    }

    @PostMapping("/genres/new")
    public String createGenre(@ModelAttribute Genre genre, RedirectAttributes redirectAttributes) {
        try {
            genreService.createGenre(genre);
            redirectAttributes.addFlashAttribute("success", "Жанр успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    @GetMapping("/genres/{id}/edit")
    public String editGenreForm(@PathVariable String id, Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("genre", genreService.getGenreById(id).orElse(null));
        model.addAttribute("isEdit", true);
        model.addAttribute("currentUser", user);
        return "admin/genres/form";
    }

    @PostMapping("/genres/{id}/edit")
    public String updateGenre(@PathVariable String id, @ModelAttribute Genre genre, RedirectAttributes redirectAttributes) {
        try {
            genreService.updateGenre(id, genre);
            redirectAttributes.addFlashAttribute("success", "Жанр обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    @PostMapping("/genres/{id}/delete")
    public String deleteGenre(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            genreService.deleteGenre(id);
            redirectAttributes.addFlashAttribute("success", "Жанр удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/genres";
    }

    // Settings

    @GetMapping("/settings")
    public String settings(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /admin/settings");

        model.addAttribute("currentUser", user);

        return "admin/settings";
    }
}
