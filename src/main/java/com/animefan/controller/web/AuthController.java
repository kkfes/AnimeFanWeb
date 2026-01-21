package com.animefan.controller.web;

import com.animefan.model.User;
import com.animefan.service.EmailService;
import com.animefan.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Web Controller for Authentication pages
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String verified,
            Model model,
            @AuthenticationPrincipal User user) {

        log.info("GET /login");

        // Redirect if already logged in
        if (user != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "Неверное имя пользователя или пароль, либо email не подтверждён");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        if (verified != null) {
            model.addAttribute("message", "Email успешно подтверждён! Теперь вы можете войти.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /register");

        // Redirect if already logged in
        if (user != null) {
            return "redirect:/";
        }

        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("POST /register - username: {}", form.getUsername());

        // Check password confirmation
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "Пароли не совпадают");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            // Register user (will be disabled until email verification)
            User newUser = userService.registerUser(
                    form.getUsername(),
                    form.getEmail(),
                    form.getPassword(),
                    form.getDisplayName()
            );

            // Generate verification token
            String verificationToken = UUID.randomUUID().toString();
            LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);
            userService.setVerificationToken(newUser.getId(), verificationToken, tokenExpiry);

            // Send verification email
            emailService.sendVerificationEmail(
                    form.getEmail(),
                    form.getUsername(),
                    verificationToken
            );

            redirectAttributes.addFlashAttribute("message",
                    "Регистрация успешна! Проверьте вашу почту для подтверждения email.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam String token,
            RedirectAttributes redirectAttributes) {

        log.info("GET /verify-email with token: {}", token);

        try {
            User user = userService.getUserByVerificationToken(token);

            // Check token expiry
            if (user.getVerificationTokenExpiry() == null ||
                    user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error",
                        "Ссылка для подтверждения истекла. Пожалуйста, запросите новую.");
                return "redirect:/login";
            }

            // Verify email
            userService.verifyEmail(user.getId());

            log.info("Email verified for user: {}", user.getUsername());
            return "redirect:/login?verified=true";

        } catch (Exception e) {
            log.error("Email verification failed", e);
            redirectAttributes.addFlashAttribute("error",
                    "Недействительная ссылка для подтверждения.");
            return "redirect:/login";
        }
    }

    @GetMapping("/resend-verification")
    public String resendVerificationPage(Model model) {
        model.addAttribute("email", "");
        return "auth/resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        log.info("POST /resend-verification for email: {}", email);

        try {
            User user = userService.getUserByEmail(email);

            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                redirectAttributes.addFlashAttribute("message",
                        "Email уже подтверждён. Вы можете войти.");
                return "redirect:/login";
            }

            // Generate new verification token
            String verificationToken = UUID.randomUUID().toString();
            LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);
            userService.setVerificationToken(user.getId(), verificationToken, tokenExpiry);

            // Send verification email
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    verificationToken
            );

            redirectAttributes.addFlashAttribute("message",
                    "Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Resend verification failed", e);
            redirectAttributes.addFlashAttribute("error",
                    "Пользователь с таким email не найден.");
            return "redirect:/resend-verification";
        }
    }

    @GetMapping("/password-reset")
    public String passwordResetPage(Model model, @AuthenticationPrincipal User user) {
        log.info("GET /password-reset");

        // Redirect if already logged in
        if (user != null) {
            return "redirect:/profile/settings";
        }

        return "auth/password-reset";
    }

    @PostMapping("/password-reset")
    public String requestPasswordReset(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        log.info("POST /password-reset for email: {}", email);

        try {
            User user = userService.getUserByEmail(email);

            // Generate password reset token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(1);
            userService.setPasswordResetToken(user.getId(), resetToken, tokenExpiry);

            // Send password reset email
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getUsername(),
                    resetToken
            );

            redirectAttributes.addFlashAttribute("success",
                    "Инструкции по сбросу пароля отправлены на " + email);
            return "redirect:/password-reset";

        } catch (Exception e) {
            log.error("Password reset request failed", e);
            // Don't reveal if email exists or not for security
            redirectAttributes.addFlashAttribute("success",
                    "Если аккаунт с таким email существует, вы получите письмо с инструкциями.");
            return "redirect:/password-reset";
        }
    }

    @GetMapping("/password-reset/confirm")
    public String passwordResetConfirmPage(
            @RequestParam String token,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("GET /password-reset/confirm with token");

        try {
            User user = userService.getUserByPasswordResetToken(token);

            // Check token expiry
            if (user.getPasswordResetTokenExpiry() == null ||
                    user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error",
                        "Ссылка для сброса пароля истекла. Пожалуйста, запросите новую.");
                return "redirect:/password-reset";
            }

            model.addAttribute("token", token);
            return "auth/password-reset-confirm";

        } catch (Exception e) {
            log.error("Password reset confirm failed", e);
            redirectAttributes.addFlashAttribute("error",
                    "Недействительная ссылка для сброса пароля.");
            return "redirect:/password-reset";
        }
    }

    @PostMapping("/password-reset/confirm")
    public String confirmPasswordReset(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        log.info("POST /password-reset/confirm");

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Пароли не совпадают");
            return "redirect:/password-reset/confirm?token=" + token;
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Пароль должен быть не менее 6 символов");
            return "redirect:/password-reset/confirm?token=" + token;
        }

        try {
            User user = userService.getUserByPasswordResetToken(token);

            // Check token expiry
            if (user.getPasswordResetTokenExpiry() == null ||
                    user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error",
                        "Ссылка для сброса пароля истекла.");
                return "redirect:/password-reset";
            }

            // Reset password
            userService.resetPassword(user.getId(), newPassword);

            redirectAttributes.addFlashAttribute("message",
                    "Пароль успешно изменён! Теперь вы можете войти с новым паролем.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Password reset failed", e);
            redirectAttributes.addFlashAttribute("error",
                    "Не удалось сбросить пароль. Попробуйте ещё раз.");
            return "redirect:/password-reset";
        }
    }

    @Data
    public static class RegisterForm {
        @jakarta.validation.constraints.NotBlank(message = "Имя пользователя обязательно")
        @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
        private String username;

        @jakarta.validation.constraints.NotBlank(message = "Email обязателен")
        @jakarta.validation.constraints.Email(message = "Некорректный формат email")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Пароль обязателен")
        @jakarta.validation.constraints.Size(min = 6, message = "Пароль должен быть не менее 6 символов")
        private String password;

        @jakarta.validation.constraints.NotBlank(message = "Подтверждение пароля обязательно")
        private String confirmPassword;

        private String displayName;
    }
}
