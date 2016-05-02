package com.mcmcg.ico.bluefin.configuration.cors;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

import com.google.common.net.HttpHeaders;
import com.mcmcg.ico.bluefin.configuration.cors.CustomCorsRegistration;

@Component
public class SimpleCORSFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        // Load global CORS configuration
        CorsConfiguration ccGlobal = CustomCorsRegistration.getGlobalCorsConfiguration();

        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedOrigins()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedMethods()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedHeaders()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                String.valueOf(ccGlobal.getAllowCredentials()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(ccGlobal.getMaxAge()));

        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}
