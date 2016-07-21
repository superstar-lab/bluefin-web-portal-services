package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.SecurityTokenBlacklist;

public interface SecurityTokenRepository extends JpaRepository<SecurityTokenBlacklist, Long> {
    public SecurityTokenBlacklist findByUserIdAndType(long userId, String type);

    public SecurityTokenBlacklist findByUserIdAndToken(long userId, String token);

    public SecurityTokenBlacklist findByToken(String token);
}