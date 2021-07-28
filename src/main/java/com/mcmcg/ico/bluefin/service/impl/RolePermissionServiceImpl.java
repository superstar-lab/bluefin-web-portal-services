package com.mcmcg.ico.bluefin.service.impl;

import com.mcmcg.ico.bluefin.repository.RolePermissionAssignmentDAO;
import com.mcmcg.ico.bluefin.service.RolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class RolePermissionServiceImpl implements RolePermissionService {
    private RolePermissionAssignmentDAO rolePermissionAssignmentDAO;

    public RolePermissionServiceImpl(RolePermissionAssignmentDAO rolePermissionAssignmentDAO){
        this.rolePermissionAssignmentDAO = rolePermissionAssignmentDAO;
    }

    @Override
    public void rolesandPermissionsAssignment(long role, String permissions, String username) {
        rolePermissionAssignmentDAO.rolesandPermissionsAssignment(role,permissions,username);
    }
}