package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;

public interface LegalEntityAppRepository extends JpaRepository<LegalEntityApp, Long> {
    public LegalEntityApp findByLegalEntityAppName(String legalEntityAppName);

    public LegalEntityApp findByLegalEntityAppId(long legalEntityAppId);
}
