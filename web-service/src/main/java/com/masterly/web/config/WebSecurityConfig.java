package com.masterly.web.config;

import com.masterly.web.security.CoreAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final CoreAuthenticationProvider coreAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring web security");

        http
                .csrf(csrf -> {
                    csrf.disable();
                    log.debug("CSRF protection disabled");
                })
                .authenticationProvider(coreAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // Публичные страницы
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/access-denied").permitAll()

                        // Список мастеров и профиль мастера — доступно всем авторизованным
                        .requestMatchers("/masters", "/masters/**").authenticated()

                        // Клиент — доступ к своим записям и форме записи (ставим ПЕРВЫМ)
                        .requestMatchers("/my-appointments", "/client/**", "/appointments/new", "/appointments/create").hasRole("CLIENT")

                        // Админ — полный доступ
                        .requestMatchers("/clients/**", "/services/**", "/materials/**", "/appointments/**", "/profile").hasRole("ADMIN")

                        // Мастер — доступ к своим данным
                        .requestMatchers("/profile", "/my-clients").hasRole("MASTER")

                        // Главная страница — доступна всем авторизованным
                        .requestMatchers("/").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        log.info("Web security configuration completed");
        return http.build();
    }
}