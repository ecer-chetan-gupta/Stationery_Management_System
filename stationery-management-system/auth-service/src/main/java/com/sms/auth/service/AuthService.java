package com.sms.auth.service;

import com.sms.auth.model.dto.AuthResponse;
import com.sms.auth.model.dto.LoginRequest;
import com.sms.auth.model.dto.RegisterRequest;

/**
 * Auth Service interface — defines the contract for authentication operations.
 * Implementation is in AuthServiceImpl.
 */
public interface AuthService {

    /**
     * Register a new user.
     * @param request registration details (email, password, fullName, role)
     * @return AuthResponse with JWT token and user info
     * @throws com.sms.auth.exception.UserAlreadyExistsException if email is taken
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate an existing user.
     * @param request login credentials (email, password)
     * @return AuthResponse with JWT token and user info
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are wrong
     */
    AuthResponse login(LoginRequest request);
}
