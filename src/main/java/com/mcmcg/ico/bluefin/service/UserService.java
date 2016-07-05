package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

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

    public Iterable<User> getUsers(BooleanExpression exp, Integer page, Integer size, String sort) {
        Page<User> result = userRepository.findAll(exp, QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }
        return result;
    }

    /**
     * Gets the legal entities by user name
     * 
     * @param userName
     * @return list of legal entities owned by the user with the user name given
     *         by parameter
     */
    public List<LegalEntityApp> getLegalEntitiesByUser(String username) {
        User user = userRepository.findByUsername(username);
        return user == null ? new ArrayList<LegalEntityApp>() : user.getLegalEntityApps();
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
        Collection<String> tokenLegalEntities = getLegalEntityApps(tokenUser.getLegalEntities());
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException("User information not found");
        }
        Collection<String> userLegalEntities = getLegalEntityApps(user.getLegalEntities());
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
        if (existUsername(username)) {
            throw new CustomBadRequestException(
                    "Unable to create the account, this username already exists: " + username);
        }

        List<UserLegalEntity> userLegalEntities = getLegalEntitiesByIds(userResource.getLegalEntityApps());
        List<UserRole> roles = getRolesByIds(userResource.getRoles());
        User newUser = userResource.toUser(roles, userLegalEntities);
        userRepository.save(newUser);
        newUser.getLegalEntities().forEach(userLegalEntity -> {
            userLegalEntity.setUser(newUser);
            userLegalEntity.setCreatedDate(new Date());
            userLegalEntityRepository.save(userLegalEntity);
        });
        newUser.getRoles().forEach(userRole -> {
            userRole.setUser(newUser);
            userRole.setCreatedDate(new Date());
            userRoleRepository.save(userRole);
        });
        return newUser.toUserResource();
    }

    private boolean existUsername(String username) {
        return userRepository.findByUsername(username) == null ? false : true;
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
        List<UserRole> updatedRoles = getRolesByIds(roles);
        userRoleRepository.deleteInBatch(user.getRoles());
        updatedRoles.forEach(userRole -> {
            userRole.setUser(user);
            userRole.setCreatedDate(new Date());
            userRoleRepository.save(userRole);
        });
        user.setRoles(updatedRoles);
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

    /**
     * Update the legalEntities of an already stored user
     * 
     * @param username
     * @param legalEntities
     * @return userResource with all the user information
     * @throws Exception
     */
    public UserResource updateUserLegalEntities(String username, List<Integer> legalEntities) throws Exception {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException(
                    "Unable to update legalEntities, this username doesn't exists: " + username);
        }
        List<UserLegalEntity> updatedLegalEntities = getLegalEntitiesByIds(legalEntities);
        userLegalEntityRepository.deleteInBatch(user.getLegalEntities());
        updatedLegalEntities.forEach(userLegalEntity -> {
            userLegalEntity.setUser(user);
            userLegalEntity.setCreatedDate(new Date());
            userLegalEntityRepository.save(userLegalEntity);
        });
        user.setLegalEntities(updatedLegalEntities);
        return user.toUserResource();
    }

    /**
     * Get all the legalEntity objects by the specified ids
     * 
     * @param legalEntitiesList
     *            as list of integers
     * @return legalEntitiesList as list of objects
     */
    private List<UserLegalEntity> getLegalEntitiesByIds(List<Integer> legalEntityList) throws Exception {
        List<UserLegalEntity> result = new ArrayList<UserLegalEntity>();
        for (Integer currentLegalEntity : legalEntityList) {
            LegalEntityApp legalEntity = legalEntityAppRepository
                    .findByLegalEntityAppId(currentLegalEntity.longValue());
            if (legalEntity != null) {
                UserLegalEntity userLegalEntity = new UserLegalEntity();
                userLegalEntity.setLegalEntityApp(legalEntity);
                result.add(userLegalEntity);
            } else {
                throw new CustomBadRequestException("The following legalEntity doesn't exist: " + currentLegalEntity);
            }
        }
        return result;
    }

}
