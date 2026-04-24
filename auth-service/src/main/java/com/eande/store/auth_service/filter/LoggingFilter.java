
package com.eande.store.auth_service.filter;

import com.eande.store.auth_service.util.LoggingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Generate request ID
        String requestId = LoggingUtil.generateRequestId();

        // Wrap request and response to cache content for logging
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Log request
        long startTime = System.currentTimeMillis();
        LoggingUtil.logRequest(requestWrapper, requestId);

        try {
            // Process request
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Log response
            LoggingUtil.logResponse(requestWrapper, responseWrapper, requestId, executionTime);

            // Log performance for slow requests
            if (executionTime > 1000) {
                LoggingUtil.logPerformance(request.getRequestURI(), executionTime);
            }

            // Clear MDC context
            LoggingUtil.clearContext();

            // Copy content back to response
            responseWrapper.copyBodyToResponse();
        }
    }
}