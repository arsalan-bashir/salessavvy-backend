package com.spring.salessavvy.services;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.spring.salessavvy.entities.*;
import com.spring.salessavvy.repositories.CartItemsRepository;
import com.spring.salessavvy.repositories.OrderItemRepository;
import com.spring.salessavvy.repositories.OrderRepository;
import com.spring.salessavvy.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    CartItemsRepository cartItemsRepository;
    UserRepository userRepository;

    public PaymentService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                          UserRepository userRepository, CartItemsRepository cartItemsRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cartItemsRepository = cartItemsRepository;
    }

    @Transactional
    public String createOrder(String username, BigDecimal totalAmount, List<OrderItem> cartItems) throws RazorpayException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RazorpayException("User not found with ID: " + username));

        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpaySecret);

        var orderRequest = new JSONObject();
        orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt","txn_"+System.currentTimeMillis());

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Order order = new Order();
        order.setOrderId(razorpayOrder.get("id"));
        order.setUserId(user.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(Status.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return razorpayOrder.get("id");
    }

    @Transactional
    public boolean verifyPayment(int userId, String orderId, String paymentId, String signature) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("razorpay_order_id", orderId);
            obj.put("razorpay_payment_id", paymentId);
            obj.put("razorpay_signature", signature);

            boolean isSignatureValid = com.razorpay.Utils.verifyPaymentSignature(obj, razorpaySecret);

            if (isSignatureValid) {
                Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new RazorpayException("Order not found with ID: " + orderId));

                order.setStatus(Status.SUCCESS);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                List<CartItems> cartItems = cartItemsRepository.findCartItemsWithProductDetails(userId);
                for (CartItems cartItem : cartItems) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProductId(cartItem.getProduct().getProductId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPricePerUnit(cartItem.getProduct().getPrice());
                    orderItem.setTotalPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                    orderItemRepository.save(orderItem);
                }

                cartItemsRepository.deleteAllCartItems(userId);
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
    }
}
