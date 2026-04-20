package com.masterly.core.service;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.mapper.ClientMapper;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import com.masterly.core.repository.AppointmentRepository;
import com.masterly.core.repository.ClientRepository;
import com.masterly.core.repository.MasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MasterRepository masterRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private Master master;
    private Client client;
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        master = new Master();
        master.setId(1L);
        master.setEmail("master@masterly.com");
        master.setFullName("Тестовый мастер");

        client = new Client();
        client.setId(1L);
        client.setEmail("ivan@test.com");
        client.setFullName("Иван Иванов");
        client.setPhone("+375441234567");
        client.setMaster(master);

        clientDto = new ClientDto();
        clientDto.setId(1L);
        clientDto.setEmail("ivan@test.com");
        clientDto.setFullName("Иван Иванов");
        clientDto.setPhone("+375441234567");
    }


    @Test
    void getClientsByMasterIdShouldReturnPageOfClientDto() {
        Long masterId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client));

        when(clientRepository.findByMasterId(masterId, pageable)).thenReturn(clientPage);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        Page<ClientDto> result = clientService.getClientsByMasterId(masterId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Иван Иванов", result.getContent().get(0).getFullName());

        verify(clientRepository).findByMasterId(masterId, pageable);
        verify(clientMapper).toDto(client);
    }

    @Test
    void getClientByIdAndMasterIdShouldReturnClientDtoWhenClientExists() {
        Long clientId = 1L;
        Long masterId = 1L;
        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.getClientByIdAndMasterId(clientId, masterId);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getFullName());
        assertEquals("+375441234567", result.getPhone());

        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(clientMapper).toDto(client);
    }

    @Test
    void getClientByIdAndMasterId_ShouldThrowException_WhenClientNotFound() {
        // given
        Long clientId = 999L;
        Long masterId = 1L;
        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.getClientByIdAndMasterId(clientId, masterId);
        });

        assertEquals("Client not found", exception.getMessage());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        // Удали эту строку или оставь, но ошибка не должна быть
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void updateClientShouldUpdateAndReturnClientDto() {
        Long clientId = 1L;
        Long masterId = 1L;

        ClientDto updateDto = new ClientDto();
        updateDto.setId(clientId);
        updateDto.setFullName("Петр Петров");
        updateDto.setPhone("+375298765432");
        updateDto.setEmail("petr@test.com");

        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.updateClient(updateDto, masterId);

        assertNotNull(result);

        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(clientRepository).save(client);
        verify(clientMapper).toDto(client);
    }

    @Test
    void updateClient_ShouldThrowException_WhenClientNotFound() {
        // given
        Long clientId = 999L;
        Long masterId = 1L;
        ClientDto updateDto = new ClientDto();
        updateDto.setId(clientId);
        updateDto.setFullName("Несуществующий");
        updateDto.setPhone("+375291234567");
        updateDto.setEmail("notfound@test.com");

        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.updateClient(updateDto, masterId);
        });

        assertEquals("Client not found", exception.getMessage());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(clientRepository, never()).save(any());
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void createClientShouldCreateAndReturnClientDto() {
        Long masterId = 1L;
        ClientDto createDto = new ClientDto();
        createDto.setFullName("Новый клиент");
        createDto.setPhone("+325448765432");
        createDto.setEmail("new@test.com");

        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(clientMapper.toEntity(createDto, master)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        ClientDto result = clientService.createClient(createDto, masterId);

        assertNotNull(result);

        verify(masterRepository).findById(masterId);
        verify(clientMapper).toEntity(createDto, master);
        verify(clientRepository).save(client);
        verify(clientMapper).toDto(client);
    }

    @Test
    void createClientShouldThrowExceptionWhenMasterNotFound() {
        Long masterId = 999L;
        ClientDto createDto = new ClientDto();
        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.createClient(createDto, masterId);
        });

        assertEquals("Master not found", exception.getMessage());
        verify(masterRepository).findById(masterId);
        verify(clientMapper, never()).toEntity(any(), any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClientShouldDeleteClient_WhenClientExists() {
        Long clientId = 1L;
        Long masterId = 1L;
        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).delete(client);

        clientService.deleteClient(clientId, masterId);

        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(clientRepository).delete(client);
    }

    @Test
    void deleteClientShouldThrowException_WhenClientNotFound() {
        Long clientId = 999L;
        Long masterId = 1L;
        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.deleteClient(clientId, masterId);
        });

        assertEquals("Client not found", exception.getMessage());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void deleteClient_ShouldThrowException_WhenClientHasAppointments() {
        // given
        Long clientId = 1L;
        Long masterId = 1L;
        List<Appointment> appointments = Collections.singletonList(new Appointment());

        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        when(appointmentRepository.findByClientId(clientId)).thenReturn(appointments);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.deleteClient(clientId, masterId);
        });

        assertEquals("Нельзя удалить клиента, так как у него есть записи", exception.getMessage());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(appointmentRepository).findByClientId(clientId);
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void getAllClientsByMasterIdShouldReturnListOfClientDto() {
        Long masterId = 1L;
        java.util.List<Client> clients = java.util.Collections.singletonList(client);
        java.util.List<ClientDto> clientDtos = java.util.Collections.singletonList(clientDto);

        when(clientRepository.findByMasterId(masterId)).thenReturn(clients);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        java.util.List<ClientDto> result = clientService.getAllClientsByMasterId(masterId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Иван Иванов", result.get(0).getFullName());

        verify(clientRepository).findByMasterId(masterId);
        verify(clientMapper).toDto(client);
    }

    @Test
    void getAllClientsByMasterIdShouldReturnEmptyListWhenNoClients() {
        Long masterId = 1L;
        when(clientRepository.findByMasterId(masterId)).thenReturn(Collections.emptyList());

        java.util.List<ClientDto> result = clientService.getAllClientsByMasterId(masterId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository).findByMasterId(masterId);
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void findByEmail_ShouldReturnClientDto_WhenClientExists() {
        // given
        String email = "ivan@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        // when
        ClientDto result = clientService.findByEmail(email);

        // then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Иван Иванов", result.getFullName());

        verify(clientRepository).findByEmail(email);
        verify(clientMapper).toDto(client);
    }

    @Test
    void findByEmail_ShouldThrowException_WhenClientNotFound() {
        // given
        String email = "notfound@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.findByEmail(email);
        });

        assertEquals("Client not found with email: " + email, exception.getMessage());
        verify(clientRepository).findByEmail(email);
        verify(clientMapper, never()).toDto(any());
    }

    @Test
    void getClientsForAdmin_ShouldReturnPageOfClientDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client));

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        // when
        Page<ClientDto> result = clientService.getClientsForAdmin(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(clientRepository).findAll(pageable);
        verify(clientMapper).toDto(client);
    }

    // ==================== updateClientProfile ====================

    @Test
    void updateClientProfile_ShouldUpdateAndReturnClientDto() {
        // given
        Long clientId = 1L;
        ClientDto updateDto = new ClientDto();
        updateDto.setFullName("Обновлённый Клиент");
        updateDto.setPhone("+375331112233");
        updateDto.setEmail("updated@test.com");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        // when
        ClientDto result = clientService.updateClientProfile(clientId, updateDto);

        // then
        assertNotNull(result);
        assertEquals("Обновлённый Клиент", client.getFullName());
        assertEquals("+375331112233", client.getPhone());
        assertEquals("updated@test.com", client.getEmail());
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
        verify(clientMapper).toDto(client);
    }

    @Test
    void updateClientProfile_ShouldThrowException_WhenClientNotFound() {
        // given
        Long clientId = 999L;
        ClientDto updateDto = new ClientDto();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.updateClientProfile(clientId, updateDto);
        });

        assertEquals("Client not found", exception.getMessage());
        verify(clientRepository).findById(clientId);
        verify(clientRepository, never()).save(any());
        verify(clientMapper, never()).toDto(any());
    }

