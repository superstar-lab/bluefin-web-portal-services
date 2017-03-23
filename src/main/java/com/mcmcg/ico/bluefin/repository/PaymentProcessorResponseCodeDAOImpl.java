/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorResponseCodeDAOImpl implements PaymentProcessorResponseCodeDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorResponseCodeDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorResponseCode, String transactionTypeName, PaymentProcessor paymentProcessor) {


		com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorStatusCodeList = null;

		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>) jdbcTemplate
				.query(Queries.findPaymentProcessorResponseCodeByCodeId,
						new Object[] { transactionTypeName, paymentProcessorResponseCode,
								paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>(
								new PaymentProcessorResponseCodeRowMapper()));
		paymentProcessorStatusCodeList = DataAccessUtils.singleResult(list);

		if (paymentProcessorStatusCodeList != null) {
			LOGGER.debug("Found payment processor statuscode for : "
					+ paymentProcessorStatusCodeList.getPaymentProcessorResponseCode());
		} else {
			LOGGER.debug("Found payment processor statuscode not found for : " + paymentProcessorResponseCode + "/"
					+ transactionTypeName + "/" + paymentProcessor.getPaymentProcessorId());
		}

		return paymentProcessorStatusCodeList;
	
	}

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> findByTransactionTypeNameAndPaymentProcessor(String transactionTypeName,
			com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor) {

		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>) jdbcTemplate
				.query(Queries.findPaymentProcessorResponseCodeByTypeId,
						new Object[] { transactionTypeName, paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode>(
								new PaymentProcessorResponseCodeRowMapper()));

		if (list != null) {
			LOGGER.debug("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor statuscode not found for : " + transactionTypeName + "/"
					+ paymentProcessor.getPaymentProcessorId());
		}

		return list;
	}

	@Override
	public void deletePaymentProcessorResponseCode(Long paymentProcessorId) {
			int rows = jdbcTemplate.update(Queries.deletePaymentProcessorResponseCodeByID, new Object[] { paymentProcessorId });
			LOGGER.debug("Deleted payment Processor Response Code by  PaymentProcessorId: " + paymentProcessorId + ", rows affected = " + rows);
	}
}

class PaymentProcessorResponseCodeRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> {
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode mapRow(ResultSet rs, int row) throws SQLException {
		// SELECT PaymentProcessorResponseCode, PaymentProcessorID,
		// PaymentProcessorResponseCode,TransactionType,
		// PaymentProcessorResponseCodeDescription,DateCreated,DatedModified,ModifiedBy
		// FROM PaymentProcessorResponseCode_Lookup WHERE TransactionType='SALE'
		// AND PaymentProcessorResponseCode=1 AND PaymentProcessorID=1
		com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode();
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeId(rs.getLong("PaymentProcessorResponseCodeID"));
		paymentProcessorResponseCode.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		paymentProcessorResponseCode.setTransactionTypeName(rs.getString("TransactionType"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
		paymentProcessorResponseCode.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorResponseCode.setModifiedDate(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		paymentProcessorResponseCode.setLastModifiedBy(rs.getString("ModifiedBy"));

		return paymentProcessorResponseCode;
	}
}