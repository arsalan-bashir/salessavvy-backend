package com.spring.salessavvy.controllers;

import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registeruser(@RequestBody User user) {
        try {
            User registeredUser = userService.userRegister(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
