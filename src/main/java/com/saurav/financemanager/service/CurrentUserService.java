package com.saurav.financemanager.service;

import com.saurav.financemanager.entity.User;
import com.saurav.financemanager.exception.UnauthorizedException;
import com.saurav.financemanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Unauthorized");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    }
}
