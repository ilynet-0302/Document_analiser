package com.example.Document_analiser.controller;

import com.example.Document_analiser.dto.JwtResponse;
import com.example.Document_analiser.dto.LoginRequest;
import com.example.Document_analiser.dto.RegisterRequest;
import com.example.Document_analiser.entity.Role;
import com.example.Document_analiser.entity.User;
import com.example.Document_analiser.repository.UserRepository;
import com.example.Document_analiser.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Контролер за регистрация и логин с JWT.
 * - /auth/register: създава потребител с роля (по подразбиране USER).
 * - /auth/login: валидира и връща JWT токен при успех.
 */
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                         AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /** Регистрира нов потребител. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    /** Логва потребител и връща JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String token = jwtUtil.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));
    }
} 
