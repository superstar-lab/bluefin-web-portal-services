/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorDAOImpl implements PaymentProcessorDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PaymentProcessorMerchantDAO paymentProcessorMerchantDAO;
	
	@Override
	public PaymentProcessor findByPaymentProcessorId(Long paymentProcessorId) {
		try {
			return jdbcTemplate.queryForObject(Queries.findPaymentProcessorById, new Object[] { paymentProcessorId },
					new PaymentProcessorRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for payment processor id = {}",paymentProcessorId,e);
        	}
			return null;
		}
	}

	@Override
	public List<PaymentProcessor> findAll() {
		List<PaymentProcessor> paymentProcessors = jdbcTemplate.query(Queries.findAllPaymentProcessors,
				new PaymentProcessorRowMapper());

		LOGGER.debug("PaymentProcessorDAOImpl :: findAll() : Number of rows: " + paymentProcessors.size());

		return paymentProcessors;
	}

	@Override
	public void delete(PaymentProcessor paymentProcessor) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorByID, new Object[] { paymentProcessor.getPaymentProcessorId() });
		LOGGER.debug("PaymentProcessorDAOImpl :: delete() : Deleted payment Processor by Id: " + paymentProcessor.getPaymentProcessorId() + ", rows affected = " + rows);
	}

	@Override
	public List<PaymentProcessor> findAll(Set<Long> paymentProcessorIds) {
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		Map<String, Set<Long>> map = Collections.singletonMap("paymentProcessorIds", paymentProcessorIds);
		List<PaymentProcessor> paymentProcessors = namedParameterJdbcTemplate.query(Queries.findAllPaymentProcessorsByIds,
				map, new PaymentProcessorRowMapper());

		LOGGER.debug("PaymentProcessorDAOImpl :: findAll(set) : Number of rows: " + paymentProcessors.size());

		return paymentProcessors;
	}

	@Override
	public PaymentProcessor getPaymentProcessorByProcessorName(String processorName) {
		try {
			return jdbcTemplate.queryForObject(Queries.findPaymentProcessorByName, new Object[] { processorName },
					new PaymentProcessorRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for payment processor name = {}",processorName,e);
        	}
			return null;
		}
	}

	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessor save(PaymentProcessor paymentProcessor) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessor.getCreatedDate() != null ? paymentProcessor.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTime utc2 =  paymentProcessor.getModifiedDate() != null ? paymentProcessor.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.savePaymentProcessors,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, paymentProcessor.getProcessorName()); // ProcessorName
				ps.setTimestamp(2, dateCreated); // DateCreated
				ps.setTimestamp(3, dateModified); // DateModified
				ps.setString(4, paymentProcessor.getLastModifiedBy()); // ModifiedBy
				ps.setShort(5, paymentProcessor.getIsActive());
				ps.setTime(6, paymentProcessor.getRemitTransactionOpenTime());
				ps.setTime(7, paymentProcessor.getRemitTransactionCloseTime());
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessor.setPaymentProcessorId(id);
		LOGGER.debug("PaymentProcessorDAOImpl :: save() : Saved Payment Processor - id: " + id);
		return paymentProcessor;
	}

	@Override
	public PaymentProcessor update(PaymentProcessor paymentProcessor) {
		if (paymentProcessor.getPaymentProcessorMerchants() == null) {
			paymentProcessorMerchantDAO.deletPaymentProcessorMerchantByProcID(paymentProcessor.getPaymentProcessorId());
		}
		if (paymentProcessor.getPaymentProcessorRules() == null) {
			paymentProcessorMerchantDAO.deletePaymentProcessorRules(paymentProcessor.getPaymentProcessorId());
		}
		LOGGER.debug("PaymentProcessorDAOImpl :: update() : Updating Payment Processor, PaymentProcessorId - "+(paymentProcessor.getPaymentProcessorId()));
		DateTime utc4 = paymentProcessor.getModifiedDate() != null ? paymentProcessor.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC); 
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc4));
//UPDATE PaymentProcessor_Lookup SET ProcessorName=?,IsActive=?,RemitTransactionOpenTime=?,RemitTransactionCloseTime=?,DatedModified=? WHERE PaymentProcessorID=?		
		int rows = jdbcTemplate.update(Queries.updatePaymentProcessor,
					new Object[] { 	paymentProcessor.getProcessorName(), paymentProcessor.getIsActive(), paymentProcessor.getRemitTransactionOpenTime(),
							paymentProcessor.getRemitTransactionOpenTime(), dateModified, 
							paymentProcessor.getPaymentProcessorId()
								 });
		LOGGER.debug("PaymentProcessorDAOImpl :: update() : Updated PaymentProcessor, No of Rows Updated " + rows);
		return paymentProcessor;
	}
}

class PaymentProcessorRowMapper implements RowMapper<PaymentProcessor> {
	@Override
	public PaymentProcessor mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessor.setProcessorName(rs.getString("ProcessorName"));
		paymentProcessor.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessor.setModifiedDate(new DateTime(rs.getTimestamp("DatedModified"))); // Misspelled
		paymentProcessor.setLastModifiedBy(rs.getString("ModifiedBy"));
		paymentProcessor.setIsActive(rs.getShort("IsActive"));
		paymentProcessor.setRemitTransactionOpenTime(rs.getTime("RemitTransactionOpenTime"));
		paymentProcessor.setRemitTransactionCloseTime(rs.getTime("RemitTransactionCloseTime"));
		
		
		return paymentProcessor;
	}
}
