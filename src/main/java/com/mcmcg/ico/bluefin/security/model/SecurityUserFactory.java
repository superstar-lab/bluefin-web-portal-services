package com.mcmcg.ico.bluefin.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserRole;

public class SecurityUserFactory {

    public static SecurityUser create(User user) {
        return new SecurityUser(user, getRoles(user.getRoles()));
    }

    public static Collection<? extends GrantedAuthority> getRoles(Collection<UserRole> roles) {
        List<SimpleGrantedAuthority> result = new ArrayList<SimpleGrantedAuthority>();
        if (roles == null) {
            return null;
        } else {
            for (UserRole role : roles) {
                result.add(new SimpleGrantedAuthority(role.getRole().getRoleName()));
            }
        }
        return result;
    }

}
