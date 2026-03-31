package com.masterly.core.controller;

import com.masterly.core.dto.LoginRequest;
import com.masterly.core.dto.LoginResponse;
import com.masterly.core.model.Master;
import com.masterly.core.security.JwtUtil;
import com.masterly.core.service.MasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MasterService masterService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());

        // Аутентификация
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Устанавливаем аутентификацию в контекст
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication successful for user: {}", loginRequest.getEmail());

        // Получаем UserDetails из аутентификации
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Генерируем JWT
        String jwt = jwtUtil.generateToken(userDetails);
        log.debug("JWT generated for user: {}", loginRequest.getEmail());

        // Получаем мастера для дополнительной информации
        Master master = masterService.findByEmail(loginRequest.getEmail());

        log.info("User logged in successfully: {}", loginRequest.getEmail());

        // Возвращаем ответ
        return ResponseEntity.ok(new LoginResponse(
                jwt,
                master.getId(),
                master.getEmail(),
                master.getFullName(),
                master.getRole().getName()
        ));
    }
}