// ==================== getClientByEmail ====================

    @Test
    void getClientByEmail_ShouldReturnClientDto_WhenClientExists() {
        // given
        String email = "ivan@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);

        // when
        ClientDto result = clientService.getClientByEmail(email);

        // then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(clientRepository).findByEmail(email);
        verify(clientMapper).toDto(client);
    }

    @Test
    void getClientByEmail_ShouldThrowException_WhenClientNotFound() {
        // given
        String email = "notfound@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.getClientByEmail(email);
        });

        assertEquals("Client not found with email: " + email, exception.getMessage());
        verify(clientRepository).findByEmail(email);
        verify(clientMapper, never()).toDto(any());
    }

// ==================== getClientByIdAndMasterId with appointments ====================

    @Test
    void getClientByIdAndMasterId_ShouldSetLastAppointmentDate_WhenAppointmentsExist() {
        // given
        Long clientId = 1L;
        Long masterId = 1L;

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setAppointmentDate(LocalDate.of(2026, 4, 15));
        appointment.setStartTime(LocalTime.of(10, 0));

        List<Appointment> appointments = Collections.singletonList(appointment);

        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);
        when(appointmentRepository.findByClientId(clientId)).thenReturn(appointments);

        // when
        ClientDto result = clientService.getClientByIdAndMasterId(clientId, masterId);

        // then
        assertNotNull(result);
        assertNotNull(result.getLastAppointmentDate());
        assertEquals(LocalDateTime.of(2026, 4, 15, 10, 0), result.getLastAppointmentDate());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(appointmentRepository, times(2)).findByClientId(clientId);
    }

    @Test
    void getClientByIdAndMasterId_ShouldSetNullLastAppointmentDate_WhenNoAppointments() {
        // given
        Long clientId = 1L;
        Long masterId = 1L;

        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);
        when(appointmentRepository.findByClientId(clientId)).thenReturn(Collections.emptyList());

        // when
        ClientDto result = clientService.getClientByIdAndMasterId(clientId, masterId);

        // then
        assertNotNull(result);
        assertNull(result.getLastAppointmentDate());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(appointmentRepository, times(2)).findByClientId(clientId);
    }

    @Test
    void getLastAppointmentDate_ShouldReturnNull_WhenNoAppointments() {
        // given
        Long clientId = 1L;
        Long masterId = 1L;

        when(clientRepository.findByIdAndMasterId(clientId, masterId)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(clientDto);
        when(appointmentRepository.findByClientId(clientId)).thenReturn(Collections.emptyList());

        // when
        ClientDto result = clientService.getClientByIdAndMasterId(clientId, masterId);

        // then
        assertNull(result.getLastAppointmentDate());
        verify(clientRepository).findByIdAndMasterId(clientId, masterId);
        verify(appointmentRepository, times(2)).findByClientId(clientId);
    }

    @Test
    void getLastAppointmentDate_ShouldReturnNull_WhenLastAppointmentIsNull() throws Exception {
        // given
        Client client = new Client();
        client.setId(1L);

        // Пустой список - lastAppointment будет null
        when(appointmentRepository.findByClientId(client.getId())).thenReturn(Collections.emptyList());

        // when - вызываем приватный метод через Reflection
        java.lang.reflect.Method method = ClientService.class.getDeclaredMethod("getLastAppointmentDate", Client.class);
        method.setAccessible(true);
        LocalDateTime result = (LocalDateTime) method.invoke(clientService, client);

        // then
        assertNull(result);
    }
}
