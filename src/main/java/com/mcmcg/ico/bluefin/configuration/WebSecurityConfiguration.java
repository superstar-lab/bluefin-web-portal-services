package com.mcmcg.ico.bluefin.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mcmcg.ico.bluefin.security.AuthenticationTokenFilter;
import com.mcmcg.ico.bluefin.security.CustomAccessDeniedHandler;
import com.mcmcg.ico.bluefin.security.EntryPointUnauthorizedHandler;
import com.mcmcg.ico.bluefin.security.service.SecurityService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private EntryPointUnauthorizedHandler unauthorizedHandler;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
        authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationTokenFilter;
    }

    @Bean
    public SecurityService securityService() {
        return this.securityService;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        final String apiBaseURL = "/api";
        final String transactionsApiBaseURL = apiBaseURL + "/transactions";
        final String sessionApiBaseURL = apiBaseURL + "/session";
        final String usersApiBaseURL = apiBaseURL + "/users";
        final String legalEntitiesApiBaseURL = apiBaseURL + "/legal-entities";
        final String rolesApiBaseURL = apiBaseURL + "/roles";
        final String internalResponseCodesApiBaseURL = apiBaseURL + "/internal-response-codes";
        final String paymentProcessorApiBaseURL = apiBaseURL + "/payment-processors";
        final String paymentProcessorRulesApiBaseURL = apiBaseURL + "/payment-processor-rules";
        final String internalStatusCodesApiBaseURL = apiBaseURL + "/internal-status-codes";

        // @formatter:off
        httpSecurity
            .csrf()
                .disable()
            .exceptionHandling()
                .accessDeniedHandler(this.accessDeniedHandler)
                    .authenticationEntryPoint(this.unauthorizedHandler)
                    .and()
                        .sessionManagement()
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                        .authorizeRequests()
                            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .antMatchers(HttpMethod.GET, "/").permitAll()
                            .antMatchers(HttpMethod.GET, "**/*.html", "**/*.css", "**/*.js", "**/*.ico", "/assets/**").permitAll()

                            // Swagger
                            .antMatchers("/swagger-ui.html", "/webjars/springfox-swagger-ui/**", "/swagger-resources","/v2/api-docs", 
                                    "/configuration/**", "/images/**").permitAll()

                            // Transactions
                            .antMatchers(HttpMethod.GET, transactionsApiBaseURL, transactionsApiBaseURL + "/", transactionsApiBaseURL + "/{transactionId}", transactionsApiBaseURL + "/{transactionId}/").hasAnyAuthority("SEARCH_REPORTING")

                            // Session
                            .antMatchers(HttpMethod.POST, sessionApiBaseURL, sessionApiBaseURL + "/", sessionApiBaseURL + "/recovery/password", sessionApiBaseURL + "/recovery/password/").permitAll()
                            .antMatchers(HttpMethod.POST, sessionApiBaseURL + "/consumer/{username}", sessionApiBaseURL + "/consumer/{username}/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, sessionApiBaseURL, sessionApiBaseURL + "/").authenticated()
                            .antMatchers(HttpMethod.DELETE, sessionApiBaseURL, sessionApiBaseURL + "/").authenticated()

                            // Users
                            .antMatchers(HttpMethod.GET, usersApiBaseURL, usersApiBaseURL + "/").hasAnyAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.GET, usersApiBaseURL + "/{username}", usersApiBaseURL + "/{username}/").hasAnyAuthority("MANAGE_CURRENT_USER", "ADMINISTRATIVE")
                            .antMatchers(HttpMethod.POST, usersApiBaseURL, usersApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, usersApiBaseURL + "/{username}", usersApiBaseURL + "/{username}/", usersApiBaseURL + "/{username}/password", usersApiBaseURL + "/{username}/password/").hasAnyAuthority("MANAGE_CURRENT_USER", "ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, usersApiBaseURL + "/{username}/legal-entities", usersApiBaseURL + "/{username}/legal-entities/",  usersApiBaseURL + "/{username}/roles", usersApiBaseURL + "/{username}/roles/").hasAnyAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.DELETE, usersApiBaseURL + "/{username}", usersApiBaseURL + "/{username}/").hasAuthority("ADMINISTRATIVE")

                            // Legal entities
                            .antMatchers(HttpMethod.GET, legalEntitiesApiBaseURL, legalEntitiesApiBaseURL + "/", legalEntitiesApiBaseURL + "/{id}", legalEntitiesApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.POST, legalEntitiesApiBaseURL, legalEntitiesApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, legalEntitiesApiBaseURL + "/{id}", legalEntitiesApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.DELETE, legalEntitiesApiBaseURL + "/{id}", legalEntitiesApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")

                            // Payment Processors
                            .antMatchers(HttpMethod.GET, paymentProcessorApiBaseURL, paymentProcessorApiBaseURL + "/", paymentProcessorApiBaseURL + "/{id}", paymentProcessorApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.POST, paymentProcessorApiBaseURL, paymentProcessorApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, paymentProcessorApiBaseURL + "/{id}", paymentProcessorApiBaseURL + "/{id}/", paymentProcessorApiBaseURL + "/{id}/payment-processor-merchants", paymentProcessorApiBaseURL + "/{id}/payment-processor-merchants/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.DELETE, paymentProcessorApiBaseURL + "/{id}", paymentProcessorApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")

                            // Payment Processor Rules
                            .antMatchers(HttpMethod.GET, paymentProcessorRulesApiBaseURL, paymentProcessorRulesApiBaseURL + "/", paymentProcessorRulesApiBaseURL + "/{id}", paymentProcessorRulesApiBaseURL + "/{id}/", paymentProcessorRulesApiBaseURL + "/transaction-types", paymentProcessorRulesApiBaseURL + "/transaction-types/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.POST, paymentProcessorRulesApiBaseURL, paymentProcessorRulesApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, paymentProcessorRulesApiBaseURL + "/{id}", paymentProcessorRulesApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.DELETE, paymentProcessorRulesApiBaseURL + "/{id}", paymentProcessorRulesApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")

                            // Roles
                            .antMatchers(HttpMethod.GET, rolesApiBaseURL, rolesApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            
                            // Internal Response Codes
                            .antMatchers(HttpMethod.GET, internalResponseCodesApiBaseURL, internalResponseCodesApiBaseURL + "/").hasAuthority("MANAGE_RESPONSE_CODES")
                            .antMatchers(HttpMethod.PUT, internalResponseCodesApiBaseURL, internalResponseCodesApiBaseURL + "/").hasAuthority("MANAGE_RESPONSE_CODES")
                            .antMatchers(HttpMethod.DELETE, internalResponseCodesApiBaseURL + "/{id}", internalResponseCodesApiBaseURL + "/{id}/").hasAuthority("MANAGE_RESPONSE_CODES")

                         // Internal Response Codes
                            .antMatchers(HttpMethod.GET, internalStatusCodesApiBaseURL, internalStatusCodesApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.PUT, internalStatusCodesApiBaseURL, internalStatusCodesApiBaseURL + "/").hasAuthority("ADMINISTRATIVE")
                            .antMatchers(HttpMethod.DELETE, internalStatusCodesApiBaseURL + "/{id}", internalStatusCodesApiBaseURL + "/{id}/").hasAuthority("ADMINISTRATIVE")

                            .anyRequest().authenticated();
        // @formatter:on

        // Custom JWT based authentication
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }

}
