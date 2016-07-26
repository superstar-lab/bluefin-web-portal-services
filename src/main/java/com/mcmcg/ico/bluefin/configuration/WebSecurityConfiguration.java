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
                            .antMatchers(HttpMethod.GET, transactionsApiBaseURL, transactionsApiBaseURL + "/").authenticated()
                            .antMatchers(HttpMethod.GET, transactionsApiBaseURL + "/{transactionId}", transactionsApiBaseURL + "/{transactionId}/").authenticated()

                            // Session
                            .antMatchers(HttpMethod.POST, sessionApiBaseURL, sessionApiBaseURL + "/").permitAll()
                            .antMatchers(HttpMethod.PUT, sessionApiBaseURL, sessionApiBaseURL + "/").authenticated()
                            .antMatchers(HttpMethod.POST, sessionApiBaseURL + "/recovery/password", sessionApiBaseURL + "/recovery/password/").permitAll()
                            .antMatchers(HttpMethod.DELETE, sessionApiBaseURL, sessionApiBaseURL + "/").authenticated()

                            // Users
                            .antMatchers(HttpMethod.GET, usersApiBaseURL + "/{username}", usersApiBaseURL + "/{username}/").authenticated()
                            .antMatchers(HttpMethod.POST, usersApiBaseURL, usersApiBaseURL + "/").authenticated()
                            .antMatchers(HttpMethod.PUT, usersApiBaseURL + "/{username}", usersApiBaseURL + "/{username}/").authenticated()
                            .antMatchers(HttpMethod.PUT, usersApiBaseURL + "/{username}/password", usersApiBaseURL + "/{username}/password/").authenticated()
                            .antMatchers(HttpMethod.DELETE, usersApiBaseURL + "/{username}", usersApiBaseURL + "/{username}/").authenticated()

                            // Legal entities
                            .antMatchers(HttpMethod.GET, legalEntitiesApiBaseURL, legalEntitiesApiBaseURL + "/").authenticated()
//                            .antMatchers(HttpMethod.POST, legalEntitiesApiBaseURL).permitAll()
//                            .antMatchers(HttpMethod.PUT, legalEntitiesApiBaseURL + "/{legalEntityId}", legalEntitiesApiBaseURL + "/{legalEntityId}/").permitAll()
//                            .antMatchers(HttpMethod.DELETE, legalEntitiesApiBaseURL + "/{legalEntityId}", legalEntitiesApiBaseURL + "/{legalEntityId}/").permitAll()

                            // Roles
                            .antMatchers(HttpMethod.GET, rolesApiBaseURL, rolesApiBaseURL + "/").authenticated()
//                            .antMatchers(HttpMethod.POST, rolesApiBaseURL).permitAll()
//                            .antMatchers(HttpMethod.PUT, rolesApiBaseURL + "/{roleId}", rolesApiBaseURL + "/{roleId}/").permitAll()
//                            .antMatchers(HttpMethod.DELETE, rolesApiBaseURL + "/{roleId}", rolesApiBaseURL + "/{roleId}/").permitAll()

                            .anyRequest().authenticated();
        // @formatter:on

        // Custom JWT based authentication
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }

}
