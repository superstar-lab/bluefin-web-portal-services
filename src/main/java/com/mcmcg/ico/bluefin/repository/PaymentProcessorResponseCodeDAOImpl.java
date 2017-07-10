/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
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
	public PaymentProcessorResponseCode findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorResponseCode, String transactionTypeName, PaymentProcessor paymentProcessor) {
		PaymentProcessorResponseCode paymentProcessorStatusCodeList;

		ArrayList<PaymentProcessorResponseCode> list = (ArrayList<PaymentProcessorResponseCode>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRESPONSECODEBYCODEID,
						new Object[] { transactionTypeName, paymentProcessorResponseCode,
								paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<PaymentProcessorResponseCode>(
								new PaymentProcessorResponseCodeRowMapper()));
		LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: findByPaymentProcessorResponseCodeAndTransactionTypeNameAndPaymentProcessor() : PaymentProcessorResponseCode size : "+list.size());
		paymentProcessorStatusCodeList = DataAccessUtils.singleResult(list);

		if (paymentProcessorStatusCodeList != null) {
			LOGGER.debug("Found payment processor statuscode for : "
					+ paymentProcessorStatusCodeList.getPaymentProcessorResponseCodeValue());
		} else {
			LOGGER.debug("Found payment processor statuscode not found for : " + paymentProcessorResponseCode + "/"
					+ transactionTypeName + "/" + paymentProcessor.getPaymentProcessorId());
		}

		return paymentProcessorStatusCodeList;
	
	}

	@Override
	public List<PaymentProcessorResponseCode> findByTransactionTypeNameAndPaymentProcessor(String transactionTypeName,PaymentProcessor paymentProcessor) {

		ArrayList<PaymentProcessorResponseCode> list = (ArrayList<PaymentProcessorResponseCode>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRESPONSECODEBYTYPEID,
						new Object[] { transactionTypeName, paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<PaymentProcessorResponseCode>(
								new PaymentProcessorResponseCodeRowMapper()));

		if (list != null) {
			LOGGER.debug("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor ={} statuscode not found for transaction type = {}: ", transactionTypeName ,
					paymentProcessor.getPaymentProcessorId());
		}

		return list;
	}

	@Override
	public void deletePaymentProcessorResponseCode(Long paymentProcessorId) {
			int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORRESPONSECODEBYID, new Object[] { paymentProcessorId });
			LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: deletePaymentProcessorResponseCode() : Deleted payment Processor Response Code by  PaymentProcessorId: " + paymentProcessorId + ", rows affected = " + rows);
	}

	@Override
	public PaymentProcessorResponseCode save(PaymentProcessorResponseCode paymentProcessorResponseCode) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorResponseCode.getDateCreated() != null ? paymentProcessorResponseCode.getDateCreated().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTime utc2 =  paymentProcessorResponseCode.getDateModified() != null ? paymentProcessorResponseCode.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVEPAYMENTPROCESSORRESPONSECODE,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, paymentProcessorResponseCode.getPaymentProcessor().getPaymentProcessorId()); // ProcessorName
				ps.setString(2, paymentProcessorResponseCode.getPaymentProcessorResponseCodeValue()); // DateCreated
				ps.setString(3, paymentProcessorResponseCode.getTransactionTypeName()); // DateModified
				ps.setString(4, paymentProcessorResponseCode.getPaymentProcessorResponseCodeDescription()); // ModifiedBy
				ps.setTimestamp(5, dateCreated);
				ps.setTimestamp(6, dateModified);
				ps.setString(7, paymentProcessorResponseCode.getLastModifiedBy());
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeId(id);
		LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: PaymentProcessorResponseCode() : Saved Payment Processor Response Code - id: " + id);
		return paymentProcessorResponseCode;
	}

	@Override
	public PaymentProcessorResponseCode findOne(Long paymentProcessorCodeId) {
		try {
			PaymentProcessorResponseCode paymentProcessorResponseCode = jdbcTemplate.queryForObject(Queries.FINDPAYMENTPROCESSORRESPONSECODEBYID, new Object[] { paymentProcessorCodeId },
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
		DateTime utc1 =  paymentProcessorResponseCode.getDateModified() != null ? paymentProcessorResponseCode.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc1));

		int rows = jdbcTemplate.update(Queries.UPDATEPAYMENTPROCESSORRESPONSECODE,
				new Object[] { 	paymentProcessorResponseCode.getPaymentProcessor().getPaymentProcessorId(), paymentProcessorResponseCode.getPaymentProcessorResponseCodeValue(), 
								paymentProcessorResponseCode.getTransactionTypeName(), paymentProcessorResponseCode.getPaymentProcessorResponseCodeDescription(),
								dateModified,paymentProcessorResponseCode.getLastModifiedBy(),paymentProcessorResponseCode.getPaymentProcessorResponseCodeId()
							 });
		
		LOGGER.debug("PaymentProcessorResponseCodeDAOImpl :: update() : Updated Payment Processor Response Code - id: " + paymentProcessorResponseCode.getPaymentProcessorResponseCodeId() + "Number of rows updated - " + rows);
		return paymentProcessorResponseCode;
	}
	
	
}

class PaymentProcessorResponseCodeRowMapper implements RowMapper<PaymentProcessorResponseCode> {
	@Override
	public PaymentProcessorResponseCode mapRow(ResultSet rs, int row) throws SQLException {
		Timestamp ts;
		PaymentProcessorResponseCode paymentProcessorResponseCode = new PaymentProcessorResponseCode();
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeId(rs.getLong("PaymentProcessorResponseCodeID"));
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeValue(rs.getString("PaymentProcessorResponseCode"));
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		paymentProcessorResponseCode.setTransactionTypeName(rs.getString("TransactionType"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorResponseCode.setPaymentProcessor(paymentProcessor);
		if(rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			paymentProcessorResponseCode.setDateCreated(new DateTime(ts));
		}
		
		if(rs.getString("DatedModified") != null) {
			ts = Timestamp.valueOf(rs.getString("DatedModified"));
			paymentProcessorResponseCode.setDateModified(new DateTime(ts));
		}
		
		paymentProcessorResponseCode.setLastModifiedBy(rs.getString("ModifiedBy"));

		return paymentProcessorResponseCode;
	}
}