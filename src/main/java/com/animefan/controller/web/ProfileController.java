package com.animefan.controller.web;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web Controller for User profile pages
 */
@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserAnimeRelationService relationService;
    private final ReviewService reviewService;
    private final StatsService statsService;

    @GetMapping
    public String myProfile(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/profile/" + user.getUsername();
    }

    @GetMapping("/{username}")
    public String userProfile(
            @PathVariable String username,
            @RequestParam(defaultValue = "watching") String tab,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal User currentUser) {

        log.info("GET /profile/{}", username);

        User profileUser = userService.getUserByUsername(username);
        StatsService.UserStats userStats = statsService.getUserStats(profileUser.getId());

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("userStats", userStats);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", tab);
        model.addAttribute("isOwnProfile", currentUser != null && currentUser.getId().equals(profileUser.getId()));

        // Load list based on tab
        Page<?> listContent = switch (tab) {
            case "completed" -> relationService.getUserAnimeByStatus(
                    profileUser.getId(), UserAnimeRelation.Status.COMPLETED, page, 12);
            case "onhold" -> relationService.getUserAnimeByStatus(
                    profileUser.getId(), UserAnimeRelation.Status.ON_HOLD, page, 12);
            case "dropped" -> relationService.getUserAnimeByStatus(
                    profileUser.getId(), UserAnimeRelation.Status.DROPPED, page, 12);
            case "planned" -> relationService.getUserAnimeByStatus(
                    profileUser.getId(), UserAnimeRelation.Status.PLAN_TO_WATCH, page, 12);
            case "favorites" -> relationService.getUserFavorites(profileUser.getId(), page, 12);
            case "reviews" -> reviewService.getReviewsByUser(profileUser.getId(), page, 10);
            default -> relationService.getUserAnimeByStatus(
                    profileUser.getId(), UserAnimeRelation.Status.WATCHING, page, 12);
        };

        model.addAttribute("listContent", listContent);

        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfile(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }

        log.info("GET /profile/edit");

        // Refresh user data
        User freshUser = userService.getUserById(user.getId());

        model.addAttribute("user", freshUser);
        model.addAttribute("currentUser", user);

        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/login";
        }

        log.info("POST /profile/edit");

        try {
            User currentUser = userService.getUserById(user.getId());

            if (displayName != null && !displayName.isBlank()) {
                currentUser.setDisplayName(displayName);
            }
            if (bio != null) {
                currentUser.setBio(bio);
            }

            // Handle avatar - URL takes priority over file
            if (avatarUrl != null && !avatarUrl.isBlank()) {
                currentUser.setAvatarUrl(avatarUrl);
            } else if (avatarFile != null && !avatarFile.isEmpty()) {
                // For now, just store as base64 (in production, upload to cloud storage)
                String base64 = java.util.Base64.getEncoder().encodeToString(avatarFile.getBytes());
                String mimeType = avatarFile.getContentType();
                currentUser.setAvatarUrl("data:" + mimeType + ";base64," + base64);
            }

            userService.saveUser(currentUser);
            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен!");
        } catch (Exception e) {
            log.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении профиля: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/email")
    public String updateEmail(
            @AuthenticationPrincipal User user,
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/login";
        }

        log.info("POST /profile/email");

        try {
            userService.updateEmail(user.getId(), email);
            redirectAttributes.addFlashAttribute("success", "Email успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/profile/edit";
    }

    @PostMapping("/password")
    public String updatePassword(
            @AuthenticationPrincipal User user,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/login";
        }

        log.info("POST /profile/password");

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Пароли не совпадают!");
            return "redirect:/profile/edit";
        }

        try {
            userService.changePassword(user.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Пароль успешно изменен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/profile/edit";
    }

    @GetMapping("/settings")
    public String profileSettings(Model model, @AuthenticationPrincipal User user) {
        if (user == null) {
            return "redirect:/login";
        }

        log.info("GET /profile/settings");

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);

        return "profile/settings";
    }

    @GetMapping("/{username}/reviews")
    public String userReviews(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal User currentUser) {

        log.info("GET /profile/{}/reviews", username);

        User profileUser = userService.getUserByUsername(username);
        Page<Review> reviews = reviewService.getReviewsByUser(profileUser.getId(), page, 10);

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("reviews", reviews);
        model.addAttribute("currentUser", currentUser);

        return "profile/reviews";
    }
}
