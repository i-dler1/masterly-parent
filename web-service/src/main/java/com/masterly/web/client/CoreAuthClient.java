package com.masterly.web.client;

import com.masterly.web.dto.LoginRequest;
import com.masterly.web.dto.AuthResponse;
import com.masterly.web.dto.MasterRegisterRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "core-auth", url = "${core.service.url}")
public interface CoreAuthClient {

    @PostMapping("/api/auth/login")
    AuthResponse login(@RequestBody LoginRequest request);

    @PostMapping("/api/masters")
    AuthResponse register(@RequestBody MasterRegisterRequest request);
}