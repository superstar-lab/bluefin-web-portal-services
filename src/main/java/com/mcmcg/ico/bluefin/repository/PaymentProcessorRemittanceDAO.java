package com.mcmcg.ico.bluefin.repository;

import java.text.ParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;

public interface PaymentProcessorRemittanceDAO {
	PaymentProcessorRemittance findByProcessorTransactionId(String transactionId);
	PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) throws ParseException;
}
