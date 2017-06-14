package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.RefundTransaction;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class RefundTransactionDAOImpl implements RefundTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefundTransactionDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public RefundTransaction findByApplicationTransactionId(String transactionId) {
		RefundTransaction refundTransaction = null;

		ArrayList<RefundTransaction> list = (ArrayList<RefundTransaction>) jdbcTemplate.query(
				Queries.findRefundTransactionByApplicationTransactionId, new Object[] { transactionId },
				new RowMapperResultSetExtractor<RefundTransaction>(new RefundTransactionRowMapper()));
		LOGGER.debug("RefundTransactionDAOImpl :: findByApplicationTransactionId : RefundTransaction size : "+list.size());
		refundTransaction = DataAccessUtils.singleResult(list);

		if (refundTransaction != null) {
			LOGGER.debug("RefundTransactionDAOImpl :: findByApplicationTransactionId : Found RefundTransaction for transactionId: " + transactionId);
		} else {
			LOGGER.debug("RefundTransactionDAOImpl :: findByApplicationTransactionId : RefundTransaction not found for transactionId: " + transactionId);
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
		Timestamp ts = null;
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

		return refundTransaction;
	}
}