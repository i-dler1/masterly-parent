package com.masterly.core.service;

import com.masterly.core.dto.MasterCreateDto;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.dto.MasterUpdateDto;
import com.masterly.core.exception.ValidationException;
import com.masterly.core.mapper.MasterMapper;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import com.masterly.core.repository.ClientRepository;
import com.masterly.core.repository.MasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления мастерами.
 * Предоставляет бизнес-логику для регистрации, поиска и обновления профилей мастеров.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MasterService {

    private final MasterRepository masterRepository;
    private final MasterMapper masterMapper;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    /**
     * Найти мастера по email.
     *
     * @param email email мастера
     * @return DTO с данными мастера
     * @throws RuntimeException если мастер не найден
     */
    public MasterDto findByEmail(String email) {
        log.debug("Finding master by email: {}", email);

        Master master = masterRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Master not found with email: {}", email);
                    return new RuntimeException("Master not found with email: " + email);
                });

        return masterMapper.toDto(master);
    }

    /**
     * Зарегистрировать нового пользователя (мастера или клиента).
     * Если роль CLIENT — дополнительно создаётся запись в таблице clients.
     *
     * @param createDto DTO с данными для регистрации
     * @return DTO с данными созданного пользователя
     */
    public MasterDto register(MasterCreateDto createDto) {
        log.info("Registering new user with email: {}, role: {}", createDto.getEmail(), createDto.getRole());

        if (masterRepository.existsByEmail(createDto.getEmail())) {
            throw new ValidationException("Email already in use: " + createDto.getEmail());
        }

        String role = createDto.getRole() != null ? createDto.getRole() : "MASTER";

        Master master = new Master();
        master.setEmail(createDto.getEmail());
        master.setPasswordHash(passwordEncoder.encode(createDto.getPassword()));
        master.setFullName(createDto.getFullName());
        master.setPhone(createDto.getPhone());
        master.setRole(role);
        master.setIsActive(true);

        Master saved = masterRepository.save(master);
        log.info("User registered successfully with id: {}, role: {}", saved.getId(), saved.getRole());

        // Если роль CLIENT - создаем запись в таблице clients
        if ("CLIENT".equals(role)) {
            Client client = new Client();
            client.setFullName(createDto.getFullName());
            client.setPhone(createDto.getPhone());
            client.setEmail(createDto.getEmail());
            client.setMaster(master);  // привязываем к мастеру? или можно null
            client.setCreatedBy(master);
            client.setIsRegular(false);
            clientRepository.save(client);
            log.info("Client record created for user: {}", createDto.getEmail());
        }

        return masterMapper.toDto(saved);
    }

    /**
     * Получить мастера по ID.
     *
     * @param id ID мастера
     * @return DTO с данными мастера
     * @throws RuntimeException если мастер не найден
     */
    public MasterDto getMasterById(Long id) {
        log.debug("Fetching master by id: {}", id);

        Master master = masterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Master not found with id: {}", id);
                    return new RuntimeException("Master not found");
                });

        log.debug("Master found: {}", master.getEmail());
        return masterMapper.toDto(master);
    }

    /**
     * Обновить профиль мастера.
     *
     * @param id        ID мастера
     * @param updateDto DTO с обновлёнными данными
     * @return DTO с обновлёнными данными
     * @throws RuntimeException если мастер не найден
     */
    public MasterDto updateMaster(Long id, MasterUpdateDto updateDto) {
        log.info("Updating master with id: {}", id);

        Master master = masterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Master not found for update - id: {}", id);
                    return new RuntimeException("Master not found");
                });

        log.debug("Updating master details - name: {}, phone: {}, business: {}, specialization: {}",
                updateDto.getFullName(), updateDto.getPhone(),
                updateDto.getBusinessName(), updateDto.getSpecialization());

        master.setFullName(updateDto.getFullName());
        master.setPhone(updateDto.getPhone());
        master.setBusinessName(updateDto.getBusinessName());
        master.setSpecialization(updateDto.getSpecialization());

        Master updated = masterRepository.save(master);
        log.info("Master {} updated successfully", id);

        return masterMapper.toDto(updated);
    }

    /**
     * Получить список всех мастеров (пользователей с ролью MASTER).
     *
     * @return список DTO всех мастеров
     */
    public List<MasterDto> getAllMasters() {
        log.debug("Fetching all masters (users with MASTER role)");

        // Получаем всех мастеров с ролью MASTER
        List<Master> masters = masterRepository.findByRole("MASTER");

        return masters.stream()
                .map(masterMapper::toDto)
                .collect(Collectors.toList());
    }
}