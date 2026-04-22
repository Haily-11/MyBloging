package com.blog.mylog.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (uri.equals("/") || uri.startsWith("/article/") ||
            uri.equals("/login") || uri.equals("/register") ||
            uri.startsWith("/css/") || uri.startsWith("/js/") ||
            uri.startsWith("/images/") || uri.equals("/doWrite") || 
            uri.equals("/doEdit") || uri.startsWith("/delete/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}