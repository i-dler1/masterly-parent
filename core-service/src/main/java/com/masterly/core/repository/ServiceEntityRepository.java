package com.masterly.core.repository;

import com.masterly.core.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link ServiceEntity}.
 * Предоставляет методы для поиска услуг по мастеру.
 */
public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {

    /**
     * Найти все услуги мастера.
     *
     * @param masterId ID мастера
     * @return список услуг мастера
     */
    List<ServiceEntity> findByMasterId(Long masterId);

    /**
     * Найти услуги мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с услугами мастера
     */
    Page<ServiceEntity> findByMasterId(Long masterId, Pageable pageable);
}