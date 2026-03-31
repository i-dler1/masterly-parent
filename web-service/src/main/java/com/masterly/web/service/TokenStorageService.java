package com.masterly.web.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenStorageService {

    private static final String TOKEN_KEY = "jwt_token";
    private final HttpSession httpSession;

    public void saveToken(String token) {
        httpSession.setAttribute(TOKEN_KEY, token);
    }

    public String getToken() {
        return (String) httpSession.getAttribute(TOKEN_KEY);
    }

    public void clearToken() {
        httpSession.removeAttribute(TOKEN_KEY);
    }
}