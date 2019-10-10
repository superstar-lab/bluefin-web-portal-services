package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRuleTrendsRequest;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorRuleTrendsDAOImpl implements PaymentProcessorRuleTrendsDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleTrendsDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<PaymentProcessorRule> getTrendsByFrequencyandDateRange(
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest) {
		List<PaymentProcessorRule> list = jdbcTemplate.query(Queries.FINDTRENDSBYFREQUENCY,
				new Object[] { paymentProcessorRuleTrendsRequest.getFrequencyType(),
						paymentProcessorRuleTrendsRequest.getStartDate(),
						paymentProcessorRuleTrendsRequest.getEndDate()},
				new RowMapperResultSetExtractor<PaymentProcessorRule>(new PaymentProcessorRuleTrendsRowMapper()));

		LOGGER.debug("Number of rows={} ", list.size());
		return list;
	}

}

class PaymentProcessorRuleTrendsRowMapper implements RowMapper<PaymentProcessorRule> {
	@Override
	public PaymentProcessorRule mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorRule paymentProcessorRule = new PaymentProcessorRule();
		paymentProcessorRule.setPaymentProcessorRuleId(rs.getLong("PaymentProcessorRuleID"));
		PaymentProcessor paymentProcessor = new PaymentProcessor();
		paymentProcessor.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorRule.setPaymentProcessor(paymentProcessor);
		paymentProcessorRule.setResetFrequency(rs.getString("ResetFrequency"));
		paymentProcessorRule.setCardType(CardType.valueOf(rs.getString("CardType")));
		paymentProcessorRule.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorRule.setHistoryCreationDate(new DateTime(rs.getTimestamp("HistoryDateCreated")));
		paymentProcessorRule.setLastModifiedBy(rs.getString("ModifiedBy"));
		paymentProcessorRule.setConsumedPercentage(rs.getBigDecimal("ConsumedPercentage"));
		paymentProcessorRule.setTargetPercentage(rs.getBigDecimal("TargetPercentage"));
		paymentProcessorRule.setIsRuleActive(rs.getInt("IsActive"));
		paymentProcessorRule.setMaximumMonthlyAmount(rs.getBigDecimal("MaximumMonthlyAmount"));
		paymentProcessorRule.setMonthToDateCumulativeAmount(rs.getBigDecimal("MonthToDateCumulativeAmount"));
		paymentProcessorRule.setNoMaximumMonthlyAmountFlag(rs.getShort("NoMaximumMonthlyAmountFlag"));
		return paymentProcessorRule;
	}
}
