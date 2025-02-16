package com.spring.salessavvy.controllers;

import com.spring.salessavvy.dtos.LoginDTO;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            User user = (User) request.getAttribute("authenticatedUser");
            authService.logout(user);
            Cookie cookie = new Cookie("authToken", "");
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Logout successful");
            return ResponseEntity.ok(responseBody);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Logout failed ::: "+e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserDetails(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "UNAUTHORIZED USER"));
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile/update")
    public ResponseEntity<Object> updateUser(@RequestBody Map<String, Object> userDetails, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "UNAUTHORIZED USER"));
            }
            if (!userDetails.get("username").equals(authenticatedUser.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "USERNAME DOES NOT MATCH"));
            }
            User updatedUser = authService.updateUser(authenticatedUser, userDetails);
            return ResponseEntity.ok(updatedUser);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Update failed :: "+e.getMessage()));
        }
    }
}
