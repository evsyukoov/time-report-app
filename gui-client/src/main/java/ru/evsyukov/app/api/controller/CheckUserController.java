package ru.evsyukov.app.api.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController("/check")
@RequestMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
@Slf4j
public class CheckUserController {

    /**
     * Проверка наличия куки, логика редиректов на нужные страницы (ОК - в админ-панель, 401 - на страницу /login) реализована на фронте
     * @param authentication
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<?> checkUser(Authentication authentication, HttpServletRequest request) {
        log.info("GET /check/user");
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("User with cookie has gone");
            return ResponseEntity.ok().build();
        } else {
            log.debug("User hasn't authenificated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
