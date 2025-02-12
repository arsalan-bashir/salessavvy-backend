package com.spring.salessavvy.controllers;

import com.spring.salessavvy.entities.Product;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/products")
public class ProductController {

    ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category, HttpServletRequest request) {

        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized Access"));
            }

            List<Product> products = productService.getProductsByCategory(category);

            Map<String, Object> response = new HashMap<>();

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("username", authenticatedUser.getUsername());
            userInfo.put("name", authenticatedUser.getFullname());
            userInfo.put("email", authenticatedUser.getEmail());
            userInfo.put("role", authenticatedUser.getRole().name());
            response.put("user", userInfo);

            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productDetails = new HashMap<>();
                productDetails.put("product_id", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("description", product.getDescription());
                productDetails.put("price", product.getPrice());
                productDetails.put("stock", product.getStock());
                productList.add(productDetails);

                List<String> images = productService.getProductImages(product.getProductId());
                productDetails.put("images", images);
            }
            response.put("products", productList);
            response.put("categories", productService.getCategories());

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
