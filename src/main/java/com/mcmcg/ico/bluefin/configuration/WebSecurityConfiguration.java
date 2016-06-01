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

        httpSecurity.csrf().disable().exceptionHandling().accessDeniedHandler(this.accessDeniedHandler)
                .authenticationEntryPoint(this.unauthorizedHandler).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll().antMatchers(HttpMethod.GET, "/").permitAll()
                .antMatchers("/swagger-ui.html", "/webjars/springfox-swagger-ui/**", "/swagger-resources",
                        "/v2/api-docs")
                .permitAll()

                .antMatchers(HttpMethod.GET, "/api/rest/bluefin/transactions/**").permitAll()

                .antMatchers(HttpMethod.POST, "/api/rest/bluefin/session").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/rest/bluefin/session").authenticated()

                .antMatchers(HttpMethod.GET, "/api/rest/bluefin/users/{username}",
                        "/api/rest/bluefin/users/{username}/")
                .authenticated()

                .antMatchers(HttpMethod.POST, "/api/rest/bluefin/users").hasAuthority("void")
                .antMatchers(HttpMethod.PUT, "/api/rest/bluefin/users").hasAuthority("register")
                .antMatchers(HttpMethod.PUT, "/api/rest/bluefin/users/**").hasAuthority("void")
                .antMatchers(HttpMethod.DELETE, "/api/rest/bluefin/users/**").hasAuthority("void")

                .antMatchers(HttpMethod.GET, "/api/rest/bluefin/roles").hasAuthority("void")
                .antMatchers(HttpMethod.POST, "/api/rest/bluefin/roles").hasAuthority("void")
                .antMatchers(HttpMethod.PUT, "/api/rest/bluefin/roles/**").hasAuthority("void")
                .antMatchers(HttpMethod.DELETE, "/api/rest/bluefin/roles/**").hasAuthority("void").anyRequest()
                .authenticated();

        // Custom JWT based authentication
        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

    }

}
