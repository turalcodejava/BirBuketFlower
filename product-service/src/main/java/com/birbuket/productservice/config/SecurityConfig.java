package com.birbuket.productservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/index.html",
                                        "/webjars/**"
                                ).permitAll()
                                .requestMatchers("/uploads/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/category/**", "/api/variant/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/category/**", "/api/variant/**").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/category/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/category/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/product", "/api/product/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/product/**").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/product/**").authenticated()
                                .anyRequest().authenticated()
                );

        return http.build();
    }
}
