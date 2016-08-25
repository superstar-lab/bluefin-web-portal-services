package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long>{
    public List<RolePermission> findByRole(Role role);
}
