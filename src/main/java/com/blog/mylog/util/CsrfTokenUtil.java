package com.blog.mylog.util;

import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

public class CsrfTokenUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateToken(HttpSession session) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        session.setAttribute("_csrf", token);
        return token;
    }

    public static boolean validateToken(HttpSession session, String token) {
        if (token == null || session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute("_csrf");
        return token.equals(sessionToken);
    }
}