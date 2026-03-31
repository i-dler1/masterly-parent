package com.masterly.core.repository;

import com.masterly.core.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByMasterId(Long masterId);
    Page<Client> findByMasterId(Long masterId, Pageable pageable);
    Optional<Client> findByIdAndMasterId(Long id, Long masterId);
    Optional<Client> findByEmail(String email);
}
