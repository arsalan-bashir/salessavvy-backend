package com.spring.salessavvy.filter;

import com.spring.salessavvy.entities.Role;
import com.spring.salessavvy.entities.User;
import com.spring.salessavvy.repositories.UserRepository;
import com.spring.salessavvy.services.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@WebFilter(urlPatterns = {"/api/*","/admin/*"})
public class AuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private final AuthService authService;
    private final UserRepository userRepository;

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/users/register",
            "/api/auth/login"
    };

    public AuthFilter(AuthService authService, UserRepository userRepository) {
        System.out.println("Filter Started");
        this.authService = authService;
        this.userRepository = userRepository;
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        try {
            executeFilterLogic(req, res, chain);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while executing filter", e);
            sendErrorResponse((HttpServletResponse) res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error");
        }
    }

    private void sendErrorResponse(HttpServletResponse res, int statusCode, String message)
            throws IOException {
        res.setStatus(statusCode);
        res.getWriter().write(message);
    }

    private void executeFilterLogic(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;

        String requestURI = httpRequest.getRequestURI();
        logger.info("Request URI: {}", requestURI);

        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            chain.doFilter(req, res);
            return;
        }

        if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            setCORSHeaders(httpResponse);
            return;
        }

        // Extract and Validate a token
        String token = getAuthTokenFromCookie(httpRequest);
        if (token == null || !authService.validateToken(token)) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or missing token");
            return;
        }

        // Extract username and verify user
        String username = authService.extractUsername(token);
        Optional<User> userExtracted = userRepository.findByUsername(username);
        if(userExtracted.isEmpty()) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User not found");
            return;
        }

        // Get Authenticated user and role
        User authenticatedUser = userExtracted.get();
        Role role = authenticatedUser.getRole();
        logger.info("Authenticated user: {}, Role: {}", authenticatedUser.getUsername(), role);

        // Role based access control
        if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Admin Access Required");
            return;
        }

        if(requestURI.startsWith("/api/") && (role != Role.ADMIN && role != Role.CUSTOMER)) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer Access Required");
            return;
        }

        httpRequest.setAttribute("authenticatedUser", authenticatedUser);
        chain.doFilter(req, res);
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String getAuthTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

}
