package com.spring.salessavvy.admincontrollers;

import com.spring.salessavvy.adminservices.AdminUserService;
import com.spring.salessavvy.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PutMapping("/modify")
    public ResponseEntity<?> modifyUser(@RequestBody Map<String, Object> userRequest) {
        try {
            Integer userId = (Integer) userRequest.get("userId");
            String username = (String) userRequest.get("username");
            String email = (String) userRequest.get("email");
            String phone = (String) userRequest.get("phone");
            int age = (Integer) userRequest.get("age");
            String role = (String) userRequest.get("role");

            User updatedUser = adminUserService.modifyUser(userId, username, email, phone, age, role);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", updatedUser.getUserId());
            response.put("username", updatedUser.getUsername());
            response.put("email", updatedUser.getEmail());
            response.put("phone", updatedUser.getPhone());
            response.put("age", updatedUser.getAge());
            response.put("role", updatedUser.getRole().name());
            response.put("createdAt", updatedUser.getCreatedAt());
            response.put("updatedAt", updatedUser.getUpdatedAt());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @PostMapping("/getbyid")
    public ResponseEntity<?> getUserById(@RequestBody Map<String, Integer> userRequest) {
        try {
            Integer userId = userRequest.get("userId");
            User user = adminUserService.getUserById(userId);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }
}
