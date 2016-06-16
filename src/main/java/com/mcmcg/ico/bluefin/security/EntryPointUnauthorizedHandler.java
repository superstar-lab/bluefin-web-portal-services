package com.mcmcg.ico.bluefin.security;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPointUnauthorizedHandler.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AuthenticationException e) throws IOException, ServletException {
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