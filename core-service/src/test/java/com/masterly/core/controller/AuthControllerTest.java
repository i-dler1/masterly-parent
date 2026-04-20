package com.masterly.core.controller;

import com.masterly.core.dto.LoginRequest;
import com.masterly.core.dto.LoginResponse;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.security.JwtUtil;
import com.masterly.core.service.MasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MasterService masterService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private MasterDto masterDto;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@masterly.com");
        loginRequest.setPassword("123");

        masterDto = new MasterDto();
        masterDto.setId(1L);
        masterDto.setEmail("test@masterly.com");
        masterDto.setFullName("Тестовый Мастер");
        masterDto.setRole("MASTER");

        jwtToken = "eyJhbGciOiJIUzI1NiJ9.test";
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsValid() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(jwtToken);
        when(masterService.findByEmail(loginRequest.getEmail())).thenReturn(masterDto);

        // when
        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jwtToken, response.getBody().getToken());
        assertEquals(masterDto.getId(), response.getBody().getId());
        assertEquals(masterDto.getEmail(), response.getBody().getEmail());
        assertEquals(masterDto.getFullName(), response.getBody().getName());
        assertEquals("MASTER", response.getBody().getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
        verify(masterService).findByEmail(loginRequest.getEmail());
    }
}