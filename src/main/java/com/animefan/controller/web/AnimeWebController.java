package com.animefan.controller.web;

import com.animefan.model.Anime;
import com.animefan.model.Review;
import com.animefan.model.User;
import com.animefan.model.UserAnimeRelation;
import com.animefan.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Web Controller for Anime pages
 */
@Slf4j
@Controller
@RequestMapping("/anime")
@RequiredArgsConstructor
public class AnimeWebController {

    private final AnimeService animeService;
    private final ReviewService reviewService;
    private final UserAnimeRelationService relationService;
    private final StatsService statsService;
    private final StudioService studioService;

    @GetMapping
    public String animeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /anime - page: {}", page);

        Page<Anime> animeList = animeService.getAllAnime(page, size, sortBy, sortDirection);
        List<String> genres = animeService.getAllGenres();

        model.addAttribute("animeList", animeList);
        model.addAttribute("genres", genres);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("currentUser", user);

        return "anime/list";
    }

    @GetMapping("/{id}")
    public String animeDetail(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int reviewPage,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /anime/{}", id);

        Anime anime = animeService.getAnimeByIdWithView(id);
        Page<Review> reviews = reviewService.getReviewsForAnime(id, reviewPage, 5);
        StatsService.AnimeStats stats = statsService.getAnimeStats(id);

        model.addAttribute("anime", anime);
        model.addAttribute("reviews", reviews);
        model.addAttribute("stats", stats);
        model.addAttribute("currentUser", user);

        // Check user's relation with this anime
        if (user != null) {
            Optional<UserAnimeRelation> relation = relationService.getRelation(user.getId(), id);
            model.addAttribute("userRelation", relation.orElse(null));
            model.addAttribute("hasReviewed", reviewService.hasUserReviewedAnime(user.getId(), id));

            if (reviewService.hasUserReviewedAnime(user.getId(), id)) {
                Review userReview = reviewService.getUserReviewForAnime(user.getId(), id);
                model.addAttribute("userReview", userReview);
            }
        }

        // Get related anime by genre
        if (anime.getGenres() != null && !anime.getGenres().isEmpty()) {
            Page<Anime> relatedAnime = animeService.getAnimeByGenre(anime.getGenres().get(0), 0, 6);
            model.addAttribute("relatedAnime", relatedAnime.getContent().stream()
                    .filter(a -> !a.getId().equals(id))
                    .limit(5)
                    .toList());
        }

        return "anime/detail";
    }

    @GetMapping("/{id}/episodes")
    public String animeEpisodes(
            @PathVariable String id,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /anime/{}/episodes", id);

        Anime anime = animeService.getAnimeById(id);

        model.addAttribute("anime", anime);
        model.addAttribute("episodes", anime.getEpisodes());
        model.addAttribute("currentUser", user);

        if (user != null) {
            Optional<UserAnimeRelation> relation = relationService.getRelation(user.getId(), id);
            model.addAttribute("userRelation", relation.orElse(null));
        }

        return "anime/episodes";
    }

    @GetMapping("/{animeId}/watch/{episodeNumber}")
    public String watchEpisode(
            @PathVariable String animeId,
            @PathVariable int episodeNumber,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /anime/{}/watch/{}", animeId, episodeNumber);

        Anime anime = animeService.getAnimeById(animeId);
        Anime.Episode currentEpisode = anime.getEpisodes().stream()
                .filter(e -> e.getNumber() == episodeNumber)
                .findFirst()
                .orElse(null);

        if (currentEpisode == null) {
            return "redirect:/anime/" + animeId;
        }

        model.addAttribute("anime", anime);
        model.addAttribute("currentEpisode", currentEpisode);
        model.addAttribute("episodes", anime.getEpisodes());
        model.addAttribute("currentUser", user);

        // Find prev/next episodes
        anime.getEpisodes().stream()
                .filter(e -> e.getNumber() == episodeNumber - 1)
                .findFirst()
                .ifPresent(e -> model.addAttribute("prevEpisode", e));

        anime.getEpisodes().stream()
                .filter(e -> e.getNumber() == episodeNumber + 1)
                .findFirst()
                .ifPresent(e -> model.addAttribute("nextEpisode", e));

        return "anime/watch";
    }
}
