package com.animefan.config;

import com.animefan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security Configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Disable CSRF for API
            )
            .authorizeHttpRequests(auth -> auth
                // Public static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()

                // Public pages
                .requestMatchers("/", "/home", "/anime/**", "/search", "/studios/**").permitAll()
                .requestMatchers("/login", "/register", "/error/**").permitAll()
                .requestMatchers("/verify-email", "/resend-verification", "/password-reset/**").permitAll()
                .requestMatchers("/about", "/genre/**", "/genres", "/top", "/status").permitAll()

                // Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()

                // Public API endpoints (read-only)
                .requestMatchers(HttpMethod.GET, "/api/v1/anime/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/genres/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/studios/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/stats/**").permitAll()

                // User registration
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()

                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/anime/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/anime/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/anime/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/studios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/studios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/studios/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )
            .rememberMe(remember -> remember
                .key("animefan-remember-me-key")
                .tokenValiditySeconds(86400 * 7) // 7 days
            );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
