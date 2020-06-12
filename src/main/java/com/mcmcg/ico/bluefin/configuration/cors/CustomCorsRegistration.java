package com.mcmcg.ico.bluefin.configuration.cors;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

public class CustomCorsRegistration extends CorsRegistration {

    private static final String[] REQUEST_METHOD_SUPPORTED = { "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS",
            "HEAD" };
    private static final String GLOBAL_MAPPING = "/**";

    private static final String ALLOWED_HEADERS = "Origin, X-Requested-With, Content-Type, Accept, ";

    public CustomCorsRegistration(String pathPattern) {
        super(pathPattern);
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
                CustomCorsRegistration.GLOBAL_MAPPING).allowedOrigins("*").allowedHeaders(ALLOWED_HEADERS)
                        .allowedMethods(REQUEST_METHOD_SUPPORTED).allowCredentials(true).maxAge(3600);

        return ccr.getCorsConfiguration();
    }
}
