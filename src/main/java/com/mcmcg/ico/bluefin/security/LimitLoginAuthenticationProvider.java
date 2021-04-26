package com.mcmcg.ico.bluefin.security;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.security.service.PasswordUtilsService;
import com.mcmcg.ico.bluefin.service.UserLoginHistoryRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component("authenticationProvider")
public class LimitLoginAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    UserDAO userDAO;

    @Autowired
    UserLoginHistoryRepoService userLoginHistoryRepoService;

    @Autowired
    @Qualifier("userDetailsService")
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Autowired
    PasswordUtilsService passwordUtilsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            Authentication auth = super.authenticate(authentication);
            return auth;
        } catch (Exception e) {
            User user = userDAO.findByUsername(authentication.getPrincipal().toString());
            UserLoginHistory userLoginHistory = new UserLoginHistory();
            userLoginHistory.setUsername(authentication.getPrincipal().toString());
            userLoginHistory.setPassword(this.getPasswordEncoder().encode(authentication.getCredentials().toString()));
            userLoginHistoryRepoService.saveUserLoginHistoryFailAuthentication(user, userLoginHistory, authentication.getPrincipal().toString());
            passwordUtilsService.updateUserLookUp(user, userLoginHistory);
            throw e;
        }
    }

}
