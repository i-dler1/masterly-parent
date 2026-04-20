package com.masterly.core.controller;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ClientDto clientDto;
    private Long clientId;
    private Long masterId;

    @BeforeEach
    void setUp() {
        clientId = 1L;
        masterId = 1L;

        clientDto = new ClientDto();
        clientDto.setId(clientId);
        clientDto.setFullName("Иван Иванов");
        clientDto.setPhone("+375291234567");
        clientDto.setEmail("ivan@test.com");
    }

    // ==================== getClient ====================

    @Test
    void getClient_ShouldReturnClient_WhenExists() {
        // given
        when(clientService.getClientByIdAndMasterId(clientId, masterId)).thenReturn(clientDto);

        // when
        ResponseEntity<ClientDto> response = clientController.getClient(clientId, masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(clientId, response.getBody().getId());
        verify(clientService).getClientByIdAndMasterId(clientId, masterId);
    }

    @Test
    void getClient_ShouldReturnNotFound_WhenClientNotExists() {
        // given
        when(clientService.getClientByIdAndMasterId(clientId, masterId)).thenReturn(null);

        // when
        ResponseEntity<ClientDto> response = clientController.getClient(clientId, masterId);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(clientService).getClientByIdAndMasterId(clientId, masterId);
    }

    // ==================== updateClient ====================

    @Test
    void updateClient_ShouldUpdateAndReturnClient() {
        when(clientService.updateClient(any(ClientDto.class), eq(masterId))).thenReturn(clientDto);

        ResponseEntity<ClientDto> response = clientController.updateClient(clientId, masterId, clientDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<ClientDto> clientCaptor = ArgumentCaptor.forClass(ClientDto.class);
        verify(clientService).updateClient(clientCaptor.capture(), eq(masterId));

        assertEquals(clientId, clientCaptor.getValue().getId());
    }

    // ==================== getClientsPaginated ====================

    @Test
    void getClientsPaginated_ShouldReturnPageOfClients() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Pageable pageable = PageRequest.of(page, size);
        Page<ClientDto> clientPage = new PageImpl<>(Collections.singletonList(clientDto));

        when(clientService.getClientsByMasterId(eq(masterId), any(Pageable.class))).thenReturn(clientPage);

        // when
        ResponseEntity<Page<ClientDto>> response = clientController.getClientsPaginated(page, size, sortBy, sortDir, masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(clientService).getClientsByMasterId(eq(masterId), any(Pageable.class));
    }

    @Test
    void getClientsPaginated_WithDescSort_ShouldReturnPageOfClients() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<ClientDto> clientPage = new PageImpl<>(Collections.singletonList(clientDto));

        when(clientService.getClientsByMasterId(eq(masterId), any(Pageable.class))).thenReturn(clientPage);

        // when
        ResponseEntity<Page<ClientDto>> response = clientController.getClientsPaginated(page, size, sortBy, sortDir, masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(clientService).getClientsByMasterId(eq(masterId), any(Pageable.class));
    }

    // ==================== createClient ====================

    @Test
    void createClient_ShouldCreateAndReturnClient() {
        // given
        when(clientService.createClient(any(ClientDto.class), eq(masterId))).thenReturn(clientDto);

        // when
        ResponseEntity<ClientDto> response = clientController.createClient(masterId, clientDto);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(clientService).createClient(any(ClientDto.class), eq(masterId));
    }

    // ==================== deleteClient ====================

    @Test
    void deleteClient_ShouldDeleteClient() {
        // given
        doNothing().when(clientService).deleteClient(clientId, masterId);

        // when
        ResponseEntity<Void> response = clientController.deleteClient(clientId, masterId);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(clientService).deleteClient(clientId, masterId);
    }

    // ==================== getAllClients ====================

    @Test
    void getAllClients_ShouldReturnListOfClients() {
        // given
        List<ClientDto> clients = Collections.singletonList(clientDto);
        when(clientService.getAllClientsByMasterId(masterId)).thenReturn(clients);

        // when
        ResponseEntity<List<ClientDto>> response = clientController.getAllClients(masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(clientService). getAllClientsByMasterId(masterId);
    }

    @Test
    void getAllClients_ShouldReturnEmptyList_WhenNoClients() {
        // given
        when(clientService.getAllClientsByMasterId(masterId)).thenReturn(Collections.emptyList());

        // when
        ResponseEntity<List<ClientDto>> response = clientController.getAllClients(masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(clientService).getAllClientsByMasterId(masterId);
    }

    // ==================== getClientByEmail ====================

    @Test
    void getClientByEmail_ShouldReturnClient_WhenExists() {
        // given
        String email = "ivan@test.com";
        when(clientService.findByEmail(email)).thenReturn(clientDto);

        // when
        ResponseEntity<ClientDto> response = clientController.getClientByEmail(email);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(email, response.getBody().getEmail());
        verify(clientService).findByEmail(email);
    }

    @Test
    void getAllClientsForAdmin_ShouldReturnPageOfClients() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ClientDto> clientPage = new PageImpl<>(Collections.singletonList(clientDto));

        when(clientService.getClientsForAdmin(any(Pageable.class))).thenReturn(clientPage);

        // when
        ResponseEntity<Page<ClientDto>> response = clientController.getAllClientsForAdmin(page, size, sortBy, sortDir);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(clientService).getClientsForAdmin(any(Pageable.class));
    }

    @Test
    void getAllClientsForAdmin_WithDescSort_ShouldReturnPageOfClients() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<ClientDto> clientPage = new PageImpl<>(Collections.singletonList(clientDto));

        when(clientService.getClientsForAdmin(any(Pageable.class))).thenReturn(clientPage);

        // when
        ResponseEntity<Page<ClientDto>> response = clientController.getAllClientsForAdmin(page, size, sortBy, sortDir);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(clientService).getClientsForAdmin(any(Pageable.class));
    }

    // ==================== deleteClient - conflict ====================

    @Test
    void deleteClient_ShouldReturnConflict_WhenClientHasAppointments() {
        // given
        doThrow(new RuntimeException("Нельзя удалить клиента, так как у него есть записи"))
                .when(clientService).deleteClient(clientId, masterId);

        // when
        ResponseEntity<Void> response = clientController.deleteClient(clientId, masterId);

        // then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(clientService).deleteClient(clientId, masterId);
    }

    @Test
    void deleteClient_ShouldRethrowException_WhenOtherError() {
        // given
        doThrow(new RuntimeException("Some other error"))
                .when(clientService).deleteClient(clientId, masterId);

        // when & then
        assertThrows(RuntimeException.class, () -> {
            clientController.deleteClient(clientId, masterId);
        });
        verify(clientService).deleteClient(clientId, masterId);
    }

// ==================== updateClientProfile ====================

    @Test
    void updateClientProfile_ShouldUpdateAndReturnClient() {
        // given
        when(clientService.updateClientProfile(eq(clientId), any(ClientDto.class))).thenReturn(clientDto);

        // when
        ResponseEntity<ClientDto> response = clientController.updateClientProfile(clientId, clientDto);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(clientService).updateClientProfile(eq(clientId), any(ClientDto.class));
    }
}