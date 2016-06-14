package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserLegalEntityRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRoleRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserLegalEntityRepository userLegalEntityRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private LegalEntityAppRepository legalEntityAppRepository;

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

    public UserResource registerNewUserAccount(RegisterUserResource userResource) throws Exception {
        String username = userResource.getUsername();
        if (userNameExist(username)) {
            throw new CustomBadRequestException(
                    "Unable to create the account, this username already exists: " + username);
        }

        List<UserLegalEntity> userLegalEntities = getUserLegalEntityApps(userResource.getLegalEntityApps());
        List<UserRole> roles = getRoles(userResource.getRoles());
        User newUser = userResource.toUser(roles, userLegalEntities);
        userRepository.save(newUser);
        newUser.getUserLegalEntities().forEach(userLegalEntity -> {
            userLegalEntity.setUser(newUser);
            userLegalEntity.setCreatedDate(new Date());
            userLegalEntityRepository.save(userLegalEntity);
        });
        newUser.getUserRoles().forEach(userRole -> {
            userRole.setUser(newUser);
            userRole.setCreatedDate(new Date());
            userRoleRepository.save(userRole);
        });
        return newUser.toUserResource();
    }

    private boolean userNameExist(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return true;
        }
        return false;
    }

    private List<UserRole> getRoles(List<String> rolesList) {
        List<UserRole> result = new ArrayList<UserRole>();
        for (String currentRole : rolesList) {
            Role role = roleRepository.findByRoleName(currentRole);
            if (role != null) {
                UserRole userRole = new UserRole();
                userRole.setRole(role);
                result.add(userRole);
            } else {
                throw new CustomBadRequestException("The following role doesn't exist: " + currentRole);
            }
        }
        return result;
    }

    private List<UserLegalEntity> getUserLegalEntityApps(List<String> legalEntityApps) {
        List<UserLegalEntity> result = new ArrayList<UserLegalEntity>();
        for (String currentEntity : legalEntityApps) {
            LegalEntityApp legalEntity = legalEntityAppRepository.findByLegalEntityAppName(currentEntity);
            if (legalEntity != null) {
                UserLegalEntity userLegalEntity = new UserLegalEntity();
                userLegalEntity.setLegalEntityApp(legalEntity);
                result.add(userLegalEntity);
            } else {
                throw new CustomBadRequestException("The following legal entity doesn't exist: " + currentEntity);
            }
        }
        return result;
    }

    /**
     * Update the profile information of an already stored user
     * 
     * @param username
     * @param updateUserResource
     * @return userResource with all the user information
     * @throws Exception
     */
    public UserResource updateUserProfile(String username, UpdateUserResource userResource) throws Exception {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException(
                    "Unable to update the account, this username doesn't exists: " + username);
        }
        user = userResource.updateUser(user);
        User updatedUser = userRepository.save(user);
        return updatedUser.toUserResource();
    }

    /**
     * Update the roles of an already stored user
     * 
     * @param username
     * @param roles
     * @return userResource with all the user information
     * @throws Exception
     */
    public UserResource updateUserRoles(String username, List<Integer> roles) throws Exception {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException("Unable to update roles, this username doesn't exists: " + username);
        }
        List<UserRole> newRoles = getRolesByIds(roles);
        userRoleRepository.deleteInBatch(user.getUserRoles());
        newRoles.forEach(userRole -> {
            userRole.setUser(user);
            userRole.setCreatedDate(new Date());
            userRoleRepository.save(userRole);
        });
        user.setUserRoles(newRoles);
        return user.toUserResource();
    }

    /**
     * Get all the role objects by the specified ids
     * 
     * @param rolesList
     *            as list of integers
     * @return rolesList as list of objects
     */
    private List<UserRole> getRolesByIds(List<Integer> rolesList) throws Exception {
        List<UserRole> result = new ArrayList<UserRole>();
        for (Integer currentRole : rolesList) {
            Role role = roleRepository.findByRoleId(currentRole.longValue());
            if (role != null) {
                UserRole userRole = new UserRole();
                userRole.setRole(role);
                result.add(userRole);
            } else {
                throw new CustomBadRequestException("The following role doesn't exist: " + currentRole);
            }
        }
        return result;
    }

}
