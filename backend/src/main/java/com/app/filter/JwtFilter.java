package com.app.filter;

import com.app.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.http.Cookie;

// Apply to all API routes (but NOT auth routes)@WebFilter("/api/*")
public class JwtFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String token = null;

        // 1. SEARCH FOR COOKIE
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. VALIDATE TOKEN
        if (token != null) {
            Claims claims = JwtUtil.validateToken(token);
            if (claims != null) {
                request.setAttribute("userId", claims.get("id"));
                request.setAttribute("userRole", claims.get("role"));
                chain.doFilter(request, response);
                return;
            }
        }

        // 3. FAIL
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized\"}");
    }
}