package com.hackathon.payment.auth;

import com.hackathon.payment.audit.AuditService;
import com.hackathon.payment.auth.dto.LoginRequest;
import com.hackathon.payment.auth.dto.LoginResponse;
import com.hackathon.payment.security.JwtService;
import com.hackathon.payment.user.User;
import com.hackathon.payment.user.UserRepository;
import com.hackathon.payment.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.record(user.getId(), "LOGIN", "USER", user.getUsername(), "FAILED", "Bad password");
            throw new BadCredentialsException("Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            auditService.record(user.getId(), "LOGIN", "USER", user.getUsername(), "FAILED", "User inactive");
            throw new DisabledException("User is not active");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        auditService.record(user.getId(), "LOGIN", "USER", user.getUsername(), "SUCCESS", null);
        log.info("User {} authenticated", user.getUsername());
        return new LoginResponse(token, jwtService.getExpirationSeconds());
    }
}
