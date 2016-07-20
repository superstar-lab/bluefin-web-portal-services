package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdatePasswordResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private LegalEntityAppService legalEntityAppService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private TokenUtils tokenUtils;

    public UserResource getUserInfomation(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException("User information not found");
        }

        return new UserResource(user);
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

    public UserResource registerNewUserAccount(RegisterUserResource userResource) {
        final String username = userResource.getUsername();
        if (existUsername(username)) {
            throw new CustomBadRequestException(
                    "Unable to create the account, this username already exists: " + username);
        }

        User newUser = userResource.toUser(roleService.getRolesByIds(userResource.getRoles()),
                legalEntityAppService.getLegalEntityAppsByIds(userResource.getLegalEntityApps()));

        newUser.setUserPassword(passwordEncoder.encode(userResource.getPassword()));

        return new UserResource(userRepository.save(newUser));
    }

    public boolean existUsername(String username) {
        return userRepository.findByUsername(username) == null ? false : true;
    }

    /**
     * Update the profile information of an already stored user
     * 
     * @param username
     * @param updateUserResource
     * @return userResource with all the user information
     * @throws CustomNotFoundException
     */
    public UserResource updateUserProfile(String username, UpdateUserResource userResource) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException(
                    "Unable to update the account, this username doesn't exists: " + username);
        }

        // Updating fields from existing user
        user.setFirstName(userResource.getFirstName());
        user.setLastName(userResource.getLastName());
        user.setEmail(userResource.getEmail());
        user.setDateUpdated(new Date());

        return new UserResource(userRepository.save(user));
    }

    /**
     * Update the roles of an already stored user
     * 
     * @param username
     * @param roles
     * @return userResource with all the user information
     * @throws CustomNotFoundException
     */
    public User updateUserRoles(final String username, final Set<Long> rolesIds) {
        User userToUpdate = userRepository.findByUsername(username);
        if (userToUpdate == null) {
            throw new CustomNotFoundException("Unable to update roles, this username doesn't exists: " + username);
        }

        // Clean old user roles
        userToUpdate.getRoles().clear();
        userToUpdate = userRepository.save(userToUpdate);

        // User wants to clear roles from user
        if (rolesIds.isEmpty()) {
            return userToUpdate;
        }

        // Update user roles
        List<Role> roles = roleService.getRolesByIds(rolesIds);
        for (Role role : roles) {
            userToUpdate.addRole(role);
        }

        return userRepository.save(userToUpdate);
    }

    /**
     * Update the legalEntities of an already stored user
     * 
     * @param username
     * @param legalEntities
     * @return user with all the user information
     * @throws CustomNotFoundException
     */
    public User updateUserLegalEntities(final String username, final Set<Long> legalEntityAppsIds) {
        User userToUpdate = userRepository.findByUsername(username);
        if (userToUpdate == null) {
            throw new CustomNotFoundException(
                    "Unable to update legalEntities, this username doesn't exists: " + username);
        }

        // Clean old legal entities
        userToUpdate.getLegalEntityApps().clear();
        userToUpdate = userRepository.save(userToUpdate);

        // User wants to clear legal entities from user
        if (legalEntityAppsIds.isEmpty()) {
            return userToUpdate;
        }

        // Update legal entities
        List<LegalEntityApp> legalEntityApps = legalEntityAppService.getLegalEntityAppsByIds(legalEntityAppsIds);
        for (LegalEntityApp leApp : legalEntityApps) {
            userToUpdate.addLegalEntityApp(leApp);
        }

        return userRepository.save(userToUpdate);
    }

    /**
     * Validates if the current legal entities of the user that tries to get the
     * information are valid by checking the values of the request with the ones
     * owned by the user
     * 
     * @param legalEntityIds
     * @param userName
     */
    public boolean hasUserPrivilegesOverLegalEntities(String username, Set<Long> legalEntitiesToVerify) {
        // Get Legal Entities from user name
        Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

        return legalEntitiesToVerify.stream()
                .filter(verifyLegalEntityId -> !userLegalEntities.contains(verifyLegalEntityId))
                .collect(Collectors.toSet()).isEmpty();
    }

    /**
     * This method will return true if both users have a common legal entity,
     * false in other case
     * 
     * @param username
     * @param usernameToUpdate
     * @return true if the request user has related legal entities with the user
     *         he wants to CRUD
     */
    public boolean belongsToSameLegalEntity(Authentication authentication, String usernameToUpdate) {
        final String username = authentication.getName();
        if (usernameToUpdate.equals(username)) {
            return true;
        }
        // Get Legal Entities from consultant user
        Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());
        // Get Legal Entities from user that will be updated
        Set<Long> legalEntitiesToVerify = getLegalEntitiesByUser(usernameToUpdate).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

        return !legalEntitiesToVerify.stream().filter(userLegalEntities::contains).collect(Collectors.toSet())
                .isEmpty();
    }

    /**
     * Update the password of an already stored user
     * 
     * @param username
     * @param updatePasswordResource
     * @return user with all the user information
     * @throws CustomNotFoundException
     * @throws CustomBadRequestException
     */
    public User updateUserPassword(final String username, final UpdatePasswordResource updatePasswordResource,
            final String token) {
        User userToUpdate = userRepository.findByUsername(username);
        if (userToUpdate == null) {
            throw new CustomNotFoundException("Unable to update password, this username doesn't exists: " + username);
        }

        String tokenType = tokenUtils.getTypeFromToken(token);
        if (tokenType == null) {
            throw new CustomBadRequestException("An authorization token is required to request this resource");
        }
        if (tokenType.equals(TokenType.AUTHENTICATION.name())
                && !isValidOldPassword(updatePasswordResource.getOldPassword(), userToUpdate.getUserPassword())) {
            throw new CustomBadRequestException("The old password is incorrect.");
        }
        userToUpdate.setUserPassword(passwordEncoder.encode(updatePasswordResource.getNewPassword()));
        return userRepository.save(userToUpdate);
    }

    private boolean isValidOldPassword(String oldPassword, String currentUserPassword) {
        if (oldPassword.isEmpty()) {
            throw new CustomBadRequestException("oldPassword must not be empty");
        } else {
            return passwordEncoder.matches(oldPassword, currentUserPassword);
        }
    }

}
