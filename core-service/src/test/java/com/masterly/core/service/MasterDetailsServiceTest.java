package com.masterly.core.service;

import com.masterly.core.entity.Master;
import com.masterly.core.repository.MasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterDetailsServiceTest {

    @Mock
    private MasterRepository masterRepository;

    @InjectMocks
    private MasterDetailsService masterDetailsService;

    private Master master;

    @BeforeEach
    void setUp() {
        master = new Master();
        master.setId(1L);
        master.setEmail("test@masterly.com");
        master.setPasswordHash("123");
        master.setFullName("Тестовый Мастер");
        master.setRole("MASTER");
        master.setIsActive(true);
    }

    // ==================== loadUserByUsername ====================

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // given
        String email = "test@masterly.com";
        when(masterRepository.findByEmail(email)).thenReturn(Optional.of(master));

        // when
        UserDetails result = masterDetailsService.loadUserByUsername(email);

        // then
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("123", result.getPassword());
        assertTrue(result.isEnabled());
        verify(masterRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // given
        String email = "notfound@masterly.com";
        when(masterRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            masterDetailsService.loadUserByUsername(email);
        });
        verify(masterRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserWithAuthorities_WhenUserExists() {
        // given
        String email = "test@masterly.com";
        when(masterRepository.findByEmail(email)).thenReturn(Optional.of(master));

        // when
        UserDetails result = masterDetailsService.loadUserByUsername(email);

        // then
        assertNotNull(result);
        assertFalse(result.getAuthorities().isEmpty());
        assertEquals("ROLE_MASTER", result.getAuthorities().iterator().next().getAuthority());
        verify(masterRepository).findByEmail(email);
    }
}