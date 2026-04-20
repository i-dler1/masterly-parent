package com.masterly.core.config;

import com.masterly.core.security.JwtAuthenticationFilter;
import com.masterly.core.service.MasterDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация Spring Security для приложения.
 * <p>
 * Настраивает:
 * <ul>
 *   <li>Stateless сессии (JWT аутентификация)</li>
 *   <li>Публичные и защищённые эндпоинты</li>
 *   <li>BCrypt кодирование паролей</li>
 *   <li>JWT фильтр перед UsernamePasswordAuthenticationFilter</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MasterDetailsService masterDetailsService;

    /**
     * Настройка цепочки фильтров безопасности.
     *
     * @param http HttpSecurity для конфигурации
     * @return настроенная цепочка фильтров
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring security filters");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/masters").permitAll()
                        .requestMatchers("/api/masters/by-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/masters/*").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/clients/*").authenticated()

                        // Admin эндпоинты (только для ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Все остальные эндпоинты требуют аутентификации
                        .anyRequest().authenticated()
                )
                .userDetailsService(masterDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security configuration completed with stateless session management");
        return http.build();
    }

    /**
     * Кодировщик паролей BCrypt.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Using BCryptPasswordEncoder for production");
        return new BCryptPasswordEncoder();
    }

    /**
     * Менеджер аутентификации.
     *
     * @param config конфигурация аутентификации
     * @return AuthenticationManager
     * @throws Exception при ошибке создания
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("Configuring AuthenticationManager");
        return config.getAuthenticationManager();
    }
}