package com.blog.mylog.service;

import com.blog.mylog.entity.User;

public interface UserService {
    User register(String username, String password, String email);
    User login(String username, String password);
    User findById(Long id);
    User findByUsername(String username);
}