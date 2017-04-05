package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.SecurityTokenBlacklist;

public interface SecurityTokenBlacklistDAO {
	SecurityTokenBlacklist findByTokenId(long tokenId);

	SecurityTokenBlacklist findByToken(String token);

	SecurityTokenBlacklist findByUserIdAndToken(long userId, String token);

	SecurityTokenBlacklist findByUserIdAndType(long userId, String type);

	long saveSecurityTokenBlacklist(SecurityTokenBlacklist securityTokenBlacklist);
}
