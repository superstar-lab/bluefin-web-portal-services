package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;

public interface LegalEntityAppRepository extends JpaRepository<LegalEntityApp, Long>, QueryDslPredicateExecutor<LegalEntityApp> {
    public LegalEntityApp findByLegalEntityAppName(String legalEntityAppName);

    public LegalEntityApp findByLegalEntityAppId(Long legalEntityAppId);
}
