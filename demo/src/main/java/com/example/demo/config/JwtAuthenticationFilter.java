package com.example.demo.config;

import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Mark this as a Spring bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // --- UPDATED LOGIC to handle OPTIONS requests ---
        final String requestURI = request.getRequestURI();
        final String requestMethod = request.getMethod();

        // 1. If it's a known non-secure path (auth, h2-console) OR the WebSocket handshake
        // 2. OR if it is an OPTIONS request (CORS pre-flight)
        if (requestURI.startsWith("/api/auth/") || 
            requestURI.startsWith("/h2-console") ||
            requestURI.startsWith("/ws") ||
            requestMethod.equals("OPTIONS")) { // Bypass for CORS pre-flight
            filterChain.doFilter(request, response);
            return;
        }
        // --- END UPDATED LOGIC ---

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If no token, pass to the next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token
        jwt = authHeader.substring(7); // "Bearer " is 7 chars
        username = jwtService.extractUsername(jwt);

        // If we have a username and the user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // If token is valid, create an auth token and set it in the security context
            if (jwtService.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // We don't need credentials
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // This is the line that "logs in" the user for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Pass to the next filter
        filterChain.doFilter(request, response);
    }
}
