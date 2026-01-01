package com.app.filter;

import com.app.util.JWTUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class JwtFilter implements Filter {

    @Override
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
        String path = request.getRequestURI();
        if (token != null) {
            DecodedJWT jwt = JWTUtil.validateToken(token);
            String role = JWTUtil.getRole(jwt);

            if (jwt != null) {
                request.setAttribute("userId", JWTUtil.getUserId(jwt));
                request.setAttribute("userRole", JWTUtil.getRole(jwt));
                //TODO : Add role-based access control here
                // if((path.startsWith("/api/admin/") || path.startsWith("/api/packages")) && !"ADMIN".equals(role)) {
                //     response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                //     response.setContentType("application/json");
                //     response.getWriter().write("{\"error\":\"Forbidden\"}");
                //     return;
                // }
                chain.doFilter(request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\"}");
    }
}
