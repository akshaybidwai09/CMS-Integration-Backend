package com.example.cms.service;

import com.example.cms.UserApplication.User;
import com.example.cms.DAO.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String firstName, String lastName, Date dob, String email, String password) throws Exception {
        if (userRepository.existsByEmail(email)) {
            // Handle the case where a user with the given email already exists
            throw new RuntimeException("Email already in use.");
        }

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setDob(dob);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
