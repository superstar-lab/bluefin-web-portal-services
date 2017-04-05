package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.ReconciliationStatus;

public interface ReconciliationStatusDAO {
	List<ReconciliationStatus> findAll();

	ReconciliationStatus findByReconciliationStatusId(long userId);

	ReconciliationStatus findByReconciliationStatus(String reconciliationStatus);
}
