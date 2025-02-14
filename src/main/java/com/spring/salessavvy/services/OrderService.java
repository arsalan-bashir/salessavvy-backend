package com.spring.salessavvy.services;

import com.spring.salessavvy.entities.OrderItem;
import com.spring.salessavvy.entities.Product;
import com.spring.salessavvy.entities.ProductImage;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.repositories.OrderItemRepository;
import com.spring.salessavvy.repositories.ProductImageRepository;
import com.spring.salessavvy.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    OrderItemRepository orderItemRepository;
    ProductRepository productRepository;
    ProductImageRepository productImageRepository;

    public OrderService(OrderItemRepository orderItemRepository, ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public Map<String, Object> getOrdersForUser(User user) {
        Map<String, Object> response = new HashMap<>();

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getFullname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole().name());

        response.put("user", userInfo);

        List<OrderItem> orderItems = orderItemRepository.findSuccessfulOrderItemByUserId(user.getUserId());

        List<Map<String, Object>> products = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findById(orderItem.getProductId()).orElse(null);
            if (product == null) {
                continue;
            }

            List<ProductImage> images = productImageRepository.findByProduct_ProductId(product.getProductId());
            String imageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();

            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("order_id", orderItem.getOrder().getOrderId());
            productInfo.put("quantity", orderItem.getQuantity());
            productInfo.put("image_url", imageUrl);
            productInfo.put("total_price", orderItem.getTotalPrice());
            productInfo.put("product_id", product.getProductId());
            productInfo.put("name", product.getName());
            productInfo.put("description", product.getDescription());
            productInfo.put("price_per_unit", orderItem.getPricePerUnit());

            products.add(productInfo);
        }
        response.put("orders", products);

        return response;
    }
}