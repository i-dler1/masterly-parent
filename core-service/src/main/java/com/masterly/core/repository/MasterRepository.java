package com.masterly.core.repository;

import com.masterly.core.model.Master;
import com.masterly.core.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master, Long> {
    Optional<Master> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Master> findByRole(Role role);
}