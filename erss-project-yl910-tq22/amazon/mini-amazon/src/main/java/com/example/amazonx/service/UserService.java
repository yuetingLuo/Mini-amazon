package com.example.amazonx.service;

import com.example.amazonx.model.User;
import com.example.amazonx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registerNewUserAccount(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }
        User newUser = new User(username, passwordEncoder.encode(password));
        userRepository.save(newUser);
        return true;
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return true;
        }
        return false;
    }
}
