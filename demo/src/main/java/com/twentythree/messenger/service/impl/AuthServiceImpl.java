package com.twentythree.messenger.service.impl;
// ... imports ...
import com.twentythree.messenger.dto.auth.JwtAuthResponse;
import com.twentythree.messenger.dto.auth.LoginRequest;
import com.twentythree.messenger.dto.auth.RegistrationRequest;
import com.twentythree.messenger.entity.User;
import com.twentythree.messenger.exception.BadRequestException;
import com.twentythree.messenger.repository.UserRepository;
import com.twentythree.messenger.security.JwtTokenProvider;
import com.twentythree.messenger.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    public JwtAuthResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getNickname(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        // User user = userRepository.findByNickname(loginRequest.getNickname())
        //         .orElseThrow(() -> new UsernameNotFoundException("User not found with nickname: " + loginRequest.getNickname()));
        // Here you could also generate a refresh token if needed
        return new JwtAuthResponse(jwt);
    }

    @Override
    @Transactional
    public String registerUser(RegistrationRequest registrationRequest) {
        if (userRepository.existsByNickname(registrationRequest.getNickname())) {
            throw new BadRequestException("Nickname is already taken!");
        }

        User user = new User();
        user.setNickname(registrationRequest.getNickname());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        // Set default reputation in entity or here
        // user.setReputation(BigDecimal.valueOf(5.0));

        userRepository.save(user);
        return "User registered successfully!";
    }
}