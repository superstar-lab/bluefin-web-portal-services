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
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorStatusCodeDAOImpl implements PaymentProcessorStatusCodeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorStatusCodeDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public PaymentProcessorStatusCode findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
			String paymentProcessorStatusCode, String transactionTypeName, PaymentProcessor paymentProcessor) {
		PaymentProcessorStatusCode paymentProcessorStatusCodeList;
		
		ArrayList<PaymentProcessorStatusCode> list = (ArrayList<PaymentProcessorStatusCode>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORSTATUSCODEBYCODEID,
						new Object[] { transactionTypeName, paymentProcessorStatusCode,
								paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<PaymentProcessorStatusCode>(
								new PaymentProcessorStatusCodeRowMapper()));
		LOGGER.debug("PaymentProcessorStatusCode size ={} ",list.size());
		paymentProcessorStatusCodeList = DataAccessUtils.singleResult(list);

		if (paymentProcessorStatusCodeList != null) {
			LOGGER.debug("Found payment processor statuscode for ={} ", paymentProcessorStatusCodeList.getPaymentProcessorStatusCodeValue());
		} else {
			LOGGER.debug("Found payment processor statuscode not found for ={} {} Transaction type={} {} PaymentProcessorId={} ", paymentProcessorStatusCode, "/"
					,transactionTypeName, "/", paymentProcessor.getPaymentProcessorId());
		}

		return paymentProcessorStatusCodeList;
	}

	@Override
	public List<PaymentProcessorStatusCode> findByTransactionTypeNameAndPaymentProcessor(
			String transactionTypeName, PaymentProcessor paymentProcessor) {

		ArrayList<PaymentProcessorStatusCode> list = (ArrayList<PaymentProcessorStatusCode>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORSTATUSCODEBYTYPEID,
						new Object[] { transactionTypeName, paymentProcessor.getPaymentProcessorId() },
						new RowMapperResultSetExtractor<PaymentProcessorStatusCode>(
								new PaymentProcessorStatusCodeRowMapper()));

		if (list != null) {
			LOGGER.info("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor statuscode not found for transactionType={} , PaymentProcessorId={} ", transactionTypeName , paymentProcessor.getPaymentProcessorId());
		}
		return list;
	}

	@Override
	public PaymentProcessorStatusCode findOne(Long paymentProcessorStatusCode) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDPAYMENTPROCESSORSTATUSCODEBYID, new Object[] { paymentProcessorStatusCode },
					new PaymentProcessorStatusCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for payment processor status code id = {}",paymentProcessorStatusCode,e);
        	}
			return null;
		}
	}

	@Override
	public void deletePaymentProcessorStatusCode(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORSTATUSCODEBYID, new Object[] { paymentProcessorId });
		LOGGER.debug("Deleted payment Processor Status Code by  PaymentProcessorId ={} , rows affected = {}", paymentProcessorId, rows);
	}

	@Override
	public PaymentProcessorStatusCode save(PaymentProcessorStatusCode paymentProcessorStatusCode) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorStatusCode.getDateCreated() != null ? paymentProcessorStatusCode.getDateCreated().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTime utc2 =  paymentProcessorStatusCode.getDateModified() != null ? paymentProcessorStatusCode.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVEPAYMENTPROCESSORSTATUSCODE,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, paymentProcessorStatusCode.getPaymentProcessor().getPaymentProcessorId()); // ProcessorName
				ps.setString(2, paymentProcessorStatusCode.getPaymentProcessorStatusCodeValue()); // Status Code
				ps.setString(3, paymentProcessorStatusCode.getTransactionTypeName()); // Transaction Type
				ps.setString(4, paymentProcessorStatusCode.getPaymentProcessorStatusCodeDescription()); // ModifiedBy
				ps.setTimestamp(5, dateCreated);
				ps.setTimestamp(6, dateModified);
				ps.setString(7, paymentProcessorStatusCode.getLastModifiedBy());
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorStatusCode.setPaymentProcessorStatusCodeId(id);
		LOGGER.debug("Saved Payment Processor Status Code - id ={} ", id);
		return paymentProcessorStatusCode;
	}

	@Override
	public PaymentProcessorStatusCode update(PaymentProcessorStatusCode paymentProcessorStatusCode) {
		DateTime utc1 =  paymentProcessorStatusCode.getDateModified() != null ? paymentProcessorStatusCode.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc1));

		int rows = jdbcTemplate.update(Queries.UPDATEPAYMENTPROCESSORSTATUSCODE,
				new Object[] { 	paymentProcessorStatusCode.getPaymentProcessor().getPaymentProcessorId(), paymentProcessorStatusCode.getPaymentProcessorStatusCodeValue(), 
								paymentProcessorStatusCode.getTransactionTypeName(), paymentProcessorStatusCode.getPaymentProcessorStatusCodeDescription(),
								dateModified,paymentProcessorStatusCode.getLastModifiedBy(),paymentProcessorStatusCode.getPaymentProcessorStatusCodeId()
							 });
		
		LOGGER.debug("Updated Payment Processor Status Code - id ={}  and affected rows are : {}", paymentProcessorStatusCode.getPaymentProcessorStatusCodeId(),rows);
		return paymentProcessorStatusCode;
	}

	@Override
	public boolean isProcessorStatusCodeMapped(String transactionTypeName, PaymentProcessor paymentProcessor) {
		try {
		Integer count = jdbcTemplate.queryForObject(Queries.ISPROCESSORSTATUSCODEMAPPED,new Object[] { transactionTypeName, paymentProcessor.getPaymentProcessorId() },Integer.class);

		if (count.intValue() > 0) {
			LOGGER.debug("Processor status code mapped, count= {}",count);
			return true;
		} else {
			LOGGER.debug("Found payment processor ={} statuscode not found for transaction type = {}: ", transactionTypeName ,
					paymentProcessor.getPaymentProcessorId());
		}
		}catch(Exception ex) {
			LOGGER.error("isProcessorStatusCodeMapped count cannot be NULL {}",ex.getMessage());
		}
		return false;
	}
}

class PaymentProcessorStatusCodeRowMapper implements RowMapper<PaymentProcessorStatusCode> {
	@Override
	public PaymentProcessorStatusCode mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorStatusCode paymentProcessorStatusCode = new PaymentProcessorStatusCode();
		paymentProcessorStatusCode.setPaymentProcessorStatusCodeId(rs.getLong("PaymentProcessorStatusCodeID"));
		paymentProcessorStatusCode.setPaymentProcessorStatusCodeValue(rs.getString("PaymentProcessorStatusCode"));
		paymentProcessorStatusCode.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusDescription"));
		paymentProcessorStatusCode.setTransactionTypeName(rs.getString("TransactionType"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorStatusCode.setPaymentProcessor(paymentProcessor);
		paymentProcessorStatusCode.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorStatusCode.setDateModified(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		paymentProcessorStatusCode.setLastModifiedBy(rs.getString("ModifiedBy"));

		return paymentProcessorStatusCode;
	}
}
