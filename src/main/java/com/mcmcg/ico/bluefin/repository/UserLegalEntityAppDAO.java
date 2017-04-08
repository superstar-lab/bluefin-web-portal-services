package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;

public interface UserLegalEntityAppDAO {
	List<UserLegalEntityApp> findByUserId(long userId);

	void deleteUserLegalEntityAppById(Collection<Long> legalEntityAppsToRemove);

	List<Long> fetchLegalEntityApps(Long id);
}
