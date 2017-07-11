package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.repository.TransactionTypeDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;

@Service
@Transactional
public class TransactionTypeService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionTypeService.class);

	@Autowired
	private TransactionTypeDAO transactionTypeDAO;

	public List<TransactionType> getTransactionTypes() {
		LOGGER.info("get TransactionTypes");
		return transactionTypeDAO.findAll();
	}

	public TransactionType getTransactionTypeById(Long transactionTypeId) {
		TransactionType transactionType = transactionTypeDAO.findByTransactionId(transactionTypeId);
		LOGGER.info("transactionType :={} ",transactionType);
		if (transactionType == null) {
			throw new CustomBadRequestException("Invalid transaction type.");
		}
		return transactionType;
	}

	public TransactionType getTransactionTypeByType(String transactionTypeName) {
		TransactionType transactionType = transactionTypeDAO.findByTransactionType(transactionTypeName);
		LOGGER.info("transactionType= {} ",transactionType);
		if (transactionType == null) {
			throw new CustomBadRequestException("Invalid transaction type.");
		}
		return transactionType;
	}
}
