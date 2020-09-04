package com.mcmcg.ico.bluefin.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;

public class RemittanceSaleRefundRowMapper implements RowMapper<PaymentProcessorRemittance> {

	@Override
	public PaymentProcessorRemittance mapRow(ResultSet rs, int rowNum) throws SQLException {
		return prepareRecordRemittanceSaleRefund(rs);
	}

	private PaymentProcessorRemittance prepareRecordRemittanceSaleRefund(ResultSet rs) throws SQLException {
		PaymentProcessorRemittance record = new PaymentProcessorRemittance();
		record.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL));// reconciliationStatusId
		record.setTransactionAmount(rs.getBigDecimal(BluefinWebPortalConstants.TRANSACTIONAMOUNT));// transactionAmount
		record.setTransactionType(rs.getString(BluefinWebPortalConstants.TRANSACTIONTYPE));// transactionType
		record.setTransactionTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.TRANSACTIONTIME)));// transactionDate
		record.setAccountId(rs.getString(BluefinWebPortalConstants.ACCOUNTIDVAL));// accountId
		record.setApplication(rs.getString(BluefinWebPortalConstants.APPLICATION));// application
		record.setProcessorTransactionId(rs.getString(BluefinWebPortalConstants.PROCESSORTRANSACTIONID));// processorTransactionId
		record.setMerchantId(rs.getString(BluefinWebPortalConstants.MERCHANTID));// merchantId
		record.setPaymentProcessorId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORIDVAL));// PaymentProcessorID
		record.setProcessorName(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL));// processorName
		record.setSaleTransactionId(rs.getLong(BluefinWebPortalConstants.SALETRANSACTIONID));// SaleTransactionID
		record.setSaleTransactionType(rs.getString(BluefinWebPortalConstants.SALETRANSACTIONTYPE));// transactionType
		record.setSaleCardNumberLast4Char(rs.getString(BluefinWebPortalConstants.SALECARDNUMBERLAST4CHAR));// cardNumberLast4Char
		record.setSaleCardType(rs.getString(BluefinWebPortalConstants.SALECARDTYPE));// cardType
		record.setSaleAccountNumber(rs.getString(BluefinWebPortalConstants.SALEACCOUNTID));// accountNumber
		record.setSaleAmount(rs.getBigDecimal(BluefinWebPortalConstants.SALECHARGEAMOUNT)); // amount
		record.setSaleApplicationTransactionId(rs.getString(BluefinWebPortalConstants.SALEAPPLICATIONTRANSACTIONID));// applicationTransactionId
		record.setSaleMerchantId(rs.getString(BluefinWebPortalConstants.SALEMERCHANTID));// merchantId
		record.setSaleProcessorName(rs.getString(BluefinWebPortalConstants.SALEPROCESSOR));// processorName
		record.setSaleApplication(rs.getString(BluefinWebPortalConstants.SALEAPPLICATION));// application
		record.setSaleProcessorTransactionId(rs.getString(BluefinWebPortalConstants.SALEPROCESSORTRANSACTIONID));// processorTransactionId
		record.setSaleTransactionDateTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.SALETRANSACTIONDATETIME)));// transactionDateTime
		record.setSaleReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.SALERECONCILIATIONSTATUSID));// reconciliationStatusId
		record.setMerchantId(rs.getString("MID"));// MID
		record.setReconReconciliationStatusId(rs.getString(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL1));
		record.setRecondProcessorName(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL1));
		return record;
	}
}