package com.masterly.core.repository;

import com.masterly.core.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Material}.
 * Предоставляет методы для поиска материалов по мастеру.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    /**
     * Найти все материалы мастера.
     *
     * @param masterId ID мастера
     * @return список материалов мастера
     */
    List<Material> findByMasterId(Long masterId);

    /**
     * Найти материалы мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с материалами мастера
     */
    Page<Material> findByMasterId(Long masterId, Pageable pageable);
}