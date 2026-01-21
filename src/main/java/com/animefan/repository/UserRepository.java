package com.animefan.repository;

import com.animefan.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByRole(User.Role role, Pageable pageable);

    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    Page<User> searchByUsername(String query, Pageable pageable);

    // Update last login timestamp
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'lastLoginAt': ?1 } }")
    void updateLastLogin(String userId, LocalDateTime lastLoginAt);

    // Increment user statistics
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'watchedCount': ?1 } }")
    void incrementWatchedCount(String userId, int delta);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'reviewCount': ?1 } }")
    void incrementReviewCount(String userId, int delta);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'favoriteCount': ?1 } }")
    void incrementFavoriteCount(String userId, int delta);

    long countByRole(User.Role role);
}
