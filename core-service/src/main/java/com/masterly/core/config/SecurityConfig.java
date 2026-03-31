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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MasterDetailsService masterDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring security filters");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/masters").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clients").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/clients").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/clients/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clients/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/clients/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/services/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/services/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/materials").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/materials/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/materials/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/materials/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/appointments/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/appointments/*/status").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/appointments/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointments/*/status").permitAll()
                        .requestMatchers("/api/services/paginated").permitAll()
                        .requestMatchers("/api/materials/paginated").permitAll()
                        .requestMatchers("/api/clients/paginated").permitAll()
                        .requestMatchers("/api/appointments/paginated").permitAll()
                        .requestMatchers("/api/appointments/calendar").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/masters/profile/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/masters/profile/*").permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(masterDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security configuration completed with stateless session management");
        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        log.debug("Using NoOpPasswordEncoder for development (passwords stored in plain text)");
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.debug("Configuring AuthenticationManager");
        return config.getAuthenticationManager();
    }
}