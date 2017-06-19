/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
		LOGGER.debug("PaymentProcessorStatusCodeDAOImpl :: findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor() : PaymentProcessorStatusCode size : "
				+list.size());
		paymentProcessorStatusCodeList = DataAccessUtils.singleResult(list);

		if (paymentProcessorStatusCodeList != null) {
			LOGGER.debug("PaymentProcessorStatusCodeDAOImpl :: findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor() : Found payment processor statuscode for : "
					+ paymentProcessorStatusCodeList.getPaymentProcessorStatusCode());
		} else {
			LOGGER.debug("PaymentProcessorStatusCodeDAOImpl :: findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor() : Found payment processor statuscode not found for : " + paymentProcessorStatusCode + "/"
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
			LOGGER.info("Found payment processor statuscode for : ");
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
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for payment processor status code id = {}",paymentProcessorStatusCode);
        	}
			return null;
		}
	}

	@Override
	public void deletePaymentProcessorStatusCode(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorStatusCodeByID, new Object[] { paymentProcessorId });
		LOGGER.debug("PaymentProcessorStatusCodeDAOImpl :: deletePaymentProcessorStatusCode() : Deleted payment Processor Status Code by  PaymentProcessorId: " + paymentProcessorId + ", rows affected = " + rows);
	}

	@Override
	public PaymentProcessorStatusCode save(PaymentProcessorStatusCode paymentProcessorStatusCode) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorStatusCode.getCreatedDate() != null ? paymentProcessorStatusCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTime utc2 =  paymentProcessorStatusCode.getModifiedDate() != null ? paymentProcessorStatusCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.savePaymentProcessorStatusCode,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, paymentProcessorStatusCode.getPaymentProcessor().getPaymentProcessorId()); // ProcessorName
				ps.setString(2, paymentProcessorStatusCode.getPaymentProcessorStatusCode()); // Status Code
				ps.setString(3, paymentProcessorStatusCode.getTransactionTypeName()); // Transaction Type
				ps.setString(4, paymentProcessorStatusCode.getPaymentProcessorStatusCodeDescription()); // ModifiedBy
				ps.setTimestamp(5, dateCreated);
				ps.setTimestamp(6, dateModified);
				ps.setString(7, paymentProcessorStatusCode.getLastModifiedBy());
				return ps;
			}
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorStatusCode.setPaymentProcessorStatusCodeId(id);
		LOGGER.debug("PaymentProcessorStatusCodeDAOImpl :: save() : Saved Payment Processor Status Code - id: " + id);
		return paymentProcessorStatusCode;
	}

	@Override
	public PaymentProcessorStatusCode update(PaymentProcessorStatusCode paymentProcessorStatusCode) {
		DateTime utc1 =  paymentProcessorStatusCode.getModifiedDate() != null ? paymentProcessorStatusCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc1));

		int rows = jdbcTemplate.update(Queries.updatePaymentProcessorStatusCode,
				new Object[] { 	paymentProcessorStatusCode.getPaymentProcessor().getPaymentProcessorId(), paymentProcessorStatusCode.getPaymentProcessorStatusCode(), 
								paymentProcessorStatusCode.getTransactionTypeName(), paymentProcessorStatusCode.getPaymentProcessorStatusCodeDescription(),
								dateModified,paymentProcessorStatusCode.getLastModifiedBy(),paymentProcessorStatusCode.getPaymentProcessorStatusCodeId()
							 });
		
		LOGGER.debug("PaymentProcessorStatusCodeDAOImpl :: update() : Updated Payment Processor Status Code - id: " + paymentProcessorStatusCode.getPaymentProcessorStatusCodeId()+" and affected rows are : "+rows);
		return paymentProcessorStatusCode;
	}
}

class PaymentProcessorStatusCodeRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode> {
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode mapRow(ResultSet rs, int row) throws SQLException {
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
