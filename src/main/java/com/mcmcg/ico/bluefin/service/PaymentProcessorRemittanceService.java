package com.mcmcg.ico.bluefin.service;

import java.text.ParseException;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.Transaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRemittanceDAO;
import com.mcmcg.ico.bluefin.repository.ReconciliationStatusDAO;
import com.mcmcg.ico.bluefin.repository.SaleTransactionDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class PaymentProcessorRemittanceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceService.class);

	@Autowired
	private SaleTransactionDAO saleTransactionDAO;
	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;
	@Autowired
	private PaymentProcessorRemittanceDAO paymentProcessorRemittanceDAO;
	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;

	@Autowired
	CustomSaleTransactionDAO customSaleTransactionDAO;
	
	/**
	 * Get transaction information for details page.
	 * 
	 * @param transactionId
	 * @param transactionType
	 * @param processorTransactionType
	 * 
	 * @return transaction details
	 */
	public Transaction getTransactionInformation(final String transactionId, TransactionTypeCode transactionType,
			final String processorTransactionType) {
		LOGGER.info("Entering to PaymentProcessorRemittanceService :: getTransactionInformation()");
		Transaction result = null;

		try {
			result = customSaleTransactionDAO.findRemittanceSaleRefundTransactionsDetail(transactionId,
					transactionType, processorTransactionType);
		} catch (ParseException e) {
			throw new CustomNotFoundException("Unable to process find remittance, sale, or refund transactions!");
		}

		if (result == null) {
			throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
		}

		LOGGER.info("Exit from PaymentProcessorRemittanceService :: getTransactionInformation()");
		return result;
	}

	public Transaction getRemittanceSaleResult(String transactionId) {
		LOGGER.info("Entering to PaymentProcessorRemittanceService :: getRemittanceSaleResult()");
		Transaction result;

		PaymentProcessorRemittance ppr = paymentProcessorRemittanceDAO.findByProcessorTransactionId(transactionId);
		if (ppr == null) {
			ppr = new PaymentProcessorRemittance();
		}
		saleTransactionDAO.findByProcessorTransactionId(transactionId);
		
		PaymentProcessorRemittance paymentProcessorRemittanceObj = new PaymentProcessorRemittance();
		paymentProcessorRemittanceObj.setPaymentProcessorRemittanceId(ppr.getPaymentProcessorRemittanceId());
		paymentProcessorRemittanceObj.setDateCreated(ppr.getDateCreated());
		paymentProcessorRemittanceObj.setReconciliationStatusId(ppr.getReconciliationStatusId());
		paymentProcessorRemittanceObj.setReconciliationDate(ppr.getReconciliationDate()); 
		paymentProcessorRemittanceObj.setPaymentMethod(ppr.getPaymentMethod());
		paymentProcessorRemittanceObj.setTransactionAmount(ppr.getTransactionAmount());
		paymentProcessorRemittanceObj.setTransactionType(ppr.getTransactionType()); 
		paymentProcessorRemittanceObj.setTransactionTime(ppr.getTransactionTime()); 
		paymentProcessorRemittanceObj.setAccountId(ppr.getAccountId()); 
		paymentProcessorRemittanceObj.setApplication(ppr.getApplication());
		paymentProcessorRemittanceObj.setProcessorTransactionId(ppr.getProcessorTransactionId()); 
		paymentProcessorRemittanceObj.setMerchantId(ppr.getMerchantId()); 
		paymentProcessorRemittanceObj.setTransactionSource(ppr.getTransactionSource()); 
		paymentProcessorRemittanceObj.setFirstName(ppr.getFirstName());
		paymentProcessorRemittanceObj.setLastName(ppr.getLastName()); 
		paymentProcessorRemittanceObj.setRemittanceCreationDate(ppr.getRemittanceCreationDate()); 
		paymentProcessorRemittanceObj.setPaymentProcessorId(ppr.getPaymentProcessorId()); 
		paymentProcessorRemittanceObj.setReProcessStatus(null); 
		paymentProcessorRemittanceObj.setEtlRunId(null); 
		paymentProcessorRemittanceObj.setSaleAccountNumber(ppr.getSaleAccountNumber()); 
		paymentProcessorRemittanceObj.setSaleAmount(ppr.getSaleAmount());

		result = paymentProcessorRemittanceObj;

		LOGGER.info("Exit from PaymentProcessorRemittanceService :: getRemittanceSaleResult()");
		return result;
	}

	/**
	 * Get remittance, sale, refund, and void transactions. This will be one
	 * column of the UI.
	 * 
	 * @param search
	 * @param paging
	 * @param negate
	 * 
	 * @return list of objects containing these transactions
	 */
	public Iterable<PaymentProcessorRemittance> getRemittanceSaleRefundVoidTransactions(String search, PageRequest paging,
			boolean negate) {
		LOGGER.info("Entering to PaymentProcessorRemittanceService :: getRemittanceSaleRefundVoidTransactions()");
		Page<PaymentProcessorRemittance> result;
		try {
			/**
			 * Commented below call, as one dao was calling another dao, removed extra layer of calling, all logic is placed in customSaleDaoimple
			 */
			result = customSaleTransactionDAO.findRemittanceSaleRefundTransactions(search, paging, negate);
		} catch (ParseException e) {
			throw new CustomNotFoundException(
					"Unable to process find remittance, sale, refund or void transactions, due an error with date formatting");
		}
		final int page = paging.getPageNumber();

		if (page > result.getTotalPages() && page != 0) {
			LOGGER.error("PaymentProcessorRemittanceService :: getRemittanceSaleRefundVoidTransactions() : Unable to find the page requested");
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		LOGGER.info("Exit from PaymentProcessorRemittanceService :: getRemittanceSaleRefundVoidTransactions()");
		return result;
	}

	/**
	 * Get processorName by id
	 * 
	 * @param id
	 * 
	 * @return processorName
	 */
	public String getProcessorNameById(String paymentProcessorId) {
		LOGGER.info("Entering to PaymentProcessorRemittanceService :: getProcessorNameById()");
		return paymentProcessorDAO.findByPaymentProcessorId(Long.parseLong(paymentProcessorId))
				.getProcessorName();
	}

	/**
	 * Get reconciliationStatusId by reconciliationStatus
	 * 
	 * @param reconciliationStatus
	 * 
	 * @return reconciliationStatusId
	 */
	public String getReconciliationStatusId(String reconciliationStatus) {
		LOGGER.info("Entering to PaymentProcessorRemittanceService :: getReconciliationStatusId()");
		return reconciliationStatusDAO.findByReconciliationStatus(reconciliationStatus).getReconciliationStatusId()
				.toString();
	}


}
