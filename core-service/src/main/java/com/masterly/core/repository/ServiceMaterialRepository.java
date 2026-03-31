package com.masterly.core.repository;

import com.masterly.core.model.ServiceMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceMaterialRepository extends JpaRepository<ServiceMaterial, Long> {
    List<ServiceMaterial> findByServiceId(Long serviceId);
}