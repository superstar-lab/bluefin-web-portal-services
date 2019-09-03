/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.math.BigDecimal;
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

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<PaymentProcessorRule> findPaymentProccessorRulByProcessorId(
			Long paymentProcessorId) {
		List<PaymentProcessorRule> list = jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRULEBYID, new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

		LOGGER.debug("Number of rows={} ", list.size());
		return list;
	}

	@Override
	public void deletePaymentProcessorRules(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORRULES, new Object[] { paymentProcessorId });
		LOGGER.debug("Deleted Payment Processor Rules for PaymentProcessor Id ={} , rows affected={} ", paymentProcessorId
				, rows);
	}

	@Override
	public List<PaymentProcessorRule> findByCardType(String cardType) {
		List<PaymentProcessorRule> list =  jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRULEBYCARDTYPE, new Object[] { cardType },
						new RowMapperResultSetExtractor<PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));
		LOGGER.debug("Number of rows ={}", list.size());
		return list;
	}

	@Override
	public PaymentProcessorRule save(PaymentProcessorRule paymentProcessorRule) {
		KeyHolder holder = new GeneratedKeyHolder();
		if (paymentProcessorRule.hasNoLimit()) {
	//		paymentProcessorRule.setMaximumMonthlyAmount(BigDecimal.ZERO);
		}
		DateTime utc1 = paymentProcessorRule.getCreatedDate() != null ? paymentProcessorRule.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		
		DateTimeFormatter dateCreatedDateFormat = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dateCreatedDateFormat.print(utc1));
		jdbcTemplate.update(connection->{
				PreparedStatement ps = connection.prepareStatement(Queries.SAVEPAYMENTPROCESSORRULE,
						Statement.RETURN_GENERATED_KEYS);
				ps.setLong(1, paymentProcessorRule.getPaymentProcessor().getPaymentProcessorId()); // PaymentProcessorID
				ps.setString(2, paymentProcessorRule.getCardType().name()); // DateCreated
				ps.setShort(3, paymentProcessorRule.getNoMaximumMonthlyAmountFlag()); 
				ps.setBigDecimal(4, paymentProcessorRule.getMaximumMonthlyAmount()); 
	//			ps.setShort(4, paymentProcessorRule.getPriority()); // ModifiedBy
				ps.setBigDecimal(5, paymentProcessorRule.getMonthToDateCumulativeAmount());
				ps.setBigDecimal(6, paymentProcessorRule.getConsumedPercentage());
				ps.setBigDecimal(7, paymentProcessorRule.getTargetPercentage());
				ps.setTimestamp(8, dateCreated);
				ps.setString(9, paymentProcessorRule.getLastModifiedBy());
				return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorRule.setPaymentProcessorRuleId(id);
		LOGGER.debug("Saved Payment Processor - id ={} ", id);
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

		LOGGER.debug("Deleted Payment Processor Rule by Id={} , rows affected = {}", paymentProcessorRuleId, rows);
	}

	@Override
	public PaymentProcessorRule updatepaymentProcessorRule(PaymentProcessorRule paymentProcessorRuleToUpdate) {
		LOGGER.debug("Updating PaymentProcessorRule = {}", paymentProcessorRuleToUpdate );
		if (paymentProcessorRuleToUpdate.hasNoLimit()) {
	//		paymentProcessorRuleToUpdate.setMaximumMonthlyAmount(BigDecimal.ZERO);
		}
		int rows = jdbcTemplate.update(Queries.UPDATEPAYMENTPROCESSORRULE,
				new Object[] { paymentProcessorRuleToUpdate.getPaymentProcessor().getPaymentProcessorId(),
						paymentProcessorRuleToUpdate.getCardType().name(),
						paymentProcessorRuleToUpdate.getMaximumMonthlyAmount(),
						paymentProcessorRuleToUpdate.getTargetPercentage(),
						paymentProcessorRuleToUpdate.getConsumedPercentage(),
						paymentProcessorRuleToUpdate.getNoMaximumMonthlyAmountFlag(),
						paymentProcessorRuleToUpdate.getPaymentProcessorRuleId() });
		LOGGER.debug("Updated PaymentProcessorRule with ID ={} , rows affected ={} ", paymentProcessorRuleToUpdate.getPaymentProcessorRuleId(), rows);
		return paymentProcessorRuleToUpdate;
	}

	@Override
	public List<PaymentProcessorRule> findByPaymentProcessor(Long paymentProcessorId) {
		ArrayList<PaymentProcessorRule> paymentProcessorRules = (ArrayList<PaymentProcessorRule>) jdbcTemplate
				.query(Queries.FINDPAYMENTPROCESSORRULESBYPAYMENTPROCESSORID,
						new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

		if (paymentProcessorRules != null) {
			LOGGER.info("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor rule not found for payment processor id ={} ", paymentProcessorId);
		}

		return paymentProcessorRules;
	}

	@Override
	public List<PaymentProcessorRule> findAll() {
		ArrayList<PaymentProcessorRule> paymentProcessorRules = (ArrayList<PaymentProcessorRule>) jdbcTemplate
				.query(Queries.FINDALLPROCESSORRULES,
						new RowMapperResultSetExtractor<PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));
		LOGGER.debug("paymentProcessorRules size ={} ",paymentProcessorRules.size());

		return paymentProcessorRules;
	}

}

class PaymentProcessorRuleRowMapper implements RowMapper<PaymentProcessorRule> {
	@Override
	public PaymentProcessorRule mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorRule paymentProcessorRule = new PaymentProcessorRule();
		paymentProcessorRule.setPaymentProcessorRuleId(rs.getLong("PaymentProcessorRuleID"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorRule.setPaymentProcessor(paymentProcessor);
		paymentProcessorRule.setCardType(CardType.valueOf(rs.getString("CardType")));
		paymentProcessorRule.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorRule.setLastModifiedBy(rs.getString("ModifiedBy"));
	//	paymentProcessorRule.setMaximumMonthlyAmount(rs.getBigDecimal("MaximumMonthlyAmount"));
		paymentProcessorRule.setMonthToDateCumulativeAmount(rs.getBigDecimal("MonthToDateCumulativeAmount"));
		paymentProcessorRule.setNoMaximumMonthlyAmountFlag(rs.getShort("NoMaximumMonthlyAmountFlag"));
		paymentProcessorRule.setPriority(rs.getShort("Priority"));

		return paymentProcessorRule;
	}
}