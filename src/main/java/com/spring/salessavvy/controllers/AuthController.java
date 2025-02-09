package com.spring.salessavvy.controllers;

import com.spring.salessavvy.dtos.LoginDTO;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {

    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO usercred, HttpServletResponse response) {
        try {
            User user = authService.authenticate(usercred.getUsername(), usercred.getPassword());

            String token = authService.generateToken(user);

            Cookie cookie = new Cookie("authToken", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(3600);
            cookie.setDomain("localhost");
            response.addCookie(cookie);
            response.addHeader("set-cookie",
                    String.format("authToken=%s; " +
                            "HttpOnly; " +
                            "Secure; " +
                            "Max-Age=3600; " +
                            "Path=/; " +
                            "SameSite=none", token));

            Map<String, Object> responseBody = new HashMap<>();

            responseBody.put("message", "Login successful");
            responseBody.put("role", user.getRole().name());
            responseBody.put("username", user.getUsername());
            return ResponseEntity.ok(responseBody);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
}
