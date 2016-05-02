package com.mcmcg.ico.bluefin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mcmcg.ico.bluefin.configuration.cors.CustomCorsRegistration;

@SpringBootApplication
public class BluefinServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BluefinServicesApplication.class, args);
    }

    @Bean
    public WebMvcConfigurerAdapter corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                for (String mapping : loadSpecificCorsConfigurations().keySet()) {

                    CorsConfiguration cc = loadSpecificCorsConfigurations().get(mapping).getCorsConfiguration();

                    registry.addMapping(mapping)
                            .allowedOrigins(StringUtils.collectionToCommaDelimitedString(cc.getAllowedOrigins()))
                            .allowedHeaders(StringUtils.collectionToCommaDelimitedString(cc.getAllowedHeaders()))
                            .allowedMethods(StringUtils.collectionToCommaDelimitedString(cc.getAllowedMethods()))
                            .allowCredentials(cc.getAllowCredentials()).maxAge(cc.getMaxAge());
                }
            }
        };
    }

    @Bean(name = "specificCorsRegistry")
    public Map<String, CustomCorsRegistration> loadSpecificCorsConfigurations() {
        Map<String, CustomCorsRegistration> configs = new LinkedHashMap<String, CustomCorsRegistration>();

        // CORS Spec Configuration for SMC
        final CustomCorsRegistration crSmc = (CustomCorsRegistration) new CustomCorsRegistration("/api/rest/bluefin/**")
                .allowedOrigins("*").allowedHeaders("").allowedMethods(CustomCorsRegistration.REQUEST_METHOD_SUPPORTED)
                .allowCredentials(true).maxAge(3600);

        // Include configurations
        configs.put(crSmc.getPathPattern(), crSmc);

        return configs;
    }
}
