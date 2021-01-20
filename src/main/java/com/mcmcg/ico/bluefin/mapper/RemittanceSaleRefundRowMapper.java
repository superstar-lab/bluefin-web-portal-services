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
		record.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL));
		record.setTransactionAmount(rs.getBigDecimal(BluefinWebPortalConstants.TRANSACTIONAMOUNT));
		record.setTransactionType(rs.getString(BluefinWebPortalConstants.TRANSACTIONTYPE));
		record.setTransactionTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.TRANSACTIONTIME)));
		record.setAccountId(rs.getString(BluefinWebPortalConstants.ACCOUNTIDVAL));
		record.setApplication(rs.getString(BluefinWebPortalConstants.APPLICATION));
		record.setProcessorTransactionId(rs.getString(BluefinWebPortalConstants.PROCESSORTRANSACTIONID));
		record.setMerchantId(rs.getString(BluefinWebPortalConstants.MERCHANTID));
		record.setPaymentProcessorId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORIDVAL));
		record.setProcessorName(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL));
		record.setSaleTransactionId(rs.getLong(BluefinWebPortalConstants.SALETRANSACTIONID));
		record.setSaleTransactionType(rs.getString(BluefinWebPortalConstants.SALETRANSACTIONTYPE));
		record.setSaleCardNumberLast4Char(rs.getString(BluefinWebPortalConstants.SALECARDNUMBERLAST4CHAR));
		record.setSaleCardType(rs.getString(BluefinWebPortalConstants.SALECARDTYPE));
		record.setSaleAccountNumber(rs.getString(BluefinWebPortalConstants.SALEACCOUNTID));
		record.setSaleAmount(rs.getBigDecimal(BluefinWebPortalConstants.SALECHARGEAMOUNT));
		record.setSaleApplicationTransactionId(rs.getString(BluefinWebPortalConstants.SALEAPPLICATIONTRANSACTIONID));
		record.setSaleMerchantId(rs.getString(BluefinWebPortalConstants.SALEMERCHANTID));
		record.setSaleProcessorName(rs.getString(BluefinWebPortalConstants.SALEPROCESSOR));
		record.setSaleApplication(rs.getString(BluefinWebPortalConstants.SALEAPPLICATION));
		record.setSaleProcessorTransactionId(rs.getString(BluefinWebPortalConstants.SALEPROCESSORTRANSACTIONID));
		record.setSaleTransactionDateTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.SALETRANSACTIONDATETIME)));
		record.setSaleReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.SALERECONCILIATIONSTATUSID));
		record.setMerchantId(rs.getString("MID"));
		record.setReconReconciliationStatusId(rs.getString(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL1));
		record.setRecondProcessorName(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL1));
		return record;
	}
}