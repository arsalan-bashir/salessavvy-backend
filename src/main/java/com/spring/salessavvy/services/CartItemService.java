package com.spring.salessavvy.services;

import com.spring.salessavvy.entities.CartItems;
import com.spring.salessavvy.entities.Product;
import com.spring.salessavvy.entities.ProductImage;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.repositories.CartItemsRepository;
import com.spring.salessavvy.repositories.ProductImageRepository;
import com.spring.salessavvy.repositories.ProductRepository;
import com.spring.salessavvy.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

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


    public Map<String, Object> getCartItems(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + username));

        // Fetch cart items for the user with all details
        List<CartItems> cartItems = cartItemsRepository.findCartItemsWithProductDetails(user.getUserId());

        // Map to send response to controller
        Map<String, Object> response = new HashMap<>();

        // Map to store user info
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getFullname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole().name());
        response.put("user", userInfo);

        // List to store all product objects
        List<Map<String, Object>> products = new ArrayList<>();
        double overallTotalPrice = 0;

        // Loop through each cart item
        for(CartItems cartItem : cartItems) {
            Map<String, Object> productInfo = new HashMap<>();
            Product product = cartItem.getProduct();
            List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(product.getProductId());

            String imageUrl = null;
            if(productImages != null && !productImages.isEmpty()) {
                imageUrl = productImages.get(0).getImageUrl();
            }
            else {
                imageUrl = "https://res.cloudinary.com/dcvkfh79b/image/upload/v1739345115/salessavvy/dcerppc0eednf2bq5fpo.png";
            }

            productInfo.put("product_id", product.getProductId());
            productInfo.put("product_name", product.getName());
            productInfo.put("image_url", imageUrl);
            productInfo.put("description", product.getDescription());
            productInfo.put("price_per_unit", product.getPrice());
            productInfo.put("quantity", cartItem.getQuantity());
            productInfo.put("total_price", cartItem.getQuantity() * product.getPrice().doubleValue());
            products.add(productInfo);

            overallTotalPrice += cartItem.getQuantity() * product.getPrice().doubleValue();
        }

        // Map to store cart objects
        Map<String, Object> cart = new HashMap<>();
        cart.put("products", products);
        cart.put("overall_total_price", overallTotalPrice);

        response.put("cart", cart);
        return response;
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

    public void updateCartItemQuantity(String username, int productId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        Optional<CartItems> existingItems = cartItemsRepository.findByUserAndProduct(user.getUserId(), productId);

        if (existingItems.isPresent()) {
            CartItems cartItem = existingItems.get();
            if(quantity == 0) {
                cartItemsRepository.deleteCartItem(user.getUserId(), productId);
            }
            else {
                cartItemsRepository.updateCartItemQuantity(cartItem.getCartItemId(), quantity);
            }
        }
    }

    public void deleteCartItem(String username, int productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + username));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        cartItemsRepository.deleteCartItem(user.getUserId(), productId);

    }
}
