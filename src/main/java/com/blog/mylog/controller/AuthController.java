package com.blog.mylog.controller;


import com.blog.mylog.entity.User;
import com.blog.mylog.service.UserService;
import com.blog.mylog.util.XssFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/doRegister")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             HttpSession session) {
        try {
            username = XssFilter.clean(username);
            email = XssFilter.clean(email);
            
            if (username.length() < 3 || username.length() > 20) {
                return "redirect:/register?error=用户名长度3-20字符";
            }
            if (password.length() < 6) {
                return "redirect:/register?error=密码至少6位";
            }
            
            User user = userService.register(username, password, email);
            session.setAttribute("user", user);
            return "redirect:/";
        } catch (Exception e) {
            return "redirect:/register?error=" + e.getMessage();
        }
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session) {
        try {
            username = XssFilter.clean(username);
            
            if (username.isEmpty() || password.isEmpty()) {
                return "redirect:/login?error=用户名和密码不能为空";
            }
            
            User user = userService.login(username, password);
            session.setAttribute("user", user);
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login?error=" + e.getMessage();
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}