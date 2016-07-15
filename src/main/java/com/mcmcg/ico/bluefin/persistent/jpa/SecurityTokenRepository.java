package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.SecurityToken;

public interface SecurityTokenRepository extends JpaRepository<SecurityToken, Long> {
    public SecurityToken findByUserIdAndType(long userId, String type);

    public SecurityToken findByUserIdAndToken(long userId, String token);

    public SecurityToken findByToken(String token);
}