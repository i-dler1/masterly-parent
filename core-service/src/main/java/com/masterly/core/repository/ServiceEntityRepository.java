package com.masterly.core.repository;

import com.masterly.core.model.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByMasterId(Long masterId);
    Page<ServiceEntity> findByMasterId(Long masterId, Pageable pageable);
}