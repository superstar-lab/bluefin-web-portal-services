package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;

public interface LegalEntityAppDAO {
	LegalEntityApp findByLegalEntityAppName(String legalEntityAppName);

	LegalEntityApp findByLegalEntityAppId(Long legalEntityAppId);

	List<LegalEntityApp> findAll();

	List<LegalEntityApp> findAll(List<Long> legalEntitiesFromUser);

	LegalEntityApp saveLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy);

	LegalEntityApp updateLegalEntityApp(LegalEntityApp legalEntityApp, String modifiedBy);

	void deleteLegalEntityApp(LegalEntityApp legalEntityAppToDelete);
}
