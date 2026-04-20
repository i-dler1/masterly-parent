package com.masterly.core.service;

import com.masterly.core.dto.MasterCreateDto;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.dto.MasterUpdateDto;
import com.masterly.core.entity.Client;
import com.masterly.core.exception.ValidationException;
import com.masterly.core.mapper.MasterMapper;
import com.masterly.core.entity.Master;
import com.masterly.core.repository.ClientRepository;
import com.masterly.core.repository.MasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class MasterServiceTest {

    @Mock
    private MasterRepository masterRepository;

    @Mock
    private MasterMapper masterMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private MasterService masterService;

    private Master master;
    private MasterDto masterDto;

    @BeforeEach
    void setUp() {
        master = new Master();
        master.setId(1L);

        master.setEmail("test@masterly.com");
        master.setPasswordHash("123");
        master.setFullName("Тестовый мастер");
        master.setRole("MASTER");
        master.setIsActive(true);

        masterDto = new MasterDto();
        masterDto.setId(1L);

        masterDto.setEmail("test@masterly.com");
        masterDto.setFullName("Тестовый Мастер");
        masterDto.setRole("MASTER");
    }

    @Test
    void findByEmailShouldReturnMasterDtoWhenMasterExists() {
        String email = "test@masterly.com";
        when(masterRepository.findByEmail(email)).thenReturn(Optional.of(master));
        when(masterMapper.toDto(master)).thenReturn(masterDto);

        MasterDto result = masterService.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Тестовый Мастер", result.getFullName());

        verify(masterRepository).findByEmail(email);
        verify(masterMapper).toDto(master);
    }

    @Test
    void findByEmailShouldThrowExceptionWhenMasterNotFound() {
        String email = "notfound@masterly.com";
        when(masterRepository.findByEmail(email)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> masterService.findByEmail(email));

        assertEquals("Master not found with email: " + email, exception.getMessage());

        verify(masterRepository).findByEmail(email);
        verify(masterMapper, never()).toDto(any());
    }

    @Test
    void getMasterByIdShouldReturnMasterDtoWhenMasterExists() {
        Long id = 1L;
        when(masterRepository.findById(id)).thenReturn(Optional.of(master));
        when(masterMapper.toDto(master)).thenReturn(masterDto);

        MasterDto result = masterService.getMasterById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("test@masterly.com", result.getEmail());

        verify(masterRepository).findById(id);
        verify(masterMapper).toDto(master);
    }

    @Test
    void getMasterByIdShouldThrowExceptionWhenMasterNotFound() {
        Long id = 999L;
        when(masterRepository.findById(id)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> masterService.getMasterById(id));

        assertEquals("Master not found", exception.getMessage());

        verify(masterRepository).findById(id);
        verify(masterMapper, never()).toDto(any());
    }

    @Test
    void getAllMastersShouldReturnListOfMasterDto() {
        java.util.List<Master> masters = java.util.Collections.singletonList(master);
        java.util.List<MasterDto> masterDtos = java.util.Collections.singletonList(masterDto);

//        when(roleRepository.findByName("MASTER")).thenReturn(Optional.of(role));
        when(masterRepository.findByRole("MASTER")).thenReturn(masters);
        when(masterMapper.toDto(master)).thenReturn(masterDto);

        java.util.List<MasterDto> result = masterService.getAllMasters();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@masterly.com", result.get(0).getEmail());

//        verify(roleRepository).findByName("MASTER");
        verify(masterRepository).findByRole("MASTER");
        verify(masterMapper).toDto(master);
    }

    @Test
    void updateMasterShouldUpdateAndReturnMasterDto() {
        Long id = 1L;
        MasterUpdateDto updateDto = new MasterUpdateDto();
        updateDto.setFullName("Новое Имя");
        updateDto.setPhone("+375291234567");
        updateDto.setSpecialization("Новая специализация");

        when(masterRepository.findById(id)).thenReturn(Optional.of(master));
        when(masterRepository.save(any(Master.class))).thenReturn(master);
        when(masterMapper.toDto(master)).thenReturn(masterDto);

        MasterDto result = masterService.updateMaster(id, updateDto);

        assertNotNull(result);

        verify(masterRepository).findById(id);
        verify(masterRepository).save(master);
        verify(masterMapper).toDto(master);
    }

    @Test
    void updateMaster_ShouldThrowException_WhenMasterNotFound() {
        // given
        Long id = 999L;
        MasterUpdateDto updateDto = new MasterUpdateDto();
        updateDto.setFullName("Новое Имя");
        updateDto.setPhone("+375291234567");
        updateDto.setBusinessName("Новый салон");
        updateDto.setSpecialization("Новая специализация");

        when(masterRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            masterService.updateMaster(id, updateDto);
        });

        assertEquals("Master not found", exception.getMessage());
        verify(masterRepository).findById(id);
        verify(masterRepository, never()).save(any());
        verify(masterMapper, never()).toDto(any());
    }

    @Test
    void register_ShouldCreateAndReturnMasterDto() {
        // given
        MasterCreateDto createDto = new MasterCreateDto();
        createDto.setEmail("new@masterly.com");
        createDto.setPassword("123");
        createDto.setFullName("Новый Мастер");

        when(masterRepository.save(any(Master.class))).thenReturn(master);
        when(masterMapper.toDto(master)).thenReturn(masterDto);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // when
        MasterDto result = masterService.register(createDto);

        // then
        assertNotNull(result);
        verify(masterRepository).save(any(Master.class));
        verify(masterMapper).toDto(master);
    }

    // ==================== getAllMasters ====================

    @Test
    void getAllMasters_ShouldReturnListOfMasterDto() {
        // given
        List<Master> masters = Collections.singletonList(master);
        List<MasterDto> masterDtos = Collections.singletonList(masterDto);

        when(masterRepository.findByRole("MASTER")).thenReturn(masters);
        when(masterMapper.toDto(master)).thenReturn(masterDto);

        // when
        List<MasterDto> result = masterService.getAllMasters();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@masterly.com", result.get(0).getEmail());
        verify(masterRepository).findByRole("MASTER");
        verify(masterMapper).toDto(master);
    }

    @Test
    void getAllMasters_ShouldReturnEmptyList_WhenNoMasters() {
        // given
        when(masterRepository.findByRole("MASTER")).thenReturn(Collections.emptyList());

        // when
        List<MasterDto> result = masterService.getAllMasters();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(masterRepository).findByRole("MASTER");
        verify(masterMapper, never()).toDto(any());
    }

