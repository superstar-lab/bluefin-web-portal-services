package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.User;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUserId(long userId);

    public User findByUsername(String username);

    public User findByEmail(String email);

    public User deleteByUsername(String username);

}
