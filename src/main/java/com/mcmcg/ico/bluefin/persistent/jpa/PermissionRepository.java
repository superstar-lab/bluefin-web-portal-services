package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    public Permission findByPermissionName(String permissionName);
}
