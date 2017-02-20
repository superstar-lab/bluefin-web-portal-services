package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.ReconciliationStatus;
import com.mcmcg.ico.bluefin.repository.ReconciliationStatusDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class ReconciliationStatusService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ReconciliationStatusService.class);

	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;

	/**
	 * This method will find a reconciliation status by its id, not found
	 * exception if it does not exist
	 * 
	 * @param id
	 * @return Reconciliation status object if found
	 */
	public ReconciliationStatus getReconciliationStatusById(long id) {
		ReconciliationStatus reconciliationStatus = reconciliationStatusDAO.findByReconciliationStatusId(id);

		if (reconciliationStatus == null) {
			throw new CustomNotFoundException(String.format("Unable to find reconciliation status with id = [%s]", id));
		}

		return reconciliationStatus;
	}

	public List<ReconciliationStatus> getReconciliationStatuses() {
		return reconciliationStatusDAO.findAll();
	}
}
