package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResource getUserInfomation(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException("User information not found");
        }

        return user.toUserResource();
    }

    public boolean havePermissionToGetOtherUsersInformation(Authentication tokenInformation, String username) {
        for (GrantedAuthority permission : tokenInformation.getAuthorities()) {
            if (permission.getAuthority().equals("readAllUsers")) {
                return true;
            } else if (permission.getAuthority().equals("readLegalEntityUsers")) {
                return belongsToSameLegalEntity(username, tokenInformation.getName());
            }
        }
        return false;
    }

    private boolean belongsToSameLegalEntity(String username, String tokenUsername) {
        User tokenUser = userRepository.findByUsername(tokenUsername);
        Collection<String> tokenLegalEntities = getLegalEntityApps(tokenUser.getUserLegalEntities());
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException("User information not found");
        }
        Collection<String> userLegalEntities = getLegalEntityApps(user.getUserLegalEntities());
        return !(intersection(tokenLegalEntities, userLegalEntities).isEmpty());
    }

    private List<String> intersection(Collection<String> tokenLegalEntities, Collection<String> userLegalEntities) {
        List<String> result = new ArrayList<String>();
        for (String legalEntity : tokenLegalEntities) {
            if (userLegalEntities.contains(legalEntity)) {
                result.add(legalEntity);
            }
        }
        return result;
    }

    private List<String> getLegalEntityApps(Collection<UserLegalEntity> userLegalEntityAppList) {
        List<String> legalEntityApps = new ArrayList<String>();
        for (UserLegalEntity userLegalEntityApp : userLegalEntityAppList) {
            legalEntityApps.add(userLegalEntityApp.getLegalEntityApp().getLegalEntityAppName());
        }
        return legalEntityApps;
    }

}
