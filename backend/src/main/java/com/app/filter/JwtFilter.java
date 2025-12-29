package com.app.filter;

import com.app.util.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT; // Use Auth0 Interface
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class JwtFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            // FIX: Use the new Auth0 method
            DecodedJWT jwt = JwtUtil.validateToken(token);
            
            if (jwt != null) {
                // Extract data using the Auth0 way
                request.setAttribute("userId", JwtUtil.getUserId(jwt));
                request.setAttribute("userRole", JwtUtil.getRole(jwt));
                chain.doFilter(request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\": \"Unauthorized\"}");
    }
}