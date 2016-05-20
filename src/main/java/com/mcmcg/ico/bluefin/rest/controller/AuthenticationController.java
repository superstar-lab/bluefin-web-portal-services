package com.mcmcg.ico.bluefin.rest.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.service.AuthenticationService;
import com.mcmcg.ico.bluefin.security.service.UserDetailsServiceImpl;

@RestController
@RequestMapping("login")
public class AuthenticationController {

    @Value("${token.header}")
    private String securitytokenHeader;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> authenticationRequest(@RequestBody AuthenticationRequest authenticationRequest)
            throws AuthenticationException {
        LOGGER.info("Authenticating user: {}", authenticationRequest.getUsername());
        Authentication authentication = this.authenticationService.authenticate(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-authentication to generate token
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        LOGGER.info("Generating token for user: {}", authenticationRequest.getUsername());
        String token = this.tokenUtils.generateToken(userDetails);
        
        AuthenticationResponse response;
        try {
            LOGGER.info("Creating login response for user: {}", authenticationRequest.getUsername());
            response = authenticationService.getLoginResponse(authenticationRequest.getUsername());
        } catch (Exception e) {
            LOGGER.error("Unable to get user information", e);
            throw new CustomNotFoundException("Unable to get user information");
        }
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    public ResponseEntity<?> authenticationRequest(HttpServletRequest request) {
        String token = request.getHeader(securitytokenHeader);
        String username = this.tokenUtils.getUsernameFromToken(token);
        
        LOGGER.info("Trying to refresh token for user: {}", username);
        SecurityUser user = (SecurityUser) this.userDetailsServiceImpl.loadUserByUsername(username);
        if (this.tokenUtils.canTokenBeRefreshed(token, user.getLastPasswordReset())) {
            LOGGER.info("Refreshing token for user: {}", username);
            String refreshedToken = this.tokenUtils.refreshToken(token);
            AuthenticationResponse response;
            try {
                LOGGER.info("Creating login response for user: {}", username);
                response = authenticationService.getLoginResponse(username);
            } catch (Exception e) {
                LOGGER.error("Unable to get user information", e);
                throw new CustomNotFoundException("Unable to get user information");
            }
            response.setToken(refreshedToken);
            return ResponseEntity.ok(response);
        } else {
            LOGGER.error("Unable to refresh token for user: {}", username);
            return ResponseEntity.badRequest().body(null);
        }
    }

}
