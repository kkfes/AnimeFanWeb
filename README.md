# AnimeFan - Anime catalog and Streaming platform

A full-featured web application for cataloging anime, managing viewing lists, and writing reviews.

## Technologies

- **Backend**: Java 21, Spring Boot 3.2
- **Frontend**: Thymeleaf, Bootstrap 5
- **Database**: MongoDB
- **Security**: Spring Security (сессии)
- **Documentation**: Swagger/OpenAPI
- **Containerization**: Docker

## Functionality

### Main features
- Full-text anime search by name and description
- CRUD operations for all entities
- User system with USER/ADMIN roles
- User anime lists (watching, viewed, postponed, scheduled)
- Favorites
- Reviews and Ratings
- MongoDB aggregations for analytics

### REST API (15+ endpoints)
- Full CRUD for anime
- Episode management
- Search with filters
- User management
- Reviews and ratings
- Custom lists
- Statistics and analytics

### Thymeleaf pages (8+ страниц)
- Home page with search and recommendations
- Anime catalog with pagination
- Anime page (details, episodes, reviews)
- User profile with lists
- Authorization/registration
- Search with filters
- Admin panel

## Launch

### Requirements
- Java 21+
- MongoDB 6.0+
- Maven 3.8+

### Локальный запуск

1. **Launch MongoDB**
   ```bash
   mongod --dbpath /path/to/data
   ```

2. **Clone and run the application**
   ```bash
   cd AnimeFanWeb
   ./mvnw spring-boot:run
   ```

3. **Open it in a browser**
   - App: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

## Test users

| Username | Password  | Role  |
|----------|-----------|-------|
| admin    | admin123  | ADMIN |
| user     | user123   | USER  |

## Project structure

```
src/main/java/com/animefan/
├── AnimeFanApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── MongoConfig.java
│   ├── CacheConfig.java
│   ├── OpenApiConfig.java
│   └── DataInitializer.java
├── model/
│   ├── Anime.java (с embedded Episode)
│   ├── User.java
│   ├── Studio.java
│   ├── Review.java
│   └── UserAnimeRelation.java
├── dto/
│   ├── AnimeDTO.java
│   ├── ReviewDTO.java
│   └── ...
├── repository/
│   ├── AnimeRepository.java
│   ├── AnimeRepositoryCustom.java
│   └── ...
├── service/
│   ├── AnimeService.java
│   ├── UserService.java
│   └── ...
├── controller/
│   ├── api/v1/
│   │   ├── AnimeApiController.java
│   │   └── ...
│   └── web/
│       ├── HomeController.java
│       └── ...
└── exception/
    └── GlobalExceptionHandler.java
```

## Safety

- Session-based authentication
- BCrypt password hashing
- CSRF protection for forms
- Role-based access model (USER/ADMIN)
- Validation of input data
- **Email verification during registration**

## Email Settings

For email verification to work, you need to configure the SMTP server in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```


## 📈 API Versioning

All API endpoints use the version `/api/v1/`:
- `/api/v1/anime`
- `/api/v1/users`
- `/api/v1/reviews`
- `/api/v1/lists`
- `/api/v1/studios`
- `/api/v1/stats`


## 📖 API Documentation

The Swagger UI is available at: http://localhost:8080/swagger-ui.html