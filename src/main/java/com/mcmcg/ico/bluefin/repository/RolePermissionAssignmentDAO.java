package com.mcmcg.ico.bluefin.repository;

public interface RolePermissionAssignmentDAO {
    void rolesandPermissionsAssignment(long role, String permissions, String username);
}
