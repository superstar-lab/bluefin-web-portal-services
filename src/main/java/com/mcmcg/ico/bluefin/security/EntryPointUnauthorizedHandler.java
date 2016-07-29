package com.mcmcg.ico.bluefin.security;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

import com.google.common.net.HttpHeaders;
import com.mcmcg.ico.bluefin.configuration.cors.CustomCorsRegistration;

@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPointUnauthorizedHandler.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AuthenticationException e) throws IOException, ServletException {
        
        // Load global CORS configuration
        CorsConfiguration ccGlobal = CustomCorsRegistration.getGlobalCorsConfiguration();
        // Adding custom headers
        List<String> allowHeaders = ccGlobal.getAllowedHeaders();
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedOrigins()));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedMethods()));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                StringUtils.collectionToCommaDelimitedString(allowHeaders));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                String.valueOf(ccGlobal.getAllowCredentials()));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(ccGlobal.getMaxAge()));
       
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.getOutputStream()
                .println("{ \"uniqueErrorId\": \"" + logException(e) + "\",\"timestamp\": \""
                        + Calendar.getInstance().getTimeInMillis() + "\", \"message\": \"" + e.getMessage()
                        + "\", \"exception\": \"" + e.getClass().getName() + "\" }");
    }

    private UUID logException(final Exception exception) {
        final UUID uniqueErrorId = UUID.randomUUID();
        LOGGER.error("[id={}] Error on rest call", uniqueErrorId, exception);

        return uniqueErrorId;
    }

}
