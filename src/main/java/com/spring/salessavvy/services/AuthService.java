package com.spring.salessavvy.services;

import com.spring.salessavvy.entities.JWTToken;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.repositories.JWTTokenRepository;
import com.spring.salessavvy.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {

    private final Key SECRET_KEY;
    private final UserRepository userRepository;
    private final JWTTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, JWTTokenRepository tokenRepository, @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.SECRET_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    }
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("Username " + username + " not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        return user;
    }

    public String generateToken(User user) {
        String token;

        JWTToken existingToken = tokenRepository.findByUserId(user.getUserId());

        LocalDateTime now = LocalDateTime.now();

        if (existingToken != null && now.isBefore(existingToken.getExpiresAt())) {
            token = existingToken.getToken();
        }
        else {
            token = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("role", user.getRole().name())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                    .compact();
            if(existingToken != null) {
                tokenRepository.delete(existingToken);
            }
            JWTToken newToken = new JWTToken(user, token, LocalDateTime.now().plusHours(1));
            tokenRepository.save(newToken);
        }
        return token;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);

            Optional<JWTToken> jwtToken = tokenRepository.findByToken(token);

            if(jwtToken.isPresent()) {
                return jwtToken.get().getExpiresAt().isAfter(LocalDateTime.now());
            }
            return false;
        }
        catch (Exception e) {
            System.err.println("Token Validation Failed "+e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
