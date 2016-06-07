package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
