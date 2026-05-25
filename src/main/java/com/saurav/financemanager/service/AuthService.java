package com.saurav.financemanager.service;

import com.saurav.financemanager.dto.auth.AuthMessageResponse;
import com.saurav.financemanager.dto.auth.LoginRequest;
import com.saurav.financemanager.dto.auth.RegisterRequest;
import com.saurav.financemanager.dto.auth.RegisterResponse;
import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.exception.ConflictException;
import com.saurav.financemanager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse("User registered successfully", savedUser.getId());
    }

    public AuthMessageResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return new AuthMessageResponse("Login successful");
    }

    public AuthMessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return new AuthMessageResponse("Logout successful");
    }
}
