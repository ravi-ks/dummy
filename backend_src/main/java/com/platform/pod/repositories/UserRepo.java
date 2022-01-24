package com.platform.pod.repositories;

import com.platform.pod.entities.Teams;
import com.platform.pod.entities.Users;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepo extends JpaRepository<Users, Integer> {
    Users findByEmail(String email);
}
