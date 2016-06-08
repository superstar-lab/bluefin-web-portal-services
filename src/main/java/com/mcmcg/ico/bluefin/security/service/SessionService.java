package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.persistent.RolePermission;
import com.mcmcg.ico.bluefin.persistent.Token;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.TokenRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.security.TokenHandler;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;

@Service
public class SessionService {

    @Value("${bluefin.wp.services.token.expiration}")
    private Integer securityTokenExpiration;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    private TokenHandler tokenHandler;

    private static final String AUTHENTICATION_TOKEN_TYPE = "authentication";
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    public UsernamePasswordAuthenticationToken authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BadCredentialsException("Username doesn't exists: " + username);
        }
        return new UsernamePasswordAuthenticationToken(username, password);
    }

    public AuthenticationResponse generateToken(String username) {
        SecurityUser securityUser = userDetailsServiceImpl.loadUserByUsername(username);
        String generatedToken = getCurrentTokenIfValid(securityUser.getId());
        if (generatedToken == null) {
            Token token = generateNewToken(securityUser);
            tokenRepository.save(token);
            generatedToken = token.getToken();
        }
        AuthenticationResponse response;
        try {
            LOGGER.info("Creating login response for user: {}", username);
            response = getLoginResponse(username);
        } catch (Exception e) {
            LOGGER.error("Unable to get user information", e);
            throw new CustomNotFoundException("Unable to get user information");
        }
        response.setToken(generatedToken);
        return response;
    }

    public String getCurrentTokenIfValid(String stringToken) {
        Token token = tokenRepository.findByToken(stringToken);
        return tokenHandler.validateToken(token);
    }

    public String getCurrentTokenIfValid(long userId) {
        Token token = tokenRepository.findByUserIdAndType(userId, AUTHENTICATION_TOKEN_TYPE);
        return tokenHandler.validateToken(token);
    }

    public Token generateNewToken(SecurityUser securityUser) {
        Date expire = DateUtils.addSeconds(new Date(), securityTokenExpiration);
        securityUser.setExpires(expire);
        String result = tokenHandler.createTokenForUser(securityUser);
        Token token = new Token();
        token.setExpire(expire);
        token.setType(AUTHENTICATION_TOKEN_TYPE);
        token.setUserId(securityUser.getId());
        token.setToken(result);
        return token;
    }

    public AuthenticationResponse getLoginResponse(String username) throws Exception {
        AuthenticationResponse response = new AuthenticationResponse();

        User user = userRepository.findByUsername(username);
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(username);

        List<Permission> permissionsResult = new ArrayList<Permission>();
        for (UserRole role : user.getUserRoles()) {
            for (RolePermission permission : role.getRole().getRolePermissions()) {
                permissionsResult.add(permission.getPermission());
            }
        }
        response.setPermissions(permissionsResult);
        return response;
    }

    public AuthenticationResponse refreshToken(String token) {
        LOGGER.info("Parsing token to get user information");
        SecurityUser securityUser = tokenHandler.parseUserFromToken(token);
        String username = securityUser.getUsername();

        Date expire = DateUtils.addSeconds(new Date(), securityTokenExpiration);
        securityUser.setExpires(expire);
        LOGGER.info("Trying to refresh token for user: {}", username);
        String newToken = tokenHandler.createTokenForUser(securityUser);

        Token currentToken = tokenRepository.findByToken(token);
        currentToken.setToken(newToken);
        currentToken.setExpire(expire);
        tokenRepository.save(currentToken);

        AuthenticationResponse response;
        try {
            LOGGER.info("Creating response for user: {}", username);
            response = getLoginResponse(username);
        } catch (Exception e) {
            LOGGER.error("Unable to get user information", e);
            throw new CustomNotFoundException("Unable to get user information");
        }
        response.setToken(newToken);
        return response;
    }
}
