/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorStatusCodeDAOImpl implements PaymentProcessorStatusCodeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorStatusCodeDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorStatusCode, String transactionTypeName, PaymentProcessor paymentProcessor) {

		com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCodeList = null;

		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>) jdbcTemplate
				.query(Queries.findPaymentProcessorStatusCodeByCodeId,
						new Object[] { transactionTypeName, paymentProcessorStatusCode,
								paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>(
								new PaymentProcessorStatusCodeRowMapper()));
		paymentProcessorStatusCodeList = DataAccessUtils.singleResult(list);

		if (paymentProcessorStatusCodeList != null) {
			LOGGER.debug("Found payment processor statuscode for : "
					+ paymentProcessorStatusCodeList.getPaymentProcessorStatusCode());
		} else {
			LOGGER.debug("Found payment processor statuscode not found for : " + paymentProcessorStatusCode + "/"
					+ transactionTypeName + "/" + paymentProcessor.getPaymentProcessorId());
		}

		return paymentProcessorStatusCodeList;
	}

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> findByTransactionTypeNameAndPaymentProcessor(
			String transactionTypeName, PaymentProcessor paymentProcessor) {

		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>) jdbcTemplate
				.query(Queries.findPaymentProcessorStatusCodeByTypeId,
						new Object[] { transactionTypeName, paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>(
								new PaymentProcessorStatusCodeRowMapper()));

		if (list != null) {
			LOGGER.debug("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor statuscode not found for : " + transactionTypeName + "/"
					+ paymentProcessor.getPaymentProcessorId());
		}

		return list;
	}

	@Override
	public PaymentProcessorStatusCode findOne(Long paymentProcessorStatusCode) {
		try {
			return jdbcTemplate.queryForObject(Queries.findPaymentProcessorStatusCodeById, new Object[] { paymentProcessorStatusCode },
					new PaymentProcessorStatusCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public void deletePaymentProcessorStatusCode(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorStatusCodeByID, new Object[] { paymentProcessorId });
		LOGGER.debug("Deleted payment Processor Status Code by  PaymentProcessorId: " + paymentProcessorId + ", rows affected = " + rows);
	}

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> findByPaymentProcessorId(Long paymentProcessorId) {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>) jdbcTemplate
				.query(Queries.findPaymentProcessorSatusCodesByPPId,
						new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode>(
								new PaymentProcessorStatusCodeRowMapper()));

		if (list != null) {
			LOGGER.debug("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor statuscode not found for payment processor id: " + paymentProcessorId);
		}

		return list;
	}

	@Override
	public void deletePaymentProcessorStatusCode(List<Long> paymentProcessorStatusCodes) {
		Map<String, List<Long>> namedParameters = Collections.singletonMap("paymentProcessorStatusCodes", paymentProcessorStatusCodes);

		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorStatusCodes, new Object[] { namedParameters });

		LOGGER.debug("Deleted List of payment Processor status Codes by Ids " + "rows affected = " + rows);
	
	}
}

class PaymentProcessorStatusCodeRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> {
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode mapRow(ResultSet rs, int row) throws SQLException {
	//SELECT PaymentProcessorStatusCodeID, PaymentProcessorID, PaymentProcessorStatusCode,TransactionType,
		//PaymentProcessorStatusDescription,DateCreated,DatedModified,ModifiedBy FROM PaymentProcessorStatusCode_Lookup WHERE TransactionType=? AND PaymentProcessorStatusCode=? AND PaymentProcessorID=?
		com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode paymentProcessorStatusCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode();
		paymentProcessorStatusCode.setPaymentProcessorStatusCodeId(rs.getLong("PaymentProcessorStatusCodeID"));
		paymentProcessorStatusCode.setPaymentProcessorStatusCode(rs.getString("PaymentProcessorStatusCode"));
		paymentProcessorStatusCode
				.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusDescription"));
		paymentProcessorStatusCode.setTransactionTypeName(rs.getString("TransactionType"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
		paymentProcessorStatusCode.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorStatusCode.setModifiedDate(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		paymentProcessorStatusCode.setLastModifiedBy(rs.getString("ModifiedBy"));

		return paymentProcessorStatusCode;
	}
}
