package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.bindb.service.TransationBinDBDetailsService;
import com.mcmcg.ico.bluefin.model.RefundTransaction;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class RefundTransactionDAOImpl implements RefundTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefundTransactionDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransationBinDBDetailsService transationBinDBDetailsService;
	
	@Override
	public RefundTransaction findByApplicationTransactionId(String transactionId) {
		ArrayList<RefundTransaction> list = (ArrayList<RefundTransaction>) jdbcTemplate.query(
				Queries.FINDREFUNDTRANSACTIONBYAPPLICATIONTRANSACTIONID, new Object[] { transactionId },
				new RowMapperResultSetExtractor<RefundTransaction>(new RefundTransactionRowMapper()));
		LOGGER.debug("RefundTransaction size ={} ",list.size());
		RefundTransaction refundTransaction = DataAccessUtils.singleResult(list);

		if (refundTransaction != null) {
			LOGGER.debug("Found RefundTransaction for transactionId ={} ", transactionId);
			String saleTransactionId = refundTransaction.getSaleTransactionId();
			if (saleTransactionId != null) {
				saleTransactionId = saleTransactionId.trim();
				if (saleTransactionId.length() > 0) {
					refundTransaction = findByApplicationTransactionIdInSale(saleTransactionId, refundTransaction);
				}
			} else {
				LOGGER.debug("SaleTransactionId found invalid");
			}
		} else {
			LOGGER.debug("RefundTransaction not found for transactionId ={} ", transactionId);
		}

		return refundTransaction;
	}
	
	public RefundTransaction findByApplicationTransactionIdInSale(String saleTransactionId, RefundTransaction refundTransaction) {
		ArrayList<SaleTransaction> saleTransactions = (ArrayList<SaleTransaction>) jdbcTemplate.query(
				Queries.FINDSALETRANSACTIONBYSALETRANSACTIONID, new Object[] { saleTransactionId },
				new RowMapperResultSetExtractor<SaleTransaction>(new SaleTransactionRowMapper()));
		LOGGER.debug("Number of Sale transactions: {}", saleTransactions.size());
		SaleTransaction saleTransaction = DataAccessUtils.singleResult(saleTransactions);

		if (saleTransaction != null) {
			LOGGER.debug("Record found for sale transactionId: {}", saleTransactionId);
			saleTransaction.setBinDBDetails(transationBinDBDetailsService.fetchBinDBDetail(saleTransaction.getCardNumberFirst6Char()));
			refundTransaction.setBinDBDetails(saleTransaction.getBinDBDetails());
			refundTransaction.setSaleTransaction(saleTransaction);
		} else {
			LOGGER.debug("Record not found for transactionId: {} ", saleTransactionId);
		}
		
		return refundTransaction;
	}

}

class RefundTransactionRowMapper implements RowMapper<RefundTransaction> {

	@Override
	public RefundTransaction mapRow(ResultSet rs, int row) throws SQLException {
		RefundTransaction refundTransaction = new RefundTransaction();
		refundTransaction.setRefundTransactionId(rs.getLong("RefundTransactionID"));
		refundTransaction.setSaleTransactionId(rs.getString("SaleTransactionID"));
		refundTransaction.setApprovalCode(rs.getString("ApprovalCode"));
		refundTransaction.setProcessor(rs.getString("Processor"));
		refundTransaction.setRefundAmount(rs.getBigDecimal("RefundAmount"));
		refundTransaction.setMerchantId(rs.getString("merchantID"));
		refundTransaction.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
		refundTransaction.setTransactionDateTime(new DateTime(rs.getTimestamp("TransactionDateTime")));
		refundTransaction.setApplicationTransactionId(rs.getString("ApplicationTransactionID"));
		refundTransaction.setApplication(rs.getString("Application"));
		refundTransaction.setProcessUser(rs.getString("pUser"));
		refundTransaction.setOriginalSaleTransactionId(rs.getString("OriginalSaleTransactionID"));
		refundTransaction.setPaymentProcessorStatusCode(rs.getString("PaymentProcessorStatusCode"));
		refundTransaction
				.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusCodeDescription"));
		refundTransaction.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
		refundTransaction
				.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		refundTransaction.setInternalStatusCode(rs.getString("InternalStatusCode"));
		refundTransaction.setInternalStatusDescription(rs.getString("InternalStatusDescription"));
		refundTransaction.setInternalResponseCode(rs.getString("InternalResponseCode"));
		refundTransaction.setInternalResponseDescription(rs.getString("InternalResponseDescription"));
		refundTransaction.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
		refundTransaction
				.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
		Timestamp ts;
		if (rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			refundTransaction.setDateCreated(new DateTime(ts));
		}
		
		refundTransaction.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
		if (rs.getString("ReconciliationDate") != null) {

			ts = Timestamp.valueOf(rs.getString("ReconciliationDate"));
			refundTransaction.setReconciliationDate(new DateTime(ts));
		}
		refundTransaction.setEtlRunId(rs.getLong("ETL_RUNID"));
		refundTransaction.setTransactionType(rs.getString("TransactionType"));

		return refundTransaction;
	}
}