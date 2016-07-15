package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
    public Token findByUserIdAndType(long userId, String type);

    public Token findByUserIdAndToken(long userId, String token);

    public Token findByToken(String token);
}