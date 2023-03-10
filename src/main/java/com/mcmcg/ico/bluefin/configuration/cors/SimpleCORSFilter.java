package com.mcmcg.ico.bluefin.configuration.cors;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

import com.google.common.net.HttpHeaders;
import com.mcmcg.ico.bluefin.service.PropertyService;

@Component
public class SimpleCORSFilter implements Filter {

    @Autowired
    private PropertyService propertyService;
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        // Load global CORS configuration
        CorsConfiguration ccGlobal = CustomCorsRegistration.getGlobalCorsConfiguration();

        // Adding custom headers
        List<String> allowHeaders = ccGlobal.getAllowedHeaders();
        allowHeaders.add(propertyService.getPropertyValue("TOKEN_HEADER"));

        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedOrigins()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                StringUtils.collectionToCommaDelimitedString(ccGlobal.getAllowedMethods()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                StringUtils.collectionToCommaDelimitedString(allowHeaders));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                String.valueOf(ccGlobal.getAllowCredentials()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(ccGlobal.getMaxAge()));

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    	// No need to provide any implementation
    }

    @Override
    public void destroy() {
    	// No need to provide any implementation
    }
}
