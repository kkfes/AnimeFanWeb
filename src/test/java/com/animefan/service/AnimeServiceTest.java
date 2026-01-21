package com.animefan.service;

import com.animefan.dto.AnimeDTO;
import com.animefan.exception.ResourceNotFoundException;
import com.animefan.exception.ValidationException;
import com.animefan.model.Anime;
import com.animefan.repository.AnimeRepository;
import com.animefan.repository.ReviewRepository;
import com.animefan.repository.StudioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AnimeService animeService;

    private Anime testAnime;
    private AnimeDTO testAnimeDTO;

    @BeforeEach
    void setUp() {
        testAnime = Anime.builder()
                .id("1")
                .title("Test Anime")
                .description("Test description for anime")
                .genres(Arrays.asList("Action", "Adventure"))
                .releaseYear(2024)
                .rating(8.5)
                .ratingCount(100)
                .viewCount(1000)
                .favoriteCount(500)
                .status("ONGOING")
                .type("TV")
                .episodeCount(12)
                .build();

        testAnimeDTO = AnimeDTO.builder()
                .title("New Anime")
                .description("New anime description that is long enough")
                .genres(Arrays.asList("Comedy", "Romance"))
                .releaseYear(2024)
                .status("ONGOING")
                .type("TV")
                .build();
    }

    @Test
    @DisplayName("Should get anime by ID successfully")
    void getAnimeById_Success() {
        when(animeRepository.findById("1")).thenReturn(Optional.of(testAnime));

        Anime result = animeService.getAnimeById("1");

        assertNotNull(result);
        assertEquals("Test Anime", result.getTitle());
        verify(animeRepository).findById("1");
    }

    @Test
    @DisplayName("Should throw exception when anime not found")
    void getAnimeById_NotFound() {
        when(animeRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            animeService.getAnimeById("999");
        });
    }

    @Test
    @DisplayName("Should get all anime with pagination")
    void getAllAnime_Success() {
        List<Anime> animeList = Arrays.asList(testAnime);
        Page<Anime> animePage = new PageImpl<>(animeList, PageRequest.of(0, 12), 1);

        when(animeRepository.findAll(any(PageRequest.class))).thenReturn(animePage);

        Page<Anime> result = animeService.getAllAnime(0, 12, "rating", "desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Anime", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Should create anime successfully")
    void createAnime_Success() {
        when(animeRepository.existsByTitle(anyString())).thenReturn(false);
        when(animeRepository.save(any(Anime.class))).thenAnswer(invocation -> {
            Anime anime = invocation.getArgument(0);
            anime.setId("new-id");
            return anime;
        });

        Anime result = animeService.createAnime(testAnimeDTO);

        assertNotNull(result);
        assertEquals("New Anime", result.getTitle());
        verify(animeRepository).save(any(Anime.class));
    }

    @Test
    @DisplayName("Should throw exception when creating anime with duplicate title")
    void createAnime_DuplicateTitle() {
        when(animeRepository.existsByTitle("New Anime")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            animeService.createAnime(testAnimeDTO);
        });
    }

    @Test
    @DisplayName("Should update anime successfully")
    void updateAnime_Success() {
        when(animeRepository.findById("1")).thenReturn(Optional.of(testAnime));
        when(animeRepository.save(any(Anime.class))).thenReturn(testAnime);

        Anime result = animeService.updateAnime("1", testAnimeDTO);

        assertNotNull(result);
        assertEquals("New Anime", result.getTitle());
        verify(animeRepository).save(any(Anime.class));
    }

    @Test
    @DisplayName("Should delete anime successfully")
    void deleteAnime_Success() {
        when(animeRepository.findById("1")).thenReturn(Optional.of(testAnime));
        doNothing().when(animeRepository).deleteById("1");

        assertDoesNotThrow(() -> animeService.deleteAnime("1"));
        verify(animeRepository).deleteById("1");
    }

    @Test
    @DisplayName("Should get top anime")
    void getTopAnime_Success() {
        List<Anime> topAnime = Arrays.asList(testAnime);
        when(animeRepository.getTopAnimeByRating(10)).thenReturn(topAnime);

        List<Anime> result = animeService.getTopAnime(10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get anime by genre")
    void getAnimeByGenre_Success() {
        List<Anime> animeList = Arrays.asList(testAnime);
        Page<Anime> animePage = new PageImpl<>(animeList);

        when(animeRepository.findByGenresContaining(eq("Action"), any(PageRequest.class)))
                .thenReturn(animePage);

        Page<Anime> result = animeService.getAnimeByGenre("Action", 0, 12);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should increment view count")
    void getAnimeByIdWithView_Success() {
        when(animeRepository.findById("1")).thenReturn(Optional.of(testAnime));
        doNothing().when(animeRepository).incrementViewCount("1");

        Anime result = animeService.getAnimeByIdWithView("1");

        assertNotNull(result);
        verify(animeRepository).incrementViewCount("1");
    }
}
