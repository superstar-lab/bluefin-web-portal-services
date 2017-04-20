package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.SecurityTokenBlacklist;

public interface SecurityTokenBlacklistDAO {
	SecurityTokenBlacklist findByTokenId(long tokenId);

	SecurityTokenBlacklist findByUserIdAndToken(long userId, String token);

	long saveSecurityTokenBlacklist(SecurityTokenBlacklist securityTokenBlacklist);
}
