package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.ReconciliationStatus;

public interface ReconciliationStatusRepository extends JpaRepository<ReconciliationStatus, Long>, QueryDslPredicateExecutor<ReconciliationStatus> {
	public ReconciliationStatus findByReconciliationStatus(String reconciliationStatus);
}
