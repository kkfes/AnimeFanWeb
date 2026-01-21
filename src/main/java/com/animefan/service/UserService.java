package com.animefan.service;

import com.animefan.dto.UserDTO;
import com.animefan.exception.ResourceNotFoundException;
import com.animefan.exception.ValidationException;
import com.animefan.model.User;
import com.animefan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for User business logic and Spring Security UserDetailsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(int page, int size) {
        log.info("Getting all users, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable);
    }

    /**
     * Get user by ID
     */
    public User getUserById(String id) {
        log.info("Getting user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Register new user
     */
    public User registerUser(String username, String email, String password, String displayName) {
        log.info("Registering new user: {}", username);

        // Validate username uniqueness
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("Username already exists");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Email already registered");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .displayName(displayName != null ? displayName : username)
                .role(User.Role.USER)
                .enabled(false) // User needs to verify email first
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(false)
                .watchedCount(0)
                .reviewCount(0)
                .favoriteCount(0)
                .build();

        return userRepository.save(user);
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(String userId, UserDTO userDTO) {
        log.info("Updating user profile: {}", userId);

        User user = getUserById(userId);

        // Update fields
        if (userDTO.getDisplayName() != null) {
            user.setDisplayName(userDTO.getDisplayName());
        }
        if (userDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }

        return userRepository.save(user);
    }

    /**
     * Save user directly
     */
    public User saveUser(User user) {
        log.info("Saving user: {}", user.getId());
        return userRepository.save(user);
    }

    /**
     * Update user email
     */
    public User updateEmail(String userId, String newEmail) {
        log.info("Updating email for user: {}", userId);

        if (userRepository.existsByEmail(newEmail)) {
            throw new ValidationException("Email already registered");
        }

        User user = getUserById(userId);
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    /**
     * Change password
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Update user role (admin only)
     */
    public User updateUserRole(String userId, User.Role role) {
        log.info("Updating role for user {} to {}", userId, role);

        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Enable/disable user account (admin only)
     */
    public User setUserEnabled(String userId, boolean enabled) {
        log.info("Setting user {} enabled: {}", userId, enabled);

        User user = getUserById(userId);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        userRepository.deleteById(userId);
    }

    /**
     * Search users by username
     */
    public Page<User> searchUsers(String query, int page, int size) {
        log.info("Searching users by: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.searchByUsername(query, pageable);
    }

    /**
     * Update last login time
     */
    public void updateLastLogin(String userId) {
        log.debug("Updating last login for user: {}", userId);
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    /**
     * Increment watched count
     */
    public void incrementWatchedCount(String userId) {
        userRepository.incrementWatchedCount(userId, 1);
    }

    /**
     * Increment review count
     */
    public void incrementReviewCount(String userId, int delta) {
        userRepository.incrementReviewCount(userId, delta);
    }

    /**
     * Increment favorite count
     */
    public void incrementFavoriteCount(String userId, int delta) {
        userRepository.incrementFavoriteCount(userId, delta);
    }

    /**
     * Check if user exists
     */
    public boolean existsById(String userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Get user count
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Find user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Find user by verification token
     */
    public User getUserByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("User", "verificationToken", token));
    }

    /**
     * Verify user email
     */
    public void verifyEmail(String userId) {
        User user = getUserById(userId);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    /**
     * Set verification token for user
     */
    public void setVerificationToken(String userId, String token, LocalDateTime expiry) {
        User user = getUserById(userId);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(expiry);
        userRepository.save(user);
    }

    /**
     * Set password reset token for user
     */
    public void setPasswordResetToken(String userId, String token, LocalDateTime expiry) {
        User user = getUserById(userId);
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(expiry);
        userRepository.save(user);
    }

    /**
     * Find user by password reset token
     */
    public User getUserByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("User", "passwordResetToken", token));
    }

    /**
     * Reset user password
     */
    public void resetPassword(String userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
}
