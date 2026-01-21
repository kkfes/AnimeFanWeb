package com.animefan.controller.api.v1;

import com.animefan.model.Anime;
import com.animefan.service.AnimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimeApiController.class)
class AnimeApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnimeService animeService;

    private Anime testAnime;

    @BeforeEach
    void setUp() {
        testAnime = Anime.builder()
                .id("1")
                .title("Test Anime")
                .description("Test description")
                .genres(Arrays.asList("Action", "Adventure"))
                .releaseYear(2024)
                .rating(8.5)
                .ratingCount(100)
                .status("ONGOING")
                .type("TV")
                .build();
    }

    @Test
    @DisplayName("Should get all anime")
    void getAllAnime_Success() throws Exception {
        List<Anime> animeList = Arrays.asList(testAnime);
        when(animeService.getAllAnime(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(animeList, PageRequest.of(0, 12), 1));

        mockMvc.perform(get("/api/v1/anime")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Anime"));
    }

    @Test
    @DisplayName("Should get anime by ID")
    void getAnimeById_Success() throws Exception {
        when(animeService.getAnimeById("1")).thenReturn(testAnime);

        mockMvc.perform(get("/api/v1/anime/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Test Anime"));
    }

    @Test
    @DisplayName("Should search anime")
    void searchAnime_Success() throws Exception {
        List<Anime> animeList = Arrays.asList(testAnime);
        when(animeService.fullTextSearch(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(animeList));

        mockMvc.perform(get("/api/v1/anime/search")
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get top anime")
    void getTopAnime_Success() throws Exception {
        when(animeService.getTopAnime(10)).thenReturn(Arrays.asList(testAnime));

        mockMvc.perform(get("/api/v1/anime/top")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Anime"));
    }

    @Test
    @DisplayName("Should get all genres")
    void getAllGenres_Success() throws Exception {
        when(animeService.getAllGenres()).thenReturn(Arrays.asList("Action", "Comedy", "Drama"));

        mockMvc.perform(get("/api/v1/anime/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin should create anime")
    void createAnime_AdminSuccess() throws Exception {
        when(animeService.createAnime(any())).thenReturn(testAnime);

        String json = """
            {
                "title": "New Anime",
                "description": "Description that is long enough for validation",
                "genres": ["Action"],
                "releaseYear": 2024
            }
            """;

        mockMvc.perform(post("/api/v1/anime")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("User should not create anime")
    void createAnime_UserForbidden() throws Exception {
        String json = """
            {
                "title": "New Anime",
                "description": "Description that is long enough",
                "genres": ["Action"]
            }
            """;

        mockMvc.perform(post("/api/v1/anime")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }
}
