package com.animefan.config;

import com.animefan.model.*;
import com.animefan.repository.*;
import com.animefan.service.GenreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes sample data for development and testing
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AnimeRepository animeRepository;
    private final StudioRepository studioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenreService genreService;

    @Override
    public void run(String... args) {
        // Initialize genres first
        genreService.initDefaultGenres();

        if (userRepository.count() == 0) {
            log.info("Initializing sample data...");
            initUsers();
            initStudios();
            initAnime();
            log.info("Sample data initialized successfully!");
        }

        // Always recalculate genre counts on startup
        log.info("Recalculating genre counts...");
        genreService.recalculateAllGenreCounts();
    }

    private void initUsers() {
        // Admin user
        User admin = User.builder()
                .username("admin")
                .email("admin@animefan.com")
                .password(passwordEncoder.encode("admin123"))
                .displayName("Администратор")
                .role(User.Role.ADMIN)
                .enabled(true)
                .emailVerified(true)
                .build();
        userRepository.save(admin);

        // Regular user
        User user = User.builder()
                .username("user")
                .email("user@animefan.com")
                .password(passwordEncoder.encode("user123"))
                .displayName("Пользователь")
                .role(User.Role.USER)
                .enabled(true)
                .emailVerified(true)
                .build();
        userRepository.save(user);

        log.info("Created 2 sample users (admin/admin123, user/user123)");
    }

    private void initStudios() {
        List<Studio> studios = Arrays.asList(
                Studio.builder()
                        .name("Studio Ghibli")
                        .country("Japan")
                        .foundedYear(1985)
                        .description("Одна из самых известных анимационных студий Японии")
                        .website("https://www.ghibli.jp")
                        .build(),
                Studio.builder()
                        .name("Kyoto Animation")
                        .country("Japan")
                        .foundedYear(1981)
                        .description("Известная своим высоким качеством анимации")
                        .build(),
                Studio.builder()
                        .name("MAPPA")
                        .country("Japan")
                        .foundedYear(2011)
                        .description("Современная студия с впечатляющими работами")
                        .build(),
                Studio.builder()
                        .name("ufotable")
                        .country("Japan")
                        .foundedYear(2000)
                        .description("Известна качественной CGI анимацией")
                        .build(),
                Studio.builder()
                        .name("Wit Studio")
                        .country("Japan")
                        .foundedYear(2012)
                        .description("Создатели Attack on Titan")
                        .build()
        );

        studioRepository.saveAll(studios);
        log.info("Created {} sample studios", studios.size());
    }

    private void initAnime() {
        List<Studio> studios = studioRepository.findAll();

        List<Anime> animeList = Arrays.asList(
                Anime.builder()
                        .title("Атака Титанов")
                        .titleEnglish("Attack on Titan")
                        .titleJapanese("進撃の巨人")
                        .description("В мире, где человечество живёт за огромными стенами, защищающими от гигантских человекоподобных существ - Титанов, молодой Эрен Йегер мечтает о мире за стенами. Когда Титаны прорывают стену и уничтожают его родной город, Эрен клянётся уничтожить всех Титанов.")
                        .genres(Arrays.asList("Action", "Drama", "Fantasy", "Mystery"))
                        .releaseYear(2013)
                        .releaseDate(LocalDate.of(2013, 4, 7))
                        .status("COMPLETED")
                        .type("TV")
                        .episodeCount(25)
                        .rating(9.0)
                        .ratingCount(1500)
                        .viewCount(50000)
                        .favoriteCount(5000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/10/47347.jpg")
                        .studioId(studios.stream().filter(s -> s.getName().equals("Wit Studio")).findFirst().map(Studio::getId).orElse(null))
                        .studioName("Wit Studio")
                        .episodes(Arrays.asList(
                                Anime.Episode.builder().number(1).title("Падение Сигансины").duration(24).build(),
                                Anime.Episode.builder().number(2).title("Тот день").duration(24).build(),
                                Anime.Episode.builder().number(3).title("Мерцание во тьме").duration(24).build()
                        ))
                        .build(),

                Anime.builder()
                        .title("Клинок, рассекающий демонов")
                        .titleEnglish("Demon Slayer")
                        .titleJapanese("鬼滅の刃")
                        .description("Танджиро Камадо - добросердечный мальчик, который зарабатывает на жизнь, продавая уголь. Однажды его семью убивают демоны, а младшая сестра Незуко превращается в демона. Танджиро отправляется в путешествие, чтобы найти способ вернуть сестре человеческий облик и отомстить.")
                        .genres(Arrays.asList("Action", "Fantasy", "Supernatural"))
                        .releaseYear(2019)
                        .releaseDate(LocalDate.of(2019, 4, 6))
                        .status("ONGOING")
                        .type("TV")
                        .episodeCount(26)
                        .rating(8.9)
                        .ratingCount(2000)
                        .viewCount(80000)
                        .favoriteCount(8000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/1286/99889.jpg")
                        .studioId(studios.stream().filter(s -> s.getName().equals("ufotable")).findFirst().map(Studio::getId).orElse(null))
                        .studioName("ufotable")
                        .episodes(Arrays.asList(
                                Anime.Episode.builder().number(1).title("Жестокость").duration(24).build(),
                                Anime.Episode.builder().number(2).title("Учитель Саконджи Урокодаки").duration(24).build()
                        ))
                        .build(),

                Anime.builder()
                        .title("Унесённые призраками")
                        .titleEnglish("Spirited Away")
                        .titleJapanese("千と千尋の神隠し")
                        .description("Десятилетняя Тихиро вместе с родителями переезжает в новый дом. Заблудившись по дороге, семья попадает в странный заброшенный парк развлечений, где родители Тихиро превращаются в свиней. Девочка оказывается в мире духов и должна работать в бане, чтобы спасти родителей.")
                        .genres(Arrays.asList("Adventure", "Drama", "Fantasy", "Supernatural"))
                        .releaseYear(2001)
                        .releaseDate(LocalDate.of(2001, 7, 20))
                        .status("COMPLETED")
                        .type("MOVIE")
                        .episodeCount(1)
                        .rating(9.3)
                        .ratingCount(3000)
                        .viewCount(100000)
                        .favoriteCount(15000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/6/79597.jpg")
                        .studioId(studios.stream().filter(s -> s.getName().equals("Studio Ghibli")).findFirst().map(Studio::getId).orElse(null))
                        .studioName("Studio Ghibli")
                        .build(),

                Anime.builder()
                        .title("Наруто")
                        .titleEnglish("Naruto")
                        .titleJapanese("ナルト")
                        .description("История о Наруто Узумаки, молодом ниндзя, который мечтает стать Хокаге - лидером своей деревни. В нём запечатан могущественный демон-лис, из-за чего жители деревни его избегают.")
                        .genres(Arrays.asList("Action", "Adventure", "Comedy", "Fantasy"))
                        .releaseYear(2002)
                        .status("COMPLETED")
                        .type("TV")
                        .episodeCount(220)
                        .rating(8.0)
                        .ratingCount(5000)
                        .viewCount(150000)
                        .favoriteCount(12000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/13/17405.jpg")
                        .build(),

                Anime.builder()
                        .title("Тетрадь смерти")
                        .titleEnglish("Death Note")
                        .titleJapanese("デスノート")
                        .description("Старшеклассник Лайт Ягами находит таинственную тетрадь, принадлежащую богу смерти. Любой, чьё имя будет записано в ней, умрёт. Лайт решает использовать тетрадь, чтобы очистить мир от преступников.")
                        .genres(Arrays.asList("Mystery", "Thriller", "Supernatural"))
                        .releaseYear(2006)
                        .status("COMPLETED")
                        .type("TV")
                        .episodeCount(37)
                        .rating(9.0)
                        .ratingCount(4500)
                        .viewCount(120000)
                        .favoriteCount(10000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/9/9453.jpg")
                        .build(),

                Anime.builder()
                        .title("Ванпанчмен")
                        .titleEnglish("One Punch Man")
                        .titleJapanese("ワンパンマン")
                        .description("Сайтама - супергерой, который может победить любого противника одним ударом. Из-за этого он потерял вкус к жизни и ищет достойного соперника.")
                        .genres(Arrays.asList("Action", "Comedy", "Sci-Fi"))
                        .releaseYear(2015)
                        .status("ONGOING")
                        .type("TV")
                        .episodeCount(12)
                        .rating(8.7)
                        .ratingCount(3500)
                        .viewCount(90000)
                        .favoriteCount(7000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/12/76049.jpg")
                        .build(),

                Anime.builder()
                        .title("Стальной алхимик: Братство")
                        .titleEnglish("Fullmetal Alchemist: Brotherhood")
                        .titleJapanese("鋼の錬金術師")
                        .description("Братья Эдвард и Альфонс Элрик пытались воскресить мать с помощью алхимии, но эксперимент провалился. Эдвард потерял руку и ногу, Альфонс - всё тело. Теперь братья ищут философский камень, чтобы вернуть свои тела.")
                        .genres(Arrays.asList("Action", "Adventure", "Drama", "Fantasy"))
                        .releaseYear(2009)
                        .status("COMPLETED")
                        .type("TV")
                        .episodeCount(64)
                        .rating(9.5)
                        .ratingCount(6000)
                        .viewCount(180000)
                        .favoriteCount(20000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/1223/96541.jpg")
                        .build(),

                Anime.builder()
                        .title("Твоё имя")
                        .titleEnglish("Your Name")
                        .titleJapanese("君の名は。")
                        .description("Старшеклассница Мицуха живёт в маленьком городке и мечтает о жизни в Токио. Токийский школьник Таки мечтает стать архитектором. Однажды они начинают меняться телами во сне.")
                        .genres(Arrays.asList("Drama", "Romance", "Supernatural"))
                        .releaseYear(2016)
                        .status("COMPLETED")
                        .type("MOVIE")
                        .episodeCount(1)
                        .rating(9.2)
                        .ratingCount(4000)
                        .viewCount(130000)
                        .favoriteCount(13000)
                        .posterUrl("https://cdn.myanimelist.net/images/anime/5/87048.jpg")
                        .build()
        );

        animeRepository.saveAll(animeList);
        log.info("Created {} sample anime", animeList.size());
    }
}
