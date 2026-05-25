package com.saurav.financemanager.dto.user;

import com.saurav.financemanager.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String username;

    private String fullName;

    private String phoneNumber;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getPhoneNumber()
        );
    }
}
