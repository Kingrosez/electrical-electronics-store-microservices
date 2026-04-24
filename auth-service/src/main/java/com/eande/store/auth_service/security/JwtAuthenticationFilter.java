package com.eande.store.auth_service.security;
import com.eande.store.auth_service.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Skip filtering for public endpoints (performance optimization)
    private static final String[] EXCLUDED_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/register",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs",
            "/swagger-ui"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        // No Authorization header - continue chain (will be rejected by SecurityConfig)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found for path: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        try {
            // Extract userId from token
            userId = jwtService.extractUserId(jwt);
            log.debug("Extracted userId: {} from token", userId);

            // Only set authentication if not already authenticated
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from User Service (via Feign or direct call)
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                // Validate token against user details
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,  // credentials (null because we use JWT)
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authentication successful for userId: {}", userId);
                } else {
                    log.warn("Token validation failed for userId: {}", userId);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired for path: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Token expired\",\"code\":\"TOKEN_EXPIRED\"}");
            return;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature for path: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid token signature\",\"code\":\"INVALID_TOKEN\"}");
            return;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token for path: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Malformed token\",\"code\":\"INVALID_TOKEN\"}");
            return;
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Authentication error\",\"code\":\"AUTH_ERROR\"}");
            return;
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
