package com.animefan.repository;

import com.animefan.model.Genre;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Genre entity
 */
@Repository
public interface GenreRepository extends MongoRepository<Genre, String> {

    Optional<Genre> findByName(String name);

    List<Genre> findByActiveTrueOrderByOrderAsc();

    List<Genre> findAllByOrderByOrderAsc();

    boolean existsByName(String name);
}
