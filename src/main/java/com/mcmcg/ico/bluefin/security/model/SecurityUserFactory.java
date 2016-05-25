package com.mcmcg.ico.bluefin.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;

public class SecurityUserFactory {

    public static SecurityUser create(User user) {
        return new SecurityUser(user.getUserId(), user.getUsername(), user.getPassword(), user.getEmail(),
                getRoles(user.getRoles()));
    }

    public static Collection<? extends GrantedAuthority> getRoles(Collection<Role> roles) {
        List<SimpleGrantedAuthority> result = new ArrayList<SimpleGrantedAuthority>();
        if (roles == null) {
            return null;
        } else {
            for (Role role : roles) {
                result.add(new SimpleGrantedAuthority(role.getRoleName()));
            }
        }
        return result;
    }

}
