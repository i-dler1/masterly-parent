package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping("/api/check-availability")
    public boolean checkAvailability(
            @RequestParam Long masterId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        log.debug("Checking availability - master: {}, date: {}, start: {}, end: {}",
                masterId, date, startTime, endTime);

        return coreServiceClient.checkAvailability(masterId, date, startTime, endTime);
    }
}