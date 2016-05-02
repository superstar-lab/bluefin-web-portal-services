package com.mcmcg.ico.bluefin.configuration.cors;

import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

import com.mcmcg.ico.bluefin.configuration.cors.CustomCorsRegistration;

public class CustomCorsRegistration extends CorsRegistration {

    public static final String[] REQUEST_METHOD_SUPPORTED = { "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS",
            "HEAD" };
    public static final String GLOBAL_MAPPING = "/**";

    public CustomCorsRegistration(String pathPattern) {
        super(pathPattern);
    }

    @Override
    public String getPathPattern() {
        return super.getPathPattern();
    }

    @Override
    public CorsConfiguration getCorsConfiguration() {
        return super.getCorsConfiguration();
    }

    /**
     * Holds the CORS global configurations.
     * 
     * Header samples: "Access-Control-Allow-Headers", "Origin,
     * X-Requested-With, Content-Type, Accept"
     * 
     * @return CorsConfiguration
     */
    public static CorsConfiguration getGlobalCorsConfiguration() {
        // CORS Global Configuration
        CustomCorsRegistration ccr = (CustomCorsRegistration) new CustomCorsRegistration(
                CustomCorsRegistration.GLOBAL_MAPPING).allowedOrigins("*").allowedHeaders("")
                        .allowedMethods(REQUEST_METHOD_SUPPORTED).allowCredentials(true).maxAge(3600);

        return ccr.getCorsConfiguration();
    }
}
