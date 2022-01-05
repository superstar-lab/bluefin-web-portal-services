package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private PermissionDAO permissionDAO;

    /**
     * Get ALL-Permission from table
     *
     * @return list of Permission
     */
    public List<Permission> findAllPermission(){
        return permissionDAO.findAllPermission();
    }
}
