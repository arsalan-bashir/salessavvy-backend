package com.spring.salessavvy.controllers;

import com.razorpay.RazorpayException;
import com.spring.salessavvy.entities.OrderItem;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/payment")
public class PaymentController {

    PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPaymentOrder(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            BigDecimal totalAmount = new BigDecimal(payload.get("totalAmount").toString());
            List<Map<String, Object>> cartItemsRaw = (List<Map<String, Object>>) payload.get("cartItems");

            List<OrderItem> cartItems = cartItemsRaw.stream().map(item -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId((Integer) item.get("productId"));
                orderItem.setQuantity((Integer) item.get("quantity"));
                BigDecimal pricePerUnit = new BigDecimal(item.get("pricePerUnit").toString());
                BigDecimal totalPrice = new BigDecimal(item.get("totalPrice").toString());
                orderItem.setPricePerUnit(pricePerUnit);
                orderItem.setTotalPrice(totalPrice);
                return orderItem;
            }).collect(Collectors.toList());

            String razorPayOrderId = paymentService.createOrder(authenticatedUser.getUsername(), totalAmount, cartItems);

            return ResponseEntity.ok(razorPayOrderId);
        }
        catch (RazorpayException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating Razorpay order: "+e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request Data: "+e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPaymentOrder(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized Access");
            }
            int userId = authenticatedUser.getUserId();
            String razorpayOrderId = (String) payload.get("razorpayOrderId");
            String razorpayPaymentId = (String) payload.get("razorpayPaymentId");
            String razorpaySignature = (String) payload.get("razorpaySignature");

            boolean isVerified = paymentService.verifyPayment(userId, razorpayOrderId, razorpayPaymentId, razorpaySignature);
            if (isVerified) {
                return ResponseEntity.ok("Payment verified");
            }
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment not verified");
            }
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying payment: "+e.getMessage());
        }
    }
}
