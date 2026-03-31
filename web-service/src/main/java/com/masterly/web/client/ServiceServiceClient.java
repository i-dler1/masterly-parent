package com.masterly.web.client;

import com.masterly.web.dto.ServiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "core-service", url = "${core.service.url}", contextId = "serviceClient")
public interface ServiceServiceClient {

    @GetMapping("/api/services")
    List<ServiceDto> getServices(@RequestParam("masterId") Long masterId);

    @PostMapping("/api/services")
    ServiceDto createService(@RequestParam("masterId") Long masterId, @RequestBody ServiceDto serviceDto);
}