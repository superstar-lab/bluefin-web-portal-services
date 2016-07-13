package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.persistent.RolePermission;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLoginHistory;
import com.mcmcg.ico.bluefin.persistent.UserLoginHistory.MessageCode;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.UserLoginHistoryRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomForbiddenException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;

@Service
public class SessionService {

    @Value("${bluefin.wp.services.token.expiration}")
    private Integer securityTokenExpiration;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private UserLoginHistoryRepository userLoginHistoryRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    public UsernamePasswordAuthenticationToken authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        createLoginHistory(user, password, username);

        if (user == null) {
            throw new CustomForbiddenException("Error authenticating user: " + username);
        }
        return new UsernamePasswordAuthenticationToken(username, password);
    }

    private void createLoginHistory(User user, String password, String userName) {
        // Creates a login history
        UserLoginHistory userLoginHistory = new UserLoginHistory();
        userLoginHistory.setLoginDateTime(new Date());
        userLoginHistory.setMessageId(MessageCode.ERROR_USER_NOT_FOUND.getValue());
        userLoginHistory.setUserName(userName);

        if (user != null) {
            // Creates success case for login history
            userLoginHistory.setUser(user.getUserId());
            userLoginHistory.setMessageId(MessageCode.SUCCESS.getValue());
        }
        // TODO add logic for when the user's password is not found
        userLoginHistoryRepository.save(userLoginHistory);
    }

    public AuthenticationResponse generateToken(String username) {
        String token = generateNewToken(username);

        AuthenticationResponse response;
        try {
            LOGGER.info("Creating login response for user: {}", username);
            response = getLoginResponse(username);
        } catch (Exception e) {
            LOGGER.error("Unable to get user information", e);
            throw new CustomNotFoundException("Unable to get user information");
        }
        response.setToken(token);
        return response;
    }

    public String generateNewToken(String username) {
        SecurityUser securityUser = userDetailsService.loadUserByUsername(username);
        if (securityUser != null) {
            return tokenUtils.generateToken(securityUser);
        } else {
            LOGGER.error("Error generating token for user ", username);
            throw new CustomBadRequestException("Error generating token for user " + username);
        }
    }

    public AuthenticationResponse getLoginResponse(String username) throws Exception {
        AuthenticationResponse response = new AuthenticationResponse();

        User user = userRepository.findByUsername(username);
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(username);

        List<Permission> permissionsResult = new ArrayList<Permission>();
        for (UserRole role : user.getRoles()) {
            for (RolePermission permission : role.getRole().getRolePermissions()) {
                permissionsResult.add(permission.getPermission());
            }
        }
        response.setPermissions(permissionsResult);
        return response;
    }

    public AuthenticationResponse refreshToken(String token) {
        LOGGER.info("Parsing token to get user information");
        String username = tokenUtils.getUsernameFromToken(token);

        LOGGER.info("Trying to refresh token for user: {}", username);
        String newToken = generateNewToken(username);

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
