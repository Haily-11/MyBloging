package com.blog.mylog.service.impl;

import com.blog.mylog.entity.User;
import com.blog.mylog.repository.UserRepository;
import com.blog.mylog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNickname(username);
        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        System.out.println("=== Login Debug ===");
        System.out.println("Username: " + username);
        System.out.println("Input password: " + password);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("User not found!");
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();
        System.out.println("DB password hash: " + user.getPassword());
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("Password matches: " + matches);
        
        if (!matches) {
            throw new RuntimeException("Password error");
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
}