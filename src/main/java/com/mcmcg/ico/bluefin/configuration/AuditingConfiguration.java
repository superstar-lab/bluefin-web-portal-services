package com.mcmcg.ico.bluefin.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;

@Configuration
@EnableJpaAuditing
public class AuditingConfiguration {

    @Bean
    public AuditorAware<User> auditorProvider() {
        return new SpringSecurityAuditorAware();

    }

    class SpringSecurityAuditorAware implements AuditorAware<User> {

        public User getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            return ((SecurityUser) authentication.getPrincipal()).getUser();
        }
    }
}
