package com.masterly.core.repository;

import com.masterly.core.entity.Master;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Master}.
 * Предоставляет методы для поиска мастеров по email и роли.
 */
public interface MasterRepository extends JpaRepository<Master, Long> {

    /**
     * Найти мастера по email.
     *
     * @param email email мастера
     * @return Optional с мастером
     */
    Optional<Master> findByEmail(String email);

    /**
     * Проверить существование мастера с указанным email.
     *
     * @param email email для проверки
     * @return true если мастер существует
     */
    boolean existsByEmail(String email);

    /**
     * Найти всех мастеров с указанной ролью.
     *
     * @param role роль (ADMIN, MASTER, CLIENT)
     * @return список мастеров с указанной ролью
     */
    List<Master> findByRole(String role);
}