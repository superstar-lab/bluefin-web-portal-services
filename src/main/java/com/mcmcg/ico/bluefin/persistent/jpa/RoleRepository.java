package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    public Role findByRoleName(String roleName);
    public Role deleteByRoleName(String roleName);
}
