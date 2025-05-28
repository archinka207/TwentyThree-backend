package com.twentythree.messenger.service;
import com.twentythree.messenger.dto.auth.JwtAuthResponse;
import com.twentythree.messenger.dto.auth.LoginRequest;
import com.twentythree.messenger.dto.auth.RegistrationRequest;

public interface AuthService {
    JwtAuthResponse loginUser(LoginRequest loginRequest);
    String registerUser(RegistrationRequest registrationRequest);
}