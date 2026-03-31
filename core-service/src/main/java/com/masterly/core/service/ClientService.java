package com.masterly.core.service;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.mapper.ClientMapper;
import com.masterly.core.model.Client;
import com.masterly.core.model.Master;
import com.masterly.core.repository.ClientRepository;
import com.masterly.core.repository.MasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final MasterRepository masterRepository;

    public Page<ClientDto> getClientsByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching clients for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Client> clients = clientRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} clients", clients.getTotalElements());

        return clients.map(clientMapper::toDto);
    }

    public ClientDto getClientByIdAndMasterId(Long id, Long masterId) {
        log.debug("Fetching client: {} for master: {}", id, masterId);

        Client client = clientRepository.findByIdAndMasterId(id, masterId)
                .orElseThrow(() -> {
                    log.error("Client not found - id: {}, master: {}", id, masterId);
                    return new RuntimeException("Client not found");
                });

        return clientMapper.toDto(client);
    }

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

        log.debug("Client details updated - name: {}, phone: {}, email: {}",
                clientDto.getFullName(), clientDto.getPhone(), clientDto.getEmail());

        Client updated = clientRepository.save(client);
        log.info("Client {} updated successfully", updated.getId());

        return clientMapper.toDto(updated);
    }

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

    public void deleteClient(Long id, Long masterId) {
        log.info("Deleting client: {} for master: {}", id, masterId);

        Client client = clientRepository.findByIdAndMasterId(id, masterId)
                .orElseThrow(() -> {
                    log.error("Client not found for deletion - id: {}, master: {}", id, masterId);
                    return new RuntimeException("Client not found");
                });

        clientRepository.delete(client);
        log.info("Client {} deleted successfully", id);
    }

    public List<ClientDto> getAllClientsByMasterId(Long masterId) {
        log.debug("Fetching all clients DTOs for master: {}", masterId);

        List<Client> clients = clientRepository.findByMasterId(masterId);
        log.debug("Found {} clients", clients.size());

        return clients.stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    public ClientDto findByEmail(String email) {
        log.debug("Finding client by email: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Client not found with email: {}", email);
                    return new RuntimeException("Client not found with email: " + email);
                });

        return clientMapper.toDto(client);
    }
}