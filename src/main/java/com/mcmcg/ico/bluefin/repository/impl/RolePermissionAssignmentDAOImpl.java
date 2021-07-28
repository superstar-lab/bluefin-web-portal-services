package com.mcmcg.ico.bluefin.repository.impl;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.repository.RolePermissionAssignmentDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RolePermissionAssignmentDAOImpl implements RolePermissionAssignmentDAO {

    private JdbcTemplate jdbcTemplate;

    public RolePermissionAssignmentDAOImpl(@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void rolesandPermissionsAssignment(long role, String permissions, String username) {
        log.info("RolePermissionAssignmentDAOImpl -> rolesandPermissionsAssignment, Calling DAO for role: {}, permissions : {} , username : {}", role, permissions, username);
        try {
            jdbcTemplate.update("CALL spRolesandPermissionsAssignment(?,?,?)", new Object[]{role, permissions, username});
        } catch (Exception ex) {
            log.error("RolePermissionAssignmentDAOImpl -> rolesandPermissionsAssignment, Error while calling store procedure. fromDate: {}, toDate : {} , statusCode : {} , Error: {}"
                    , role, permissions, username, ex.getMessage());
        }
    }
}
