package com.mcmcg.ico.bluefin.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isNotEmpty(authToken)) {

            String username = tokenUtils.getUsernameFromToken(authToken);
            String url = httpRequest.getRequestURL().toString().replace("/me/", "/" + username + "/");

            if (username != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                boolean isTokenValid = this.tokenUtils.validateToken(authToken, userDetails);
                if (isTokenValid) {
                    String tokenType = this.tokenUtils.getTypeFromToken(authToken);
                    String tokenUrl = this.tokenUtils.getUrlFromToken(authToken);
                    if ((userDetails.isEnabled() && checkAllowedTokenForThisFilter(tokenType))
                            || checkAccountLockAndTokenURL(userDetails, url, tokenUrl)) {

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            }
        }

        chain.doFilter(request, response);
    }

    /**
     * || tokenType.equals(TokenType.TRANSACTION.name() for iframe transaction token
     */
    private boolean checkAllowedTokenForThisFilter(String tokenType) {

        return tokenType.equals(TokenType.AUTHENTICATION.name())
                || tokenType.equals(TokenType.APPLICATION.name());
    }

    private boolean checkAccountLockAndTokenURL(UserDetails userDetails, String url, String tokenUrl) {
        return userDetails.isAccountNonLocked() && url != null && org.apache.commons.lang3.StringUtils.containsIgnoreCase(url, tokenUrl);
    }
}
