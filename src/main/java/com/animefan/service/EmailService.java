package com.animefan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@animefan.com}")
    private String fromEmail;

    @Value("${app.email.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Send verification email
     */
    @Async
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        log.info("Sending verification email to: {}", to);

        String verificationLink = baseUrl + "/verify-email?token=" + verificationToken;
        String subject = "AnimeFan - Подтвердите ваш email";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎌 AnimeFan</h1>
                        <p>Подтверждение регистрации</p>
                    </div>
                    <div class="content">
                        <h2>Привет, %s!</h2>
                        <p>Спасибо за регистрацию на AnimeFan! Пожалуйста, подтвердите ваш email адрес, нажав на кнопку ниже:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Подтвердить Email</a>
                        </p>
                        <p>Или скопируйте эту ссылку в браузер:</p>
                        <p style="word-break: break-all; background: #eee; padding: 10px; border-radius: 5px;">%s</p>
                        <p><strong>Важно:</strong> Ссылка действительна в течение 24 часов.</p>
                        <p>Если вы не регистрировались на AnimeFan, просто проигнорируйте это письмо.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 AnimeFan. Все права защищены.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, verificationLink, verificationLink);

        try {
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        log.info("Sending password reset email to: {}", to);

        String resetLink = baseUrl + "/reset-password?token=" + resetToken;
        String subject = "AnimeFan - Сброс пароля";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #e74c3c; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎌 AnimeFan</h1>
                        <p>Сброс пароля</p>
                    </div>
                    <div class="content">
                        <h2>Привет, %s!</h2>
                        <p>Вы запросили сброс пароля для вашего аккаунта на AnimeFan.</p>
                        <p>Нажмите на кнопку ниже, чтобы создать новый пароль:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Сбросить пароль</a>
                        </p>
                        <p><strong>Важно:</strong> Ссылка действительна в течение 1 часа.</p>
                        <p>Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо. Ваш пароль останется прежним.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 AnimeFan. Все права защищены.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, resetLink);

        try {
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception {
        var message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
