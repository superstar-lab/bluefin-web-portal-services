/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorRuleDAOImpl implements PaymentProcessorRuleDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleDAOImpl.class);
	private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findPaymentProccessorRulByProcessorId(
			Long paymentProcessorId) {
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.findPaymentProcessorRuleById, new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

		LOGGER.debug("Number of rows: " + list.size());
		return list;
	}

	@Override
	public void createPaymentProcessorRules(Collection<PaymentProcessorRule> paymentProcessorInternalStatusCodes) {
		insertBatch(new ArrayList<PaymentProcessorRule>(paymentProcessorInternalStatusCodes));
	}

	private void insertBatch(final List<PaymentProcessorRule> paymentProcessorRules) {
		jdbcTemplate.batchUpdate(Queries.savePaymentProcessorRules, new BatchPreparedStatementSetter() {
			// INSERT INTO PaymentProcessor_Rule (PaymentProcessorRuleID,
			// PaymentProcessorID, CardType, MaximumMonthlyAmount,
			// NoMaximumMonthlyAmountFlag,
			// Priority, MonthToDateCumulativeAmount,
			// CurrentYear,CurrentMonth,DateCreated,ModifiedBy) VALUES
			// (?,?,?,?,?,?,?,?,?,?,?)
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PaymentProcessorRule paymentProcessorRule = paymentProcessorRules.get(i);
				DateTime utc1 = paymentProcessorRule.getCreatedDate() != null
						? paymentProcessorRule.getCreatedDate().withZone(DateTimeZone.UTC)
						: DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
				LOGGER.info("Creating child item , paymentProcessorRule ="
						+ (paymentProcessorRule.getPaymentProcessorRuleId()) + " , PaymentProcessorID="
						+ paymentProcessorRule.getPaymentProcessor().getPaymentProcessorId());
				ps.setLong(1, paymentProcessorRule.getPaymentProcessor().getPaymentProcessorId());
				// ps.setString(2,
				// paymentProcessorRule.getCardType().toString());
				ps.setTimestamp(2, dateCreated);
				ps.setString(3, paymentProcessorRule.getLastModifiedBy());
			}

			@Override
			public int getBatchSize() {
				return paymentProcessorRules.size();
			}
		});
	}

	@Override
	public void deletePaymentProcessorRules(Long paymentProcessorId) {

		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorRules, new Object[] { paymentProcessorId });

		LOGGER.debug("Deleted Payment Processor Rules for PaymentProcessor Id: " + paymentProcessorId
				+ ", rows affected = " + rows);

	}

}

class PaymentProcessorRuleRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> {
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule mapRow(ResultSet rs, int row) throws SQLException {
		com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule = new com.mcmcg.ico.bluefin.model.PaymentProcessorRule();
		paymentProcessorRule.setPaymentProcessorRuleId(rs.getLong("PaymentProcessorRuleID"));
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