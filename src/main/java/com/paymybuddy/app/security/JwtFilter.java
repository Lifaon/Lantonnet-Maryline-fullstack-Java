package com.paymybuddy.app.security;

import com.paymybuddy.app.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(final JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = null;
            Cookie authCookie = null;

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            else if (request.getCookies() != null) {
                authCookie = Arrays.stream(request.getCookies())
                        .filter(cookie -> cookie.getName().equals("authToken"))
                        .findFirst().orElse(null);
                if (authCookie != null)
                    token = authCookie.getValue();
            }

            if (token != null) {
                if (jwtService.validateToken(token)) {

                    UserDetails userDetails = jwtService.getUserDetails(token);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                else if (authCookie != null) {
                    authCookie.setValue(null);
                    authCookie.setMaxAge(0);
                    response.addCookie(authCookie);
                }
            }
        }


        filterChain.doFilter(request, response);
    }
}
