package com.animefan.service;

import com.animefan.exception.ResourceNotFoundException;
import com.animefan.exception.ValidationException;
import com.animefan.model.User;
import com.animefan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("1")
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .displayName("Test User")
                .role(User.Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should load user by username")
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void loadUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("unknown");
        });
    }

    @Test
    @DisplayName("Should register new user successfully")
    void registerUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("new-id");
            return user;
        });

        User result = userService.registerUser("newuser", "new@example.com", "password123", "New User");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(User.Role.USER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void registerUser_DuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            userService.registerUser("testuser", "new@example.com", "password123", null);
        });
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_DuplicateEmail() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            userService.registerUser("newuser", "test@example.com", "password123", null);
        });
    }

    @Test
    @DisplayName("Should get user by ID")
    void getUserById_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        User result = userService.getUserById("1");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void getUserById_NotFound() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById("999");
        });
    }

    @Test
    @DisplayName("Should change password successfully")
    void changePassword_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> {
            userService.changePassword("1", "oldPassword", "newPassword");
        });
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void changePassword_WrongOldPassword() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encoded_password")).thenReturn(false);

        assertThrows(ValidationException.class, () -> {
            userService.changePassword("1", "wrongPassword", "newPassword");
        });
    }

    @Test
    @DisplayName("Should update user role")
    void updateUserRole_Success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUserRole("1", User.Role.ADMIN);

        assertNotNull(result);
        assertEquals(User.Role.ADMIN, result.getRole());
    }
}
