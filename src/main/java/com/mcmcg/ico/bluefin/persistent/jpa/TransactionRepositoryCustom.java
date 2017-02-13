package com.mcmcg.ico.bluefin.persistent.jpa;

import java.text.ParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;

public interface TransactionRepositoryCustom {
	public Page<SaleTransaction> findTransaction(String search, PageRequest page) throws ParseException;

	public List<SaleTransaction> findTransactionsReport(String search) throws ParseException;

	public Page<PaymentProcessorRemittance> findRemittanceSaleRefundTransactions(String search, PageRequest page,
			boolean negate) throws ParseException;

	public List<PaymentProcessorRemittance> findRemittanceSaleRefundTransactionsReport(String search)
			throws ParseException;

	public PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) throws ParseException;
}
