package com.masterly.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        log.debug("Showing login page");
        return "login";
    }

    @GetMapping("/")
    public String home() {
        log.debug("Showing home page");
        return "index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}