package com.masterly.core.repository;

import com.masterly.core.entity.ServiceMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link ServiceMaterial}.
 * Предоставляет методы для поиска связей услуга-материал.
 */
@Repository
public interface ServiceMaterialRepository extends JpaRepository<ServiceMaterial, Long> {

    /**
     * Найти все материалы, связанные с услугой.
     *
     * @param serviceId ID услуги
     * @return список связей услуга-материал
     */
    List<ServiceMaterial> findByServiceId(Long serviceId);

    /**
     * Найти все услуги, использующие указанный материал.
     *
     * @param materialId ID материала
     * @return список связей услуга-материал
     */
    List<ServiceMaterial> findByMaterialId(Long materialId);
}