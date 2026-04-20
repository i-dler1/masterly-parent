package com.masterly.core.repository;

import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Client}.
 * Предоставляет методы для поиска клиентов по мастеру, email и ID.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Найти всех клиентов мастера.
     *
     * @param masterId ID мастера
     * @return список клиентов мастера
     */
    List<Client> findByMasterId(Long masterId);

    /**
     * Найти клиентов мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с клиентами мастера
     */
    Page<Client> findByMasterId(Long masterId, Pageable pageable);

    /**
     * Найти клиента по ID с проверкой принадлежности мастеру.
     *
     * @param id       ID клиента
     * @param masterId ID мастера
     * @return Optional с клиентом
     */
    Optional<Client> findByIdAndMasterId(Long id, Long masterId);

    /**
     * Найти клиента по email.
     *
     * @param email email клиента
     * @return Optional с клиентом
     */
    Optional<Client> findByEmail(String email);

    /**
     * Проверить существование клиента у указанного мастера.
     *
     * @param master   мастер
     * @param clientId ID клиента
     * @return true если клиент принадлежит мастеру
     */
    boolean existsByMasterAndId(Master master, Long clientId);
}
