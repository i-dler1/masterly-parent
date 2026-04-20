package com.masterly.core.service;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.mapper.ClientMapper;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import com.masterly.core.repository.AppointmentRepository;
import com.masterly.core.repository.ClientRepository;
import com.masterly.core.repository.MasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления клиентами.
 * Предоставляет бизнес-логику для создания, обновления, удаления и поиска клиентов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final MasterRepository masterRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Получить клиентов мастера с пагинацией и информацией о последней записи.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с клиентами
     */
    public Page<ClientDto> getClientsByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching clients for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Client> clients = clientRepository.findByMasterId(masterId, pageable);

        // Для каждого клиента находим дату последней записи
        Page<ClientDto> clientDtos = clients.map(client -> {
            ClientDto dto = clientMapper.toDto(client);

            // Находим последнюю запись клиента
            List<Appointment> appointments = appointmentRepository.findByClientId(client.getId());
            LocalDateTime lastAppointmentDate = appointments.stream()
                    .map(Appointment::getAppointmentDate)
                    .max(LocalDate::compareTo)
                    .map(LocalDate::atStartOfDay)
                    .orElse(null);

            dto.setLastAppointmentDate(getLastAppointmentDate(client));
            return dto;
        });

        log.debug("Found {} clients", clientDtos.getTotalElements());
        return clientDtos;
    }

    /**
     * Получить клиента по ID с проверкой принадлежности мастеру.
     *
     * @param id       ID клиента
     * @param masterId ID мастера-владельца
     * @return DTO клиента с датой последней записи
     * @throws RuntimeException если клиент не найден
     */
    public ClientDto getClientByIdAndMasterId(Long id, Long masterId) {
        log.debug("Fetching client: {} for master: {}", id, masterId);

        Client client = clientRepository.findByIdAndMasterId(id, masterId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        ClientDto dto = clientMapper.toDto(client);

        // Вычисляем дату последней записи для этого клиента
        List<Appointment> appointments = appointmentRepository.findByClientId(client.getId());
        LocalDateTime lastAppointmentDate = appointments.stream()
                .map(Appointment::getAppointmentDate)
                .max(LocalDate::compareTo)
                .map(LocalDate::atStartOfDay)
                .orElse(null);
        dto.setLastAppointmentDate(getLastAppointmentDate(client));

        return dto;
    }

    /**
     * Обновить данные клиента.
     *
     * @param clientDto DTO с новыми данными
     * @param masterId  ID мастера-владельца
     * @return обновлённый клиент
     * @throws RuntimeException если клиент не найден
     */
    public ClientDto updateClient(ClientDto clientDto, Long masterId) {
        log.info("Updating client: {} for master: {}", clientDto.getId(), masterId);

        Client client = clientRepository.findByIdAndMasterId(clientDto.getId(), masterId)
                .orElseThrow(() -> {
                    log.error("Client not found for update - id: {}, master: {}", clientDto.getId(), masterId);
                    return new RuntimeException("Client not found");
                });

        client.setFullName(clientDto.getFullName());
        client.setPhone(clientDto.getPhone());
        client.setEmail(clientDto.getEmail());
        client.setInstagram(clientDto.getInstagram());
        client.setTelegram(clientDto.getTelegram());
        client.setNotes(clientDto.getNotes());
        client.setIsRegular(clientDto.getIsRegular());

        Client updated = clientRepository.save(client);
        log.info("Client {} updated successfully", updated.getId());

        return clientMapper.toDto(updated);
    }

    /**
     * Создать нового клиента.
     *
     * @param clientDto DTO с данными клиента
     * @param masterId  ID мастера-владельца
     * @return созданный клиент
     * @throws RuntimeException если мастер не найден
     */
    public ClientDto createClient(ClientDto clientDto, Long masterId) {
        log.info("Creating new client for master: {}, name: {}", masterId, clientDto.getFullName());

        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> {
                    log.error("Master not found for client creation - masterId: {}", masterId);
                    return new RuntimeException("Master not found");
                });

        Client client = clientMapper.toEntity(clientDto, master);
        Client saved = clientRepository.save(client);
        log.info("Client created successfully with id: {}", saved.getId());

        return clientMapper.toDto(saved);
    }

    /**
     * Удалить клиента.
     *
     * @param id       ID клиента
     * @param masterId ID мастера-владельца
     * @throws RuntimeException если клиент не найден или у него есть записи
     */
    public void deleteClient(Long id, Long masterId) {
        log.info("Deleting client: {} for master: {}", id, masterId);

        Client client = clientRepository.findByIdAndMasterId(id, masterId)
                .orElseThrow(() -> {
                    log.error("Client not found for deletion - id: {}, master: {}", id, masterId);
                    return new RuntimeException("Client not found");
                });

        // Проверяем, есть ли у клиента записи
        List<Appointment> appointments = appointmentRepository.findByClientId(id);
        if (!appointments.isEmpty()) {
            log.warn("Cannot delete client {} - has existing appointments", id);
            throw new RuntimeException("Нельзя удалить клиента, так как у него есть записи");
        }

        clientRepository.delete(client);
        log.info("Client {} deleted successfully", id);
    }

    /**
     * Получить всех клиентов мастера.
     *
     * @param masterId ID мастера
     * @return список всех клиентов мастера
     */
    public List<ClientDto> getAllClientsByMasterId(Long masterId) {
        log.debug("Fetching all clients DTOs for master: {}", masterId);

        List<Client> clients = clientRepository.findByMasterId(masterId);
        log.debug("Found {} clients", clients.size());

        return clients.stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Найти клиента по email.
     *
     * @param email email клиента
     * @return DTO клиента
     * @throws RuntimeException если клиент не найден
     */
    public ClientDto findByEmail(String email) {
        log.debug("Finding client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Client not found with email: {}", email);
                    return new RuntimeException("Client not found with email: " + email);
                });

        return clientMapper.toDto(client);
    }

    /**
     * Получить всех клиентов для администратора с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница со всеми клиентами
     */
    public Page<ClientDto> getClientsForAdmin(Pageable pageable) {
        log.debug("Fetching all clients for admin");
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(clientMapper::toDto);
    }

    /**
     * Обновить профиль клиента (без привязки к мастеру).
     *
     * @param id        ID клиента
     * @param clientDto DTO с новыми данными
     * @return обновлённый клиент
     * @throws RuntimeException если клиент не найден
     */
    public ClientDto updateClientProfile(Long id, ClientDto clientDto) {
        log.info("Updating client profile: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setFullName(clientDto.getFullName());
        client.setPhone(clientDto.getPhone());
        client.setEmail(clientDto.getEmail());

        Client updated = clientRepository.save(client);
        return clientMapper.toDto(updated);
    }

    private LocalDateTime getLastAppointmentDate(Client client) {
        List<Appointment> appointments = appointmentRepository.findByClientId(client.getId());

        if (appointments.isEmpty()) {
            return null;
        }

        // Находим запись с самой поздней датой и временем
        Appointment lastAppointment = appointments.stream()
                .max(Comparator.comparing(Appointment::getAppointmentDate)
                        .thenComparing(Appointment::getStartTime))
                .orElse(null);

        return LocalDateTime.of(
                lastAppointment.getAppointmentDate(),
                lastAppointment.getStartTime()
        );
    }

    /**
     * Получить клиента по email (альтернативный метод).
     *
     * @param email email клиента
     * @return DTO клиента
     * @throws RuntimeException если клиент не найден
     */
    public ClientDto getClientByEmail(String email) {
        log.debug("Finding client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found with email: " + email));

        return clientMapper.toDto(client);
    }
}