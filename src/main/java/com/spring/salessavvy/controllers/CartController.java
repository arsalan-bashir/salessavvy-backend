package com.spring.salessavvy.controllers;

import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.CartItemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
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

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItems(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized Access"));
        }

        try {
            Map<String, Object> cartItems = cartItemService.getCartItems(authenticatedUser.getUsername());
            return ResponseEntity.ok(cartItems);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @PutMapping("/update")
    public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            int productId = (int) request.get("productId");
            int quantity = (int) request.get("quantity");

            cartItemService.updateCartItemQuantity(username, productId, quantity);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            int productId = (int) request.get("productId");

            cartItemService.deleteCartItem(username, productId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
