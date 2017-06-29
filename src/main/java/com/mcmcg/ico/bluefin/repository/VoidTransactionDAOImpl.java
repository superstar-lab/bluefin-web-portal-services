package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.VoidTransaction;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class VoidTransactionDAOImpl implements VoidTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoidTransactionDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public VoidTransaction findByApplicationTransactionId(String transactionId) {
		ArrayList<VoidTransaction> list = (ArrayList<VoidTransaction>) jdbcTemplate.query(
				Queries.findVoidTransactionByApplicationTransactionId, new Object[] { transactionId },
				new RowMapperResultSetExtractor<VoidTransaction>(new VoidTransactionRowMapper()));
		LOGGER.debug("VoidTransactionDAOImpl :: findByApplicationTransactionId() : VoidTransaction size : "+list.size());
		VoidTransaction voidTransaction = DataAccessUtils.singleResult(list);

		if (voidTransaction != null) {
			LOGGER.debug("VoidTransactionDAOImpl :: findByApplicationTransactionId() : Found VoidTransaction for transactionId: " + transactionId);
		} else {
			LOGGER.debug("VoidTransactionDAOImpl :: findByApplicationTransactionId() : VoidTransaction not found for transactionId: " + transactionId);
		}

		return voidTransaction;
	}

}

class VoidTransactionRowMapper implements RowMapper<VoidTransaction> {

	@Override
	public VoidTransaction mapRow(ResultSet rs, int row) throws SQLException {
		VoidTransaction voidTransaction = new VoidTransaction();
		voidTransaction.setVoidTransactionId(rs.getLong("VoidTransactionID"));
		voidTransaction.setSaleTransactionId(rs.getString("SaleTransactionID"));
		voidTransaction.setApprovalCode(rs.getString("ApprovalCode"));
		voidTransaction.setProcessor(rs.getString("Processor"));
		voidTransaction.setMerchantId(rs.getString("merchantID"));
		voidTransaction.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
		voidTransaction.setTransactionDateTime(new DateTime(rs.getTimestamp("TransactionDateTime")));
		voidTransaction.setApplicationTransactionId(rs.getString("ApplicationTransactionID"));
		voidTransaction.setApplication(rs.getString("Application"));
		voidTransaction.setProcessUser(rs.getString("pUser"));
		voidTransaction.setOriginalSaleTransactionId(rs.getString("OriginalSaleTransactionID"));
		voidTransaction.setPaymentProcessorStatusCode(rs.getString("PaymentProcessorStatusCode"));
		voidTransaction.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusCodeDescription"));
		voidTransaction.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
		voidTransaction
				.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		voidTransaction.setInternalStatusCode(rs.getString("InternalStatusCode"));
		voidTransaction.setInternalStatusDescription(rs.getString("InternalStatusDescription"));
		voidTransaction.setInternalResponseCode(rs.getString("InternalResponseCode"));
		voidTransaction.setInternalResponseDescription(rs.getString("InternalResponseDescription"));
		voidTransaction.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
		voidTransaction.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
		Timestamp ts = Timestamp.valueOf(rs.getString("DateCreated"));
		voidTransaction.setDateCreated(new DateTime(ts));
		return voidTransaction;
	}
}