// ==================== updateMaster ====================

    @Test
    void updateMaster_ShouldUpdateAllFields() {
        // given
        Long id = 1L;
        MasterUpdateDto updateDto = MasterUpdateDto.builder()
                .fullName("Новое Имя")
                .phone("+375291234567")
                .businessName("Новый Салон")
                .specialization("Новая Специализация")
                .build();

        when(masterRepository.findById(id)).thenReturn(Optional.of(master));
        when(masterRepository.save(any(Master.class))).thenReturn(master);
        when(masterMapper.toDto(master)).thenReturn(masterDto);

        // when
        MasterDto result = masterService.updateMaster(id, updateDto);

        // then
        assertNotNull(result);
        assertEquals("Новое Имя", master.getFullName());
        assertEquals("+375291234567", master.getPhone());
        assertEquals("Новый Салон", master.getBusinessName());
        assertEquals("Новая Специализация", master.getSpecialization());
        verify(masterRepository).findById(id);
        verify(masterRepository).save(master);
        verify(masterMapper).toDto(master);
    }

// ==================== register ====================

    @Test
    void register_ShouldCreateClient_WhenRoleIsCLIENT() {
        // given
        MasterCreateDto createDto = MasterCreateDto.builder()
                .email("client@test.com")
                .password("123")
                .fullName("Клиент Тест")
                .phone("+375291234567")
                .role("CLIENT")
                .build();

        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPassword");
        when(masterRepository.save(any(Master.class))).thenAnswer(invocation -> {
            Master m = invocation.getArgument(0);
            m.setId(2L);
            return m;
        });
        when(masterMapper.toDto(any(Master.class))).thenReturn(masterDto);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MasterDto result = masterService.register(createDto);

        // then
        assertNotNull(result);
        verify(masterRepository).save(any(Master.class));
        verify(clientRepository).save(any(Client.class));
        verify(masterMapper).toDto(any(Master.class));
    }

    @Test
    void register_ShouldSetDefaultRoleMASTER_WhenRoleIsNull() {
        // given
        MasterCreateDto createDto = MasterCreateDto.builder()
                .email("master@test.com")
                .password("123")
                .fullName("Мастер Тест")
                .phone("+375291234567")
                .role(null)
                .build();

        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPassword");
        when(masterRepository.save(any(Master.class))).thenAnswer(invocation -> {
            Master m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(masterMapper.toDto(any(Master.class))).thenReturn(masterDto);

        // when
        masterService.register(createDto);

        // then
        verify(masterRepository).save(argThat(m -> "MASTER".equals(m.getRole())));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void register_ShouldNotCreateClient_WhenRoleIsMASTER() {
        // given
        MasterCreateDto createDto = MasterCreateDto.builder()
                .email("master@test.com")
                .password("123")
                .fullName("Мастер Тест")
                .phone("+375291234567")
                .role("MASTER")
                .build();

        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPassword");
        when(masterRepository.save(any(Master.class))).thenAnswer(invocation -> {
            Master m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(masterMapper.toDto(any(Master.class))).thenReturn(masterDto);

        // when
        masterService.register(createDto);

        // then
        verify(masterRepository).save(any(Master.class));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowValidationException_WhenEmailAlreadyExists() {
        // given
        MasterCreateDto createDto = MasterCreateDto.builder()
                .email("existing@test.com")
                .password("123")
                .fullName("Тест")
                .phone("+375291234567")
                .role("MASTER")
                .build();

        when(masterRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        // when & then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            masterService.register(createDto);
        });

        assertEquals("Email already in use: existing@test.com", exception.getMessage());
        verify(masterRepository).existsByEmail(createDto.getEmail());
        verify(masterRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
