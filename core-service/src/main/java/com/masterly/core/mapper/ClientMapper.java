package com.masterly.core.mapper;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.model.Client;
import com.masterly.core.model.Master;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDto toDto(Client client) {
        ClientDto dto = new ClientDto();

        dto.setId(client.getId());
        dto.setFullName(client.getFullName());
        dto.setPhone(client.getPhone());
        dto.setEmail(client.getEmail());

        return dto;
    }

    public Client toEntity(ClientDto dto, Master master) {
        Client client = new Client();
        client.setMaster(master);
        client.setFullName(dto.getFullName());
        client.setPhone(dto.getPhone());
        client.setEmail(dto.getEmail());
        return client;
    }
}