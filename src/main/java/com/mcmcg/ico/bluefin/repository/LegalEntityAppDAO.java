package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;

public interface LegalEntityAppDAO {
	LegalEntityApp findByLegalEntityAppName(String legalEntityAppName);

	LegalEntityApp findByLegalEntityAppId(Long legalEntityAppId);
	
	LegalEntityApp findActiveLegalEntityAppId(Long legalEntityAppId);

	List<LegalEntityApp> findAll();

	List<LegalEntityApp> findAll(List<Long> legalEntityAppIds);

	LegalEntityApp saveLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy);

	LegalEntityApp updateLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy);

	void deleteLegalEntityApp(LegalEntityApp legalEntityAppToDelete);

	void createLegalEntityApps(Collection<UserLegalEntityApp> legalEntities);

	List<LegalEntityApp> findAllActive();
}
