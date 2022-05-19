package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface CustomSaleTransactionDAO  {
	public Page<SaleTransaction> findTransaction(String search, Map<String, List<String>> multipleValuesMap, PageRequest page, String timeZone) throws ParseException;

	public List<SaleTransaction> findTransactionsReport(String search,Map<String, List<String>> multipleValuesMap) throws ParseException;

	public Page<PaymentProcessorRemittance> findRemittanceSaleRefundTransactions(String search, PageRequest page,
			boolean negate) throws ParseException ;

	public List<RemittanceSale> findRemittanceSaleRefundTransactionsReport(String search,boolean negate)
			throws ParseException;

	public PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) throws ParseException;
}
