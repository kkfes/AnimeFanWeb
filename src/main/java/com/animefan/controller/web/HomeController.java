package com.animefan.controller.web;

import com.animefan.dto.AnimeSearchDTO;
import com.animefan.model.Anime;
import com.animefan.model.Banner;
import com.animefan.model.Genre;
import com.animefan.model.User;
import com.animefan.service.AnimeService;
import com.animefan.service.BannerService;
import com.animefan.service.GenreService;
import com.animefan.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web Controller for Home and main pages
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AnimeService animeService;
    private final StatsService statsService;
    private final BannerService bannerService;
    private final GenreService genreService;

    /**
     * Handle favicon.ico requests to prevent 404 errors
     */
    @GetMapping("/favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, @AuthenticationPrincipal User user) {
        log.info("GET / - Home page");

        // Get banners for slider
        List<Banner> banners = bannerService.getActiveBanners();

        // Get featured content
        List<Anime> topAnime = animeService.getTopAnime(10);
        Page<Anime> recentAnime = animeService.getRecentAnime(0, 12);

        // Get genres with banners
        List<Genre> genresWithBanners = genreService.getActiveGenres();

        model.addAttribute("banners", banners);
        model.addAttribute("topAnime", topAnime);
        model.addAttribute("recentAnime", recentAnime.getContent());
        model.addAttribute("genres", genresWithBanners);
        model.addAttribute("currentUser", user);

        return "home";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) Double ratingFrom,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "rating") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /search - query: {}", q);

        AnimeSearchDTO searchDTO = AnimeSearchDTO.builder()
                .query(q)
                .genres(genres)
                .yearFrom(yearFrom)
                .yearTo(yearTo)
                .ratingFrom(ratingFrom)
                .status(status)
                .type(type)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        Page<Anime> results = animeService.searchAnime(searchDTO);
        List<String> allGenres = animeService.getAllGenres();

        model.addAttribute("animeList", results);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("allGenres", allGenres);
        model.addAttribute("currentUser", user);
        model.addAttribute("query", q);

        return "search";
    }

    @GetMapping("/genre/{genre}")
    public String browseByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /genre/{}", genre);

        Page<Anime> animeList = animeService.getAnimeByGenre(genre, page, size);
        List<String> allGenres = animeService.getAllGenres();

        model.addAttribute("animeList", animeList);
        model.addAttribute("currentGenre", genre);
        model.addAttribute("allGenres", allGenres);
        model.addAttribute("currentUser", user);

        return "genre";
    }

    @GetMapping("/top")
    public String topAnime(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /top");

        List<Anime> topAnime = animeService.getTopAnime(50);

        model.addAttribute("topAnime", topAnime);
        model.addAttribute("currentUser", user);

        return "top";
    }

    @GetMapping("/genres")
    public String allGenres(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /genres");

        List<Genre> genres = genreService.getActiveGenres();

        model.addAttribute("genres", genres);
        model.addAttribute("currentUser", user);

        return "genres";
    }

    @GetMapping("/about")
    public String about(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /about");

        StatsService.PlatformStats stats = statsService.getPlatformStats();

        model.addAttribute("stats", stats);
        model.addAttribute("currentUser", user);

        return "about";
    }

    @GetMapping("/status")
    public String status(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /status");

        // Get platform stats
        StatsService.PlatformStats stats = statsService.getPlatformStats();

        // Check services status
        boolean mongoStatus = checkMongoConnection();

        model.addAttribute("stats", stats);
        model.addAttribute("mongoStatus", mongoStatus);
        model.addAttribute("serverTime", java.time.LocalDateTime.now());
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        model.addAttribute("osName", System.getProperty("os.name"));
        model.addAttribute("currentUser", user);

        return "status";
    }

    private boolean checkMongoConnection() {
        try {
            // Try to get stats - if it works, MongoDB is connected
            statsService.getPlatformStats();
            return true;
        } catch (Exception e) {
            log.error("MongoDB connection check failed", e);
            return false;
        }
    }
}
