package com.platform.pod.controllers;

import com.platform.pod.common.Constants;
import com.platform.pod.dto.UserDetails;
import com.platform.pod.entities.Users;
import com.platform.pod.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    List<Users> findAll() {
        return userService.findAll();
    }

    @GetMapping("/login")
    UserDetails getUserInfo(@AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email, @AuthenticationPrincipal(expression = "claims['name']") String name) {
        return new UserDetails(
                userService.getOrCreateUser(email, name)
        );
    }
}
