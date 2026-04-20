package com.masterly.core.mapper;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Маппер для преобразования между сущностью {@link Client} и DTO {@link ClientDto}.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    /**
     * Преобразовать сущность {@link Client} в {@link ClientDto}.
     *
     * @param client сущность клиента
     * @return DTO с данными клиента
     */
    ClientDto toDto(Client client);

    /**
     * Преобразовать {@link Client} в сущность {@link ClientDto}.
     *
     * @param dto DTO с данными для создания клиента
     * @param master мастер
     * @return сущность клиента
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fullName", source = "dto.fullName")
    @Mapping(target = "phone", source = "dto.phone")
    @Mapping(target = "email", source = "dto.email")
    @Mapping(target = "instagram", source = "dto.instagram")
    @Mapping(target = "telegram", source = "dto.telegram")
    @Mapping(target = "notes", source = "dto.notes")
    @Mapping(target = "isRegular", source = "dto.isRegular")
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "master", source = "master")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(ClientDto dto, Master master);
}