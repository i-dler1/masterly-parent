package com.masterly.web.config;

import com.masterly.web.service.TokenStorageService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignClientInterceptor implements RequestInterceptor {

    private final TokenStorageService tokenStorageService;

    @Override
    public void apply(RequestTemplate template) {
        String token = tokenStorageService.getToken();

        if (token != null && !token.isEmpty()) {
            log.debug("Adding Authorization token to request: {}", template.url());
            template.header("Authorization", "Bearer " + token);
        } else {
            log.debug("No token found for request to: {}", template.url());
        }
    }
}