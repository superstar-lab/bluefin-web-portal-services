package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     *         by parameter, empty list if user not found
     */
    public List<LegalEntityApp> getLegalEntitiesByUser(String userName) {
        User user = userRepository.findByUsername(userName);
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
    public UserResource updateUserRoles(String username, List<Long> roles) throws Exception {
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
    private List<UserRole> getRolesByIds(List<Long> rolesList) throws Exception {
        List<UserRole> result = new ArrayList<UserRole>();
        for (Long currentRole : rolesList) {
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
    public UserResource updateUserLegalEntities(String username, List<Long> legalEntities) throws Exception {
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
    private List<UserLegalEntity> getLegalEntitiesByIds(List<Long> legalEntityList) throws Exception {
        List<UserLegalEntity> result = new ArrayList<UserLegalEntity>();
        for (Long currentLegalEntity : legalEntityList) {
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

    /**
     * Validates if the current legal entities of the user that tries to get the
     * information are valid by checking the values of the request with the ones
     * owned by the user
     * 
     * @param legalEntityIds
     * @param userName
     */
    public Boolean hasUserPrivilegesOverLegalEntities(String username, Set<Long> legalEntitiesToVerify) {
        // Get Legal Entities from user name
        Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

        return legalEntitiesToVerify.stream()
                .filter(verifyLegalEntityId -> !userLegalEntities.contains(verifyLegalEntityId))
                .collect(Collectors.toSet()).isEmpty();
    }
    
    /**
     * This method will return true if both users have a common legal entity, false in other case
     * @param username
     * @param usernameToUpdate
     * @return true if the request user has related legal entities with the user he wants to CRUD
     */
    public boolean belongsToSameLegalEntity(String username, String usernameToUpdate) {
        // Get Legal Entities from consultant user
        Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());
        // Get Legal Entities from user that will be updated
        Set<Long> legalEntitiesToVerify = getLegalEntitiesByUser(usernameToUpdate).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

        return !legalEntitiesToVerify.stream().filter(userLegalEntities::contains).collect(Collectors.toSet())
                .isEmpty();
    }

}
