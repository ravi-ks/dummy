package com.platform.pod.dto;

import com.platform.pod.entities.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private int userId;
    private String email;
    private String name;

    public UserDetails(Users user) {
        userId = user.getUser_id();
        email = user.getEmail();
        name = user.getName();
    }
}
