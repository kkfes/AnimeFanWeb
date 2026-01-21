package com.animefan.service;

import com.animefan.model.Genre;
import com.animefan.repository.AnimeRepository;
import com.animefan.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for Genre management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final AnimeRepository animeRepository;

    public List<Genre> getActiveGenres() {
        return genreRepository.findByActiveTrueOrderByOrderAsc();
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAllByOrderByOrderAsc();
    }

    public Optional<Genre> getGenreById(String id) {
        return genreRepository.findById(id);
    }

    public Optional<Genre> getGenreByName(String name) {
        return genreRepository.findByName(name);
    }

    public Genre createGenre(Genre genre) {
        log.info("Creating new genre: {}", genre.getName());
        if (genreRepository.existsByName(genre.getName())) {
            throw new RuntimeException("Genre already exists: " + genre.getName());
        }
        if (genre.getOrder() == null) {
            genre.setOrder(getAllGenres().size() + 1);
        }
        if (genre.getActive() == null) {
            genre.setActive(true);
        }
        if (genre.getAnimeCount() == null) {
            genre.setAnimeCount(0L);
        }
        return genreRepository.save(genre);
    }

    public Genre updateGenre(String id, Genre genreData) {
        log.info("Updating genre: {}", id);
        return genreRepository.findById(id)
                .map(genre -> {
                    genre.setName(genreData.getName());
                    genre.setNameRu(genreData.getNameRu());
                    genre.setDescription(genreData.getDescription());
                    genre.setBannerUrl(genreData.getBannerUrl());
                    genre.setOrder(genreData.getOrder());
                    genre.setActive(genreData.getActive());
                    return genreRepository.save(genre);
                })
                .orElseThrow(() -> new RuntimeException("Genre not found: " + id));
    }

    public void deleteGenre(String id) {
        log.info("Deleting genre: {}", id);
        genreRepository.deleteById(id);
    }

    public void updateAnimeCount(String genreName) {
        genreRepository.findByName(genreName).ifPresent(genre -> {
            // Count anime with this genre
            long count = animeRepository.countByGenresContaining(genreName);
            genre.setAnimeCount(count);
            genreRepository.save(genre);
        });
    }

    /**
     * Recalculate anime count for all genres
     */
    public void recalculateAllGenreCounts() {
        log.info("Recalculating anime counts for all genres...");
        List<Genre> genres = genreRepository.findAll();
        for (Genre genre : genres) {
            long count = animeRepository.countByGenresContaining(genre.getName());
            genre.setAnimeCount(count);
        }
        genreRepository.saveAll(genres);
        log.info("Genre counts updated for {} genres", genres.size());
    }

    /**
     * Update counts for specific genres
     */
    public void updateGenreCounts(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) return;

        for (String genreName : genreNames) {
            updateAnimeCount(genreName);
        }
    }

    public void initDefaultGenres() {
        if (genreRepository.count() == 0) {
            log.info("Initializing default genres...");
            List<Genre> defaultGenres = List.of(
                Genre.builder().name("Action").nameRu("Экшен").order(1).active(true).animeCount(0L).build(),
                Genre.builder().name("Adventure").nameRu("Приключения").order(2).active(true).animeCount(0L).build(),
                Genre.builder().name("Comedy").nameRu("Комедия").order(3).active(true).animeCount(0L).build(),
                Genre.builder().name("Drama").nameRu("Драма").order(4).active(true).animeCount(0L).build(),
                Genre.builder().name("Fantasy").nameRu("Фэнтези").order(5).active(true).animeCount(0L).build(),
                Genre.builder().name("Horror").nameRu("Ужасы").order(6).active(true).animeCount(0L).build(),
                Genre.builder().name("Mystery").nameRu("Мистика").order(7).active(true).animeCount(0L).build(),
                Genre.builder().name("Romance").nameRu("Романтика").order(8).active(true).animeCount(0L).build(),
                Genre.builder().name("Sci-Fi").nameRu("Научная фантастика").order(9).active(true).animeCount(0L).build(),
                Genre.builder().name("Slice of Life").nameRu("Повседневность").order(10).active(true).animeCount(0L).build(),
                Genre.builder().name("Sports").nameRu("Спорт").order(11).active(true).animeCount(0L).build(),
                Genre.builder().name("Supernatural").nameRu("Сверхъестественное").order(12).active(true).animeCount(0L).build()
            );
            genreRepository.saveAll(defaultGenres);
        }
    }
}
