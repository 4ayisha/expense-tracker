package com.example.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())

        .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin()) // ✅ FIX iframe issue
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/index.html",
                "/auth.html",
                "/forgot.html",
                "/chart.html",
                "/style.css",
                "/script.js",
                "/favicon.ico",
                "/h2-console/**",   // ✅ allow console
                "/auth/**",
                "/api/**"
            ).permitAll()

            .anyRequest().authenticated()
        )

        .formLogin(form -> form.disable());

    return http.build();
   }
}
