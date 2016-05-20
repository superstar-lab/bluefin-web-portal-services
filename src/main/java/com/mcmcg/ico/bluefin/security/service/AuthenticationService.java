package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsernamePasswordAuthenticationToken authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BadCredentialsException("Username doesn't exists: " + username);
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled: " + username);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Incorrect password for user: " + username);
        }
        return new UsernamePasswordAuthenticationToken(username, password);
    }

    public AuthenticationResponse getLoginResponse(String username) throws Exception {
        AuthenticationResponse response = new AuthenticationResponse();

        User user = userRepository.findByUsername(username);
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(username);

        List<String> permissionsResult = new ArrayList<String>();
        for (Role role : user.getRoles()) {
            for (Permission permission : role.getPermissions()) {
                permissionsResult.add(permission.getPermissionName());
            }
        }
        response.setPermissions(permissionsResult);
        return response;
    }
}
