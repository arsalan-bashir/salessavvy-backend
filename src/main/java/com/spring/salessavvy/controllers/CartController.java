package com.spring.salessavvy.controllers;

import com.spring.salessavvy.repositories.CartItemsRepository;
import com.spring.salessavvy.repositories.UserRepository;
import com.spring.salessavvy.services.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {

    CartItemService cartItemService;

    public CartController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping("/items/count")
    public ResponseEntity<Integer> countCartItems(@RequestParam("username") String username) {
        int count = cartItemService.getCartItemsCount(username);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            int productId = (int) request.get("productId");

            int quantity = request.containsKey("quantity") ? (int) request.get("quantity") : 1;
            cartItemService.addToCart(username, productId, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
}
