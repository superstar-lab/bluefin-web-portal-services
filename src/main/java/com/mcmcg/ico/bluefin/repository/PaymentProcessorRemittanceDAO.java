package com.mcmcg.ico.bluefin.repository;

import java.text.ParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;

public interface PaymentProcessorRemittanceDAO {
	PaymentProcessorRemittance findByProcessorTransactionId(String transactionId);

	Page<RemittanceSale> findRemittanceSaleRefundTransactions(String search, PageRequest pageRequest, boolean negate)
			throws ParseException;

	List<RemittanceSale> findRemittanceSaleRefundTransactionsReport(String search) throws ParseException;

	PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) throws ParseException;
}
