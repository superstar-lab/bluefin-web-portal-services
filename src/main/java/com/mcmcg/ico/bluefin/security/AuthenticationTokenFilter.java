package com.mcmcg.ico.bluefin.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.service.PropertyService;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
        String username = tokenUtils.getUsernameFromToken(authToken);
        String url = httpRequest.getRequestURL().toString().replace("/me/", "/" + username + "/");

        if (username != null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (userDetails != null && this.tokenUtils.validateToken(authToken, userDetails)) {
                String tokenType = this.tokenUtils.getTypeFromToken(authToken);
                String tokenUrl = this.tokenUtils.getUrlFromToken(authToken);

                if (tokenType.equals(TokenType.AUTHENTICATION.name()) || tokenType.equals(TokenType.APPLICATION.name())
                        || url.contains(tokenUrl)) {

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        }
        chain.doFilter(request, response);
    }
}
