package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.security.service.SessionService;

@Service
@Transactional
public class RoleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private UserRepository userRepository;

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    /**
     * Get roles depending on the user and privileges of the user
     * 
     * @param authentication
     * @return list of roles
     */
    public List<Role> getRoles(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName());

        if (user == null) {
            LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
                    authentication.getName());
            return new ArrayList<Role>(0);
        }

        if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            return roleRepository.findAll();
        }

        // Roles that belongs to a user
        List<Long> rolesFromUser = user.getRoles().stream().map(userRole -> userRole.getRole().getRoleId())
                .collect(Collectors.toList());
        return roleRepository.findAll(rolesFromUser);
    }

    /**
     * Get all role objects by the entered ids
     * 
     * @param rolesIds
     *            list of role ids that we need to find
     * @return list of roles
     * @throws CustomBadRequestException
     *             when at least one id does not exist
     */
    public List<Role> getRolesByIds(Set<Long> rolesIds) {
        List<Role> result = roleRepository.findAll(rolesIds);

        if (result.size() == rolesIds.size()) {
            return result;
        }

        // Create a detail error
        if (result == null || result.isEmpty()) {
            throw new CustomBadRequestException("The following roles don't exist.  List = " + rolesIds);
        }

        Set<Long> rolesNotFound = rolesIds.stream()
                .filter(x -> !result.stream().map(Role::getRoleId).collect(Collectors.toSet()).contains(x))
                .collect(Collectors.toSet());

        throw new CustomBadRequestException("The following roles don't exist.  List = " + rolesNotFound);
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

}
