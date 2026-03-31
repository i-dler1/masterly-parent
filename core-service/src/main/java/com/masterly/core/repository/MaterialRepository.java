package com.masterly.core.repository;

import com.masterly.core.model.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByMasterId(Long masterId);
    Page<Material> findByMasterId(Long masterId, Pageable pageable);
}