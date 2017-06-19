package com.mcmcg.ico.bluefin.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mcmcg.ico.bluefin.security.model.SecurityUser;

@Configuration
public class AuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();

    }

    class SpringSecurityAuditorAware implements AuditorAware<String> {
    	@Override
        public String getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            return ((SecurityUser) authentication.getPrincipal()).getUser().getUsername();
        }
    }
}
