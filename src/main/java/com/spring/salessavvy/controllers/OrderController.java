package com.spring.salessavvy.controllers;

import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersForUser(HttpServletRequest request) {
        try {
            User user = (User) request.getAttribute("authenticatedUser");
            if (user == null) {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "User not authenticated!"));
            }

            Map<String, Object> response = orderService.getOrdersForUser(user);

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

}
