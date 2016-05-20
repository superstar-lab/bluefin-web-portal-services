package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByUserId(Integer id);
    public User findByUsername(String username);
    public User findByEmail(String email);
    public User deleteByUsername(String username);

}
