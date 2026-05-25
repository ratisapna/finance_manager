package com.saurav.financemanager.service;

import com.saurav.financemanager.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CurrentUserService currentUserService;

    public UserResponse getCurrentUser() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }
}
