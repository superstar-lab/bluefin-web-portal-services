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
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.Transaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
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
	private PaymentProcessorRepository paymentProcessorRepository;
	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;
	@Autowired
	private PaymentProcessorRemittanceDAO paymentProcessorRemittanceDAO;

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
		Transaction result = null;

		try {
			result = paymentProcessorRemittanceDAO.findRemittanceSaleRefundTransactionsDetail(transactionId,
					transactionType, processorTransactionType);
		} catch (ParseException e) {
			throw new CustomNotFoundException("Unable to process find remittance, sale, or refund transactions!");
		}

		if (result == null) {
			throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
		}

		return result;
	}

	public Transaction getRemittanceSaleResult(String transactionId) {
		Transaction result = null;

		PaymentProcessorRemittance ppr = paymentProcessorRemittanceDAO.findByProcessorTransactionId(transactionId);
		if (ppr == null) {
			ppr = new PaymentProcessorRemittance();
		}
		SaleTransaction st = saleTransactionDAO.findByProcessorTransactionId(transactionId);
		if (st == null) {
			st = new SaleTransaction();
		}

		PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance(
				ppr.getPaymentProcessorRemittanceId(), ppr.getDateCreated(), ppr.getReconciliationStatusId(),
				ppr.getReconciliationDate(), ppr.getPaymentMethod(), ppr.getTransactionAmount(),
				ppr.getTransactionType(), ppr.getTransactionTime(), ppr.getAccountId(), ppr.getApplication(),
				ppr.getProcessorTransactionId(), ppr.getMerchantId(), ppr.getTransactionSource(), ppr.getFirstName(),
				ppr.getLastName(), ppr.getRemittanceCreationDate(), ppr.getPaymentProcessorId(), null, null);

		result = (Transaction) paymentProcessorRemittance;

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
	public Iterable<RemittanceSale> getRemittanceSaleRefundVoidTransactions(String search, PageRequest paging,
			boolean negate) {
		Page<RemittanceSale> result;
		try {
			result = paymentProcessorRemittanceDAO.findRemittanceSaleRefundTransactions(search, paging, negate);
		} catch (ParseException e) {
			throw new CustomNotFoundException(
					"Unable to process find remittance, sale, refund or void transactions, due an error with date formatting");
		}
		final int page = paging.getPageNumber();

		if (page > result.getTotalPages() && page != 0) {
			LOGGER.error("Unable to find the page requested");
			throw new CustomNotFoundException("Unable to find the page requested");
		}

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
		return paymentProcessorRepository.findByPaymentProcessorId(Long.parseLong(paymentProcessorId))
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
		return reconciliationStatusDAO.findByReconciliationStatus(reconciliationStatus).getReconciliationStatusId()
				.toString();
	}

	/**
	 * Get the value of parameter in search string.
	 * 
	 * @param search
	 *            string
	 * @param parameter
	 *            in search string
	 * 
	 * @return value of parameter
	 */
	public String getValueFromParameter(String search, String parameter) {

		String value = null;
		String[] array1 = search.split("\\$\\$");

		for (String pair : array1) {
			if (pair.startsWith(parameter)) {
				String[] array2 = pair.split(":");
				value = array2[1];
				break;
			}
		}

		return value;
	}
}
