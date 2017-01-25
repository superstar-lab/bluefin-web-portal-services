package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.ActivationResource;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdatePasswordResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.security.service.SessionService;
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
    @Autowired
    private EmailService emailService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private PropertyService propertyService;

    private static final String REGISTER_USER_EMAIL_SUBJECT = "Bluefin web portal: Register user email";
    private static final String DEACTIVATE_ACCOUNT_EMAIL_SUBJECT = "Bluefin web portal: Deactivated account";

    /**
     * Get user information by username
     * 
     * @param username
     * @return UserResource object
     * @throws CustomBadRequestException
     *             user not found
     */
    public UserResource getUserInfomation(String username) {
        return new UserResource(getUser(username));
    }

    /**
     * Get user object by username
     * 
     * @param username
     * @return user object
     * @throws CustomNotFoundException
     *             when username is not found
     */
    public User getUser(final String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new CustomNotFoundException("Unable to find user by username provided: " + username);
        }

        return user;
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
    public List<LegalEntityApp> getLegalEntitiesByUser(final String username) {
        User user = userRepository.findByUsername(username);
        return (user == null || user.getLegalEntities().isEmpty()) ? new ArrayList<LegalEntityApp>()
                : user.getLegalEntityApps();
    }

    public UserResource registerNewUserAccount(RegisterUserResource userResource) {
        final String username = userResource.getUsername();
        if (existUsername(username)) {
            throw new CustomBadRequestException(
                    "Unable to create the account, this username already exists: " + username);
        }

        User newUser = userResource.toUser(roleService.getRolesByIds(userResource.getRoles()),
                legalEntityAppService.getLegalEntityAppsByIds(userResource.getLegalEntityApps()));
        newUser.setStatus("NEW");
        newUser.setUserPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        UserResource newUserResource = new UserResource(userRepository.save(newUser));

        // Send email
        final String link = "/api/users/" + username + "/password";
        final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
        String content = "Welcome to the Bluefin Portal.  Below is your username and a link to create a password. \n\n"
                + "Username: " + username + "\n\n To create your password, use the link below: \n\n"
                + propertyService.getPropertyValue("REGISTER_USER_EMAIL_LINK") + "?user=" + username + "&token="
                + token;
        emailService.sendEmail(newUser.getEmail(), REGISTER_USER_EMAIL_SUBJECT, content);

        return newUserResource;
    }

    public boolean existUsername(final String username) {
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
        User user = getUser(username);

        // Updating fields from existing user
        user.setFirstName(userResource.getFirstName());
        user.setLastName(userResource.getLastName());
        user.setEmail(userResource.getEmail());
        user.setDateUpdated(new DateTime());

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
        User userToUpdate = getUser(username);

        // User wants to clear roles from user
        if (rolesIds.isEmpty()) {
            throw new CustomBadRequestException("User MUST have at least one role assign to him.");
        }

        // Validate and load existing roles
        Map<Long, Role> newMapOfRoles = roleService.getRolesByIds(rolesIds).stream()
                .collect(Collectors.toMap(Role::getRoleId, r -> r));

        // Temporal list of roles that we need to keep in the user role list
        Set<Long> rolesToKeep = new HashSet<Long>();

        // Update current role list from user
        Iterator<UserRole> iter = userToUpdate.getRoles().iterator();
        while (iter.hasNext()) {
            UserRole element = iter.next();

            Role role = newMapOfRoles.get(element.getRole().getRoleId());
            if (role == null) {
                iter.remove();
            } else {
                rolesToKeep.add(element.getRole().getRoleId());
            }
        }

        // Add new roles to the user but ignoring the existing ones
        for (Long roleId : newMapOfRoles.keySet()) {
            if (!rolesToKeep.contains(roleId)) {
                userToUpdate.addRole(newMapOfRoles.get(roleId));
            }
        }

        userToUpdate.setDateUpdated(new DateTime());
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
        User userToUpdate = getUser(username);

        // User wants to clear legal entity apps from user
        if (legalEntityAppsIds.isEmpty()) {
            throw new CustomBadRequestException("User MUST have at least one legal entity assign to him.");
        }

        // Validate and load existing legal entity apps
        Map<Long, LegalEntityApp> newMapOfLegalEntityApps = legalEntityAppService
                .getLegalEntityAppsByIds(legalEntityAppsIds).stream()
                .collect(Collectors.toMap(LegalEntityApp::getLegalEntityAppId, l -> l));

        // Temporal list of legal entity apps that we need to keep in the user
        // legal entity app list
        Set<Long> legalEntityAppsToKeep = new HashSet<Long>();

        // Update current role list from user
        Iterator<UserLegalEntity> iter = userToUpdate.getLegalEntities().iterator();
        while (iter.hasNext()) {
            UserLegalEntity element = iter.next();

            LegalEntityApp legalEntityApp = newMapOfLegalEntityApps
                    .get(element.getLegalEntityApp().getLegalEntityAppId());
            if (legalEntityApp == null) {
                iter.remove();
            } else {
                legalEntityAppsToKeep.add(element.getLegalEntityApp().getLegalEntityAppId());
            }
        }

        // Add new roles to the user but ignoring the existing ones
        for (Long legalEntityAppId : newMapOfLegalEntityApps.keySet()) {
            if (!legalEntityAppsToKeep.contains(legalEntityAppId)) {
                userToUpdate.addLegalEntityApp(newMapOfLegalEntityApps.get(legalEntityAppId));
            }
        }
        userToUpdate.setDateUpdated(new DateTime());
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
    public boolean hasUserPrivilegesOverLegalEntities(Authentication authentication, Set<Long> legalEntitiesToVerify) {
        if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            return true;
        }
        // Get Legal Entities from user name
        Set<Long> userLegalEntities = getLegalEntitiesByUser(authentication.getName()).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

        return legalEntitiesToVerify.stream()
                .filter(verifyLegalEntityId -> !userLegalEntities.contains(verifyLegalEntityId))
                .collect(Collectors.toSet()).isEmpty();
    }

    /**
     * Verify if user has authority to manage all user.
     * 
     * @param authentication
     * 
     * @return status
     */
    public boolean hasPermissionToManageAllUsers(Authentication authentication) {
        Boolean hasPermission = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String userAuthority = authority.getAuthority();
            if (userAuthority.equals("ADMINISTRATIVE") || userAuthority.equals("MANAGE_ALL_USERS")) {
                hasPermission = true;
            }
            if (hasPermission) {
                break;
            }
        }
        return hasPermission;
    }

    /**
     * This method will return true if both users have a common legal entity,
     * false in other case
     * 
     * @param username
     * @param usernameToUpdate
     * @return true if the request user has related legal entities with the user
     *         he wants to CRUD
     * @throws CustomNotFoundException
     *             username not found
     */
    public boolean belongsToSameLegalEntity(Authentication authentication, final String usernameToUpdate) {
        final String username = authentication.getName();
        if (usernameToUpdate.equals(username)) {
            return true;
        }

        // Verify if user that needs to be updated exist
        User userToUpdate = getUser(usernameToUpdate);

        if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            return true;
        }
        // Get Legal Entities from consultant user
        Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
                .map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());
        // Get Legal Entities from user that will be updated
        Set<Long> legalEntitiesToVerify = userToUpdate.getLegalEntityApps().stream()
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
    public User updateUserPassword(String username, final UpdatePasswordResource updatePasswordResource,
            final String token) {

        username = (username.equals("me") ? tokenUtils.getUsernameFromToken(token) : username);
        String tokenType = tokenUtils.getTypeFromToken(token);
        if (username == null || tokenType == null) {
            throw new CustomBadRequestException("An authorization token is required to request this resource");
        }

        User userToUpdate = getUser(username);

        if ((tokenType.equals(TokenType.AUTHENTICATION.name()) || tokenType.equals(TokenType.APPLICATION.name()))
                && !isValidOldPassword(updatePasswordResource.getOldPassword(), userToUpdate.getUserPassword())) {
            throw new CustomBadRequestException("The old password is incorrect.");
        }
        if (tokenType.equals(TokenType.REGISTER_USER.name())) {
            userToUpdate.setStatus("ACTIVE");
        }
        if (tokenType.equals(TokenType.FORGOT_PASSWORD.name()) && userToUpdate.getStatus() == "NEW") {
            userToUpdate.setStatus("ACTIVE");
        }
        userToUpdate.setUserPassword(passwordEncoder.encode(updatePasswordResource.getNewPassword()));
        userToUpdate.setDateUpdated(new DateTime());
        return userRepository.save(userToUpdate);
    }

    public void userActivation(ActivationResource activationResource) {
        Boolean activate = activationResource.isActivate();
        List<String> notFoundUsernames = new ArrayList<String>();

        for (String username : activationResource.getUsernames()) {

            if (username == null) {
                throw new CustomBadRequestException("An authorization token is required to request this resource");
            }

            String status = (activate ? "NEW" : "INACTIVE");
            User user = userRepository.findByUsername(username);
            if (user == null) {
                notFoundUsernames.add(username);
            } else {
                activateAccount(user, status);
            }
        }
        if (!notFoundUsernames.isEmpty()) {
            throw new CustomNotFoundException("Unable to find users by usernames provided: " + notFoundUsernames);
        }
    }

    private void activateAccount(User userToUpdate, String status) {
        String username = userToUpdate.getUsername();
        if (userToUpdate.getStatus() != status) {
            userToUpdate.setStatus(status);
            if (status.equals("NEW")) {
                userToUpdate.setUserPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                // Send email
                final String link = "/api/users/" + username + "/password";
                final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
                String content = "Welcome to the Bluefin Portal.  Below is your username and a link to create a password. \n\n"
                        + "Username: " + username + "\n\n To create your password, use the link below: \n\n"
                        + propertyService.getPropertyValue("REGISTER_USER_EMAIL_LINK") + "?user=" + username + "&token="
                        + token;
                emailService.sendEmail(userToUpdate.getEmail(), REGISTER_USER_EMAIL_SUBJECT, content);
            } else {
                String content = "Your account has been deactivated. \n\n"
                        + "Please feel free to contact your system administratior. \n\n";
                emailService.sendEmail(userToUpdate.getEmail(), DEACTIVATE_ACCOUNT_EMAIL_SUBJECT, content);
            }
            userToUpdate = userRepository.save(userToUpdate);
        }
    }

    private boolean isValidOldPassword(final String oldPassword, final String currentUserPassword) {
        if (StringUtils.isEmpty(oldPassword)) {
            throw new CustomBadRequestException("oldPassword must not be empty");
        }

        return passwordEncoder.matches(oldPassword, currentUserPassword);
    }

}
