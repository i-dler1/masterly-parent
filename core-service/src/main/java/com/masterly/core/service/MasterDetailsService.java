package com.masterly.core.service;

import com.masterly.core.repository.MasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки данных пользователя при аутентификации.
 * Реализует {@link UserDetailsService} для интеграции со Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MasterDetailsService implements UserDetailsService {

    private final MasterRepository masterRepository;

    /**
     * Загрузить пользователя по email для аутентификации.
     *
     * @param email email пользователя
     * @return UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return masterRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("Master not found with email: " + email);
                });
    }
}