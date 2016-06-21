package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.User;

public interface UserRepository extends JpaRepository<User, Long>, QueryDslPredicateExecutor<User> {
    public User findByUserId(long userId);

    public User findByUsername(String username);

    public User findByEmail(String email);

    public User deleteByUsername(String username);

}
