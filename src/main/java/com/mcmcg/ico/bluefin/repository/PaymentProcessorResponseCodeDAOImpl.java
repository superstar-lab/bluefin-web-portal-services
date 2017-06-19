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
import java.util.List;

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
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
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
		LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor() : PaymentProcessorResponseCode size : "+list.size());
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
			LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: findByTransactionTypeNameAndPaymentProcessor() : Found payment processor statuscode not found for : " + transactionTypeName + "/"
					+ paymentProcessor.getPaymentProcessorId());
		}

		return list;
	}

	@Override
	public void deletePaymentProcessorResponseCode(Long paymentProcessorId) {
			int rows = jdbcTemplate.update(Queries.deletePaymentProcessorResponseCodeByID, new Object[] { paymentProcessorId });
			LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: deletePaymentProcessorResponseCode() : Deleted payment Processor Response Code by  PaymentProcessorId: " + paymentProcessorId + ", rows affected = " + rows);
	}

	@Override
	public PaymentProcessorResponseCode save(PaymentProcessorResponseCode paymentProcessorResponseCode) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorResponseCode.getCreatedDate() != null ? paymentProcessorResponseCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTime utc2 =  paymentProcessorResponseCode.getModifiedDate() != null ? paymentProcessorResponseCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.savePaymentProcessorResponseCode,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, paymentProcessorResponseCode.getPaymentProcessor().getPaymentProcessorId()); // ProcessorName
				ps.setString(2, paymentProcessorResponseCode.getPaymentProcessorResponseCode()); // DateCreated
				ps.setString(3, paymentProcessorResponseCode.getTransactionTypeName()); // DateModified
				ps.setString(4, paymentProcessorResponseCode.getPaymentProcessorResponseCodeDescription()); // ModifiedBy
				ps.setTimestamp(5, dateCreated);
				ps.setTimestamp(6, dateModified);
				ps.setString(7, paymentProcessorResponseCode.getLastModifiedBy());
				return ps;
			}
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeId(id);
		LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: PaymentProcessorResponseCode() : Saved Payment Processor Response Code - id: " + id);
		return paymentProcessorResponseCode;
	}

	@Override
	public PaymentProcessorResponseCode findOne(Long paymentProcessorCodeId) {
		try {
			PaymentProcessorResponseCode paymentProcessorResponseCode = jdbcTemplate.queryForObject(Queries.findPaymentProcessorResponseCodeByID, new Object[] { paymentProcessorCodeId },
					new PaymentProcessorResponseCodeRowMapper());
			LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: findOne() : paymentProcessorResponseCode " + paymentProcessorResponseCode);
			return paymentProcessorResponseCode;
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for payment processor response code id = {}",paymentProcessorCodeId,e);
        	}
			return null;
		}
		
	}

	@Override
	public PaymentProcessorResponseCode update(PaymentProcessorResponseCode paymentProcessorResponseCode) {
		DateTime utc1 =  paymentProcessorResponseCode.getModifiedDate() != null ? paymentProcessorResponseCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc1));

		int rows = jdbcTemplate.update(Queries.updatePaymentProcessorResponseCode,
				new Object[] { 	paymentProcessorResponseCode.getPaymentProcessor().getPaymentProcessorId(), paymentProcessorResponseCode.getPaymentProcessorResponseCode(), 
								paymentProcessorResponseCode.getTransactionTypeName(), paymentProcessorResponseCode.getPaymentProcessorResponseCodeDescription(),
								dateModified,paymentProcessorResponseCode.getLastModifiedBy(),paymentProcessorResponseCode.getPaymentProcessorResponseCodeId()
							 });
		
		LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: update() : Updated Payment Processor Response Code - id: " + paymentProcessorResponseCode.getPaymentProcessorResponseCodeId() + "Number of rows updated - " + rows);
		return paymentProcessorResponseCode;
	}
	
	
}

class PaymentProcessorResponseCodeRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode> {
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode mapRow(ResultSet rs, int row) throws SQLException {
		Timestamp ts =null;
		com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode paymentProcessorResponseCode = new com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode();
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeId(rs.getLong("PaymentProcessorResponseCodeID"));
		paymentProcessorResponseCode.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		paymentProcessorResponseCode.setTransactionTypeName(rs.getString("TransactionType"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
		if(rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			paymentProcessorResponseCode.setCreatedDate(new DateTime(ts));
		}
		
		if(rs.getString("DatedModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			paymentProcessorResponseCode.setModifiedDate(new DateTime(ts));
		}
		
		paymentProcessorResponseCode.setLastModifiedBy(rs.getString("ModifiedBy"));

		return paymentProcessorResponseCode;
	}
}