package com.mcmcg.ico.bluefin.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getRoles() {
        return roleRepository.findAll();
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
            throw new CustomBadRequestException(
                    "The following roles don't exist.  List = " + rolesIds);
        }

        Set<Long> rolesNotFound = rolesIds.stream()
                .filter(x -> !result.stream().map(Role::getRoleId).collect(Collectors.toSet()).contains(x))
                .collect(Collectors.toSet());

        throw new CustomBadRequestException(
                "The following roles don't exist.  List = " + rolesNotFound);
    }

}
