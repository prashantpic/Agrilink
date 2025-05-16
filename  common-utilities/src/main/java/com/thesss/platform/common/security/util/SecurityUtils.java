package com.thesss.platform.common.security.util;

import com.thesss.platform.common.security.jwt.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public final class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {
        // restrict instantiation
    }

    /**
     * Retrieves the current authenticated principal from Spring Security context.
     * Assumes the principal object in the Authentication is an instance of AuthenticatedPrincipal.
     *
     * @return An Optional containing the AuthenticatedPrincipal if authenticated and found, empty otherwise.
     */
    public static Optional<AuthenticatedPrincipal> getCurrentPrincipal() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AuthenticatedPrincipal) {
                return Optional.of((AuthenticatedPrincipal) principal);
            } else if (principal != null) {
                log.trace("Principal is of type {} and not AuthenticatedPrincipal. Current principal: {}",
                          principal.getClass().getName(), principal);
            }
        }
        return Optional.empty();
    }

     /**
      * Checks if a user is currently authenticated in the Spring Security context.
      *
      * @return true if a user is authenticated, false otherwise.
      */
    public static boolean isCurrentUserAuthenticated() {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
     }


    /**
     * Extracts the client's IP address from the HttpServletRequest.
     * Considers common proxy headers like X-Forwarded-For.
     *
     * @param request The HttpServletRequest from which to extract the IP.
     * @return The client IP address as a String, or null if not determinable.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headersToTry = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR", // Fallback to remote address
            "X-Real-IP" // Common header from Nginx/similar reverse proxies
        };

        for (String header : headersToTry) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                // X-Forwarded-For can contain a comma-separated list of IPs. The first one is the original client.
                return ipList.split(",")[0].trim();
            }
        }
        // If no header found, use getRemoteAddr() as a last resort
        return request.getRemoteAddr();
    }

     /**
      * Attempts to get the current HttpServletRequest from the RequestContextHolder.
      * This is useful in components where HttpServletRequest is not directly available,
      * like aspects or services, but relies on the request being processed by Spring MVC/Web.
      *
      * @return An Optional containing the HttpServletRequest if available in the current thread's context, empty otherwise.
      */
     public static Optional<HttpServletRequest> getCurrentHttpRequest() {
         try {
             return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                 .filter(ServletRequestAttributes.class::isInstance)
                 .map(ServletRequestAttributes.class::cast)
                 .map(ServletRequestAttributes::getRequest);
         } catch (IllegalStateException e) {
             // This can happen if RequestContextHolder is accessed outside a request-bound thread
             log.trace("Could not retrieve HttpServletRequest from RequestContextHolder. Not in a request context or request attributes not bound.", e);
             return Optional.empty();
         }
     }
}