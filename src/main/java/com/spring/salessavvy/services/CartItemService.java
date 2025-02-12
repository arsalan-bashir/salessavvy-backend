package com.spring.salessavvy.services;

import com.spring.salessavvy.entities.CartItems;
import com.spring.salessavvy.entities.Product;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.repositories.CartItemsRepository;
import com.spring.salessavvy.repositories.ProductImageRepository;
import com.spring.salessavvy.repositories.ProductRepository;
import com.spring.salessavvy.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartItemService {

    CartItemsRepository cartItemsRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    ProductImageRepository productImageRepository;

    public CartItemService(CartItemsRepository cartItemsRepository, UserRepository userRepository,
                           ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.cartItemsRepository = cartItemsRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public int getCartItemsCount(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return cartItemsRepository.countTotalItems(user.get().getUserId());
        }
        return 0;
    }

    public void addToCart(String username, int productId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        Optional<CartItems> existingItems = cartItemsRepository.findByUserAndProduct(user.getUserId(), productId);

        if (existingItems.isPresent()) {
            CartItems cartItem = existingItems.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemsRepository.save(cartItem);
        }
        else {
            CartItems newItem = new CartItems(user, product, quantity);
            cartItemsRepository.save(newItem);
        }

    }
}
