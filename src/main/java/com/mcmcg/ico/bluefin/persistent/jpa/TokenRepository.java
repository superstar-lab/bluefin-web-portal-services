package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.Token;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    public Token findByUserIdAndType(Integer id, String type);

    public Token findByToken(String token);
}