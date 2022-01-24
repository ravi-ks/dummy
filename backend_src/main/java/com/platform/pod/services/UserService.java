package com.platform.pod.services;

import com.platform.pod.entities.Users;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public Users getUserById(int userId) {
        Optional<Users> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User Not Found");
        }
        return user.get();
    }

    public Users getOrCreateUser(String email, String name) {
        Users dbUser = userRepo.findByEmail(email);
        if (dbUser == null) {
            return userRepo.save(new Users(
                    0,
                    email,
                    name
            ));
        }
        if (dbUser.getName() == null) {
            dbUser.setName(name);
            return userRepo.save(dbUser);
        }
        return dbUser;
    }

    public Users createUserFromEmail(String email) {
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            user = userRepo.save(new Users(0, email, null));
        }
        return user;
    }

    public Users getUserByEmail(String email) {
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User Not Found");
        }
        return user;
    }

    public List<Users> findAll() {
        return userRepo.findAll();
    }

    public int getUserIdByEmail(String email) {
        Users user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User Not Found");
        }
        return user.getUser_id();
    }

    private boolean containsUser(int userId) {
        return userRepo.findById(userId).isPresent();
    }

    public void updateUser(Users user) {
        userRepo.save(user);
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void createMockUserWith() {
//        userRepo.save(new Users(
//                1,
//                "abc@demo.com",
//                "abc"
//        ));
//    }

}
