package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;

public interface UserLegalEntityAppDAO {
	List<UserLegalEntityApp> findByUserId(long userId);
}
