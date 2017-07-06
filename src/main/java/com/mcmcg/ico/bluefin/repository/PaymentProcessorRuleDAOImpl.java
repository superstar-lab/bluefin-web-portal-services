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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorRuleDAOImpl implements PaymentProcessorRuleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findPaymentProccessorRulByProcessorId(
			Long paymentProcessorId) {
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRULEBYID, new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

		LOGGER.debug("PaymentProcessorRuleDAOImpl :: findPaymentProccessorRulByProcessorId() : Number of rows: " + list.size());
		return list;
	}

	@Override
	public void deletePaymentProcessorRules(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORRULES, new Object[] { paymentProcessorId });
		LOGGER.debug("PaymentProcessorRuleDAOImpl :: deletePaymentProcessorRules() : Deleted Payment Processor Rules for PaymentProcessor Id: " + paymentProcessorId
				+ " rows affected: " + rows);
	}

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findByCardType(String cardType) {
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRULEBYCARDTYPE, new Object[] { cardType },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));
		LOGGER.debug("PaymentProcessorRuleDAOImpl :: findByCardType() : Number of rows: " + list.size());
		return list;
	}

	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule save(com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorRule.getCreatedDate() != null ? paymentProcessorRule.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		
		DateTimeFormatter dateCreatedDateFormat = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dateCreatedDateFormat.print(utc1));
		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVEPAYMENTPROCESSORRULE,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, paymentProcessorRule.getPaymentProcessor().getPaymentProcessorId()); // PaymentProcessorID
				ps.setString(2, paymentProcessorRule.getCardType().name()); // DateCreated
				ps.setBigDecimal(3, paymentProcessorRule.getMaximumMonthlyAmount()); // DateModified
				ps.setShort(4, paymentProcessorRule.getNoMaximumMonthlyAmountFlag()); 
				ps.setShort(5, paymentProcessorRule.getPriority()); // ModifiedBy
				ps.setBigDecimal(6, paymentProcessorRule.getMonthToDateCumulativeAmount());
				ps.setTimestamp(7, dateCreated);
				ps.setString(8, paymentProcessorRule.getLastModifiedBy());
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorRule.setPaymentProcessorRuleId(id);
		LOGGER.debug("PaymentProcessorRuleDAOImpl :: save() : Saved Payment Processor - id: " + id);
		return paymentProcessorRule;
	}

	@Override
	public PaymentProcessorRule findOne(long paymentProcessorRuleId) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDPAYMENTPROCESSORRULEBY_ID, new Object[] { paymentProcessorRuleId },
					new PaymentProcessorRuleRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for payment processor rule id = {}",paymentProcessorRuleId,e);
        	}
			return null;
		}
	}

	@Override
	public void delete(Long paymentProcessorRuleId) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORRULEBYID, new Object[] { paymentProcessorRuleId });

		LOGGER.debug("PaymentProcessorRuleDAOImpl :: delete() : Deleted Payment Processor Rule by Id: " + paymentProcessorRuleId + ", rows affected = " + rows);
	}

	@Override
	public PaymentProcessorRule updatepaymentProcessorRule(PaymentProcessorRule paymentProcessorRuleToUpdate) {
		LOGGER.debug("PaymentProcessorRuleDAOImpl :: updatepaymentProcessorRule() : Updating PaymentProcessorRule##"+(paymentProcessorRuleToUpdate.toString()) );
		int rows = jdbcTemplate.update(Queries.UPDATEPAYMENTPROCESSORRULE,
					new Object[] { 	paymentProcessorRuleToUpdate.getPaymentProcessor().getPaymentProcessorId(), paymentProcessorRuleToUpdate.getCardType().name(), paymentProcessorRuleToUpdate.getMaximumMonthlyAmount(), 
							paymentProcessorRuleToUpdate.getNoMaximumMonthlyAmountFlag(), paymentProcessorRuleToUpdate.getPriority(),paymentProcessorRuleToUpdate.getPaymentProcessorRuleId()
								 });
		LOGGER.debug("PaymentProcessorRuleDAOImpl :: updatepaymentProcessorRule() : Updated PaymentProcessorRule with ID: " + paymentProcessorRuleToUpdate.getPaymentProcessorRuleId() + ", rows affected = " + rows);
		return paymentProcessorRuleToUpdate;
	}

	@Override
	public List<PaymentProcessorRule> findByPaymentProcessor(Long paymentProcessorId) {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRULESBYPAYMENTPROCESSORID,
						new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

		if (paymentProcessorRules != null) {
			LOGGER.info("PaymentProcessorRuleDAOImpl :: findByPaymentProcessor() : Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("PaymentProcessorRuleDAOImpl :: findByPaymentProcessor() : Found payment processor rule not found for payment processor id: " + paymentProcessorId);
		}

		return paymentProcessorRules;
	}

	@Override
	public List<PaymentProcessorRule> findAll() {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.FINDALLPROCESSORRULES,
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));
		LOGGER.debug("PaymentProcessorRuleDAOImpl :: findAll() : paymentProcessorRules size : "+paymentProcessorRules.size());

		return paymentProcessorRules;
	}

}

class PaymentProcessorRuleRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> {
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule mapRow(ResultSet rs, int row) throws SQLException {
		com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule = new com.mcmcg.ico.bluefin.model.PaymentProcessorRule();
		paymentProcessorRule.setPaymentProcessorRuleId(rs.getLong("PaymentProcessorRuleID"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorRule.setPaymentProcessor(paymentProcessor);
		paymentProcessorRule.setCardType(CardType.valueOf(rs.getString("CardType")));
		paymentProcessorRule.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorRule.setLastModifiedBy(rs.getString("ModifiedBy"));
		paymentProcessorRule.setMaximumMonthlyAmount(rs.getBigDecimal("MaximumMonthlyAmount"));
		paymentProcessorRule.setMonthToDateCumulativeAmount(rs.getBigDecimal("MonthToDateCumulativeAmount"));
		paymentProcessorRule.setNoMaximumMonthlyAmountFlag(rs.getShort("NoMaximumMonthlyAmountFlag"));
		paymentProcessorRule.setPriority(rs.getShort("Priority"));

		return paymentProcessorRule;
	}
}