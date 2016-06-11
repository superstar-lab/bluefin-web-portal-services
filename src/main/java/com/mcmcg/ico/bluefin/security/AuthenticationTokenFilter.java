package com.mcmcg.ico.bluefin.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.service.SessionService;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

    @Value("${bluefin.wp.services.token.header}")
    private String securityTokenHeader;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(this.securityTokenHeader);

        if (authToken != null) {
            // TODO:remove these lines when integrate with Okta
            authToken = sessionService.getCurrentTokenIfValid(1);

            SecurityUser securityUser = tokenHandler.parseUserFromToken(authToken);
            if (securityUser != null && SecurityContextHolder.getContext().getAuthentication() == null
                    && sessionService.getCurrentTokenIfValid(authToken) != null) {
                String username = securityUser.getUsername();
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}
