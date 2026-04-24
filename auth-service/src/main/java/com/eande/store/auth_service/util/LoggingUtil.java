package com.eande.store.auth_service.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.UUID;

@Component
@Slf4j
public class LoggingUtil {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String CORRELATION_ID_KEY = "correlationId";

    /**
     * Generate and set request ID in MDC
     */
    public static String generateRequestId() {
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID_KEY, requestId);
        return requestId;
    }

    /**
     * Set user ID in MDC for logging context
     */
    public static void setUserId(String userId) {
        MDC.put(USER_ID_KEY, userId);
    }

    /**
     * Set correlation ID for distributed tracing
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
    }

    /**
     * Clear MDC context
     */
    public static void clearContext() {
        MDC.clear();
    }

    /**
     * Log API Request
     */
    public static void logRequest(ContentCachingRequestWrapper request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String params = queryString != null ? queryString : "";

        log.info("📤 [REQUEST] requestId={} | method={} | uri={} | params={} | headers={}",
                requestId, method, uri, params, getHeaders(request));
    }

    /**
     * Log API Response
     */
    public static void logResponse(ContentCachingRequestWrapper request,
                                   ContentCachingResponseWrapper response,
                                   String requestId,
                                   long executionTimeMs) {
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (status >= 200 && status < 300) {
            log.info("📥 [RESPONSE] requestId={} | method={} | uri={} | status={} | time={}ms",
                    requestId, method, uri, status, executionTimeMs);
        } else if (status >= 400 && status < 500) {
            log.warn("⚠️ [RESPONSE] requestId={} | method={} | uri={} | status={} | time={}ms | error={}",
                    requestId, method, uri, status, executionTimeMs, response.getContentAsByteArray());
        } else if (status >= 500) {
            log.error("❌ [RESPONSE] requestId={} | method={} | uri={} | status={} | time={}ms | error={}",
                    requestId, method, uri, status, executionTimeMs, response.getContentAsByteArray());
        } else {
            log.debug("📥 [RESPONSE] requestId={} | method={} | uri={} | status={} | time={}ms",
                    requestId, method, uri, status, executionTimeMs);
        }
    }

    /**
     * Log authentication event
     */
    public static void logAuthEvent(String eventType, String email, String userId, String result, String reason) {
        switch (eventType) {
            case "LOGIN_ATTEMPT":
                log.info("🔐 [AUTH] event={} | email={} | result={}", eventType, maskEmail(email), result);
                break;
            case "LOGIN_SUCCESS":
                log.info("✅ [AUTH] event={} | userId={} | email={}", eventType, userId, maskEmail(email));
                break;
            case "LOGIN_FAILURE":
                log.warn("❌ [AUTH] event={} | email={} | reason={}", eventType, maskEmail(email), reason);
                break;
            case "LOGOUT":
                log.info("🚪 [AUTH] event={} | userId={} | email={}", eventType, userId, maskEmail(email));
                break;
            case "TOKEN_REFRESH":
                log.info("🔄 [AUTH] event={} | userId={} | result={}", eventType, userId, result);
                break;
            case "TOKEN_VALIDATION":
                log.debug("🔑 [AUTH] event={} | userId={} | result={}", eventType, userId, result);
                break;
            default:
                log.debug("[AUTH] event={} | userId={} | result={}", eventType, userId, result);
        }
    }

    /**
     * Log token event
     */
    public static void logTokenEvent(String eventType, String userId, String tokenId, String result) {
        if ("TOKEN_GENERATED".equals(eventType)) {
            log.debug("🎫 [TOKEN] event={} | userId={} | tokenId={}", eventType, userId, tokenId);
        } else if ("TOKEN_REVOKED".equals(eventType)) {
            log.info("🚫 [TOKEN] event={} | userId={} | tokenId={} | result={}", eventType, userId, tokenId, result);
        } else if ("TOKEN_EXPIRED".equals(eventType)) {
            log.warn("⏰ [TOKEN] event={} | userId={} | tokenId={}", eventType, userId, tokenId);
        } else {
            log.debug("[TOKEN] event={} | userId={} | result={}", eventType, userId, result);
        }
    }

    /**
     * Log database operation
     */
    public static void logDatabaseOperation(String operation, String entity, String entityId, long timeMs) {
        if (timeMs > 1000) {
            log.warn("🐘 [DB] operation={} | entity={} | entityId={} | time={}ms (SLOW)",
                    operation, entity, entityId, timeMs);
        } else {
            log.debug("🐘 [DB] operation={} | entity={} | entityId={} | time={}ms",
                    operation, entity, entityId, timeMs);
        }
    }

    /**
     * Log external service call
     */
    public static void logExternalCall(String service, String endpoint, String method, int statusCode, long timeMs) {
        if (statusCode >= 200 && statusCode < 300) {
            log.debug("🌐 [EXTERNAL] service={} | endpoint={} | method={} | status={} | time={}ms",
                    service, endpoint, method, statusCode, timeMs);
        } else if (statusCode >= 400 && statusCode < 500) {
            log.warn("⚠️ [EXTERNAL] service={} | endpoint={} | method={} | status={} | time={}ms",
                    service, endpoint, method, statusCode, timeMs);
        } else if (statusCode >= 500) {
            log.error("❌ [EXTERNAL] service={} | endpoint={} | method={} | status={} | time={}ms",
                    service, endpoint, method, statusCode, timeMs);
        } else {
            log.debug("🌐 [EXTERNAL] service={} | endpoint={} | method={} | status={} | time={}ms",
                    service, endpoint, method, statusCode, timeMs);
        }
    }

    /**
     * Log performance metric
     */
    public static void logPerformance(String operation, long timeMs) {
        if (timeMs > 2000) {
            log.error("⏱️ [PERFORMANCE] operation={} | time={}ms (CRITICAL)", operation, timeMs);
        } else if (timeMs > 1000) {
            log.warn("⏱️ [PERFORMANCE] operation={} | time={}ms (SLOW)", operation, timeMs);
        } else if (timeMs > 500) {
            log.info("⏱️ [PERFORMANCE] operation={} | time={}ms", operation, timeMs);
        } else {
            log.debug("⏱️ [PERFORMANCE] operation={} | time={}ms", operation, timeMs);
        }
    }

    /**
     * Log security event
     */
    public static void logSecurityEvent(String eventType, String email, String ipAddress, String details) {
        switch (eventType) {
            case "BRUTE_FORCE_ATTEMPT":
                log.warn("🛡️ [SECURITY] event={} | email={} | ip={} | details={}",
                        eventType, maskEmail(email), ipAddress, details);
                break;
            case "SUSPICIOUS_ACTIVITY":
                log.warn("⚠️ [SECURITY] event={} | email={} | ip={} | details={}",
                        eventType, maskEmail(email), ipAddress, details);
                break;
            case "ACCOUNT_LOCKED":
                log.warn("🔒 [SECURITY] event={} | email={} | ip={} | details={}",
                        eventType, maskEmail(email), ipAddress, details);
                break;
            default:
                log.info("🔐 [SECURITY] event={} | email={} | ip={}", eventType, maskEmail(email), ipAddress);
        }
    }

    /**
     * Mask email for logging (test@example.com -> t***@e***.com)
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal;
        if (localPart.length() <= 2) {
            maskedLocal = "***";
        } else {
            maskedLocal = localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1);
        }

        String maskedDomain;
        if (domain.length() <= 2) {
            maskedDomain = "***";
        } else {
            int dotIndex = domain.indexOf('.');
            if (dotIndex <= 1) {
                maskedDomain = "***";
            } else {
                maskedDomain = domain.charAt(0) + "***" + domain.substring(dotIndex);
            }
        }

        return maskedLocal + "@" + maskedDomain;
    }

    /**
     * Get headers as string (excluding sensitive ones)
     */
    private static String getHeaders(ContentCachingRequestWrapper request) {
        StringBuilder headers = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!isSensitiveHeader(headerName)) {
                headers.append(headerName).append("=").append(request.getHeader(headerName)).append("; ");
            }
        });
        return headers.toString();
    }

    /**
     * Check if header contains sensitive information
     */
    private static boolean isSensitiveHeader(String headerName) {
        String lowerHeader = headerName.toLowerCase();
        return lowerHeader.contains("authorization") ||
                lowerHeader.contains("password") ||
                lowerHeader.contains("token") ||
                lowerHeader.contains("secret") ||
                lowerHeader.contains("key");
    }
}