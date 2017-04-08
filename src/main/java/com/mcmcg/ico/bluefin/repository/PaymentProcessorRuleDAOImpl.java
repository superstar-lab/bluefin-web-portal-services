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
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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

	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> findByCardType(String cardType) {
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.findPaymentProcessorRuleByCardType, new Object[] { cardType },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));
		LOGGER.debug("Number of rows: " + list.size());
		return list;
	}

	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorRule save(com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorRule.getCreatedDate() != null ? paymentProcessorRule.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
//INSERT INTO PaymentProcessor_Rule (PaymentProcessorID, CardType, MaximumMonthlyAmount, NoMaximumMonthlyAmountFlag, Priority, MonthToDateCumulativeAmount, DateCreated,ModifiedBy) VALUES (?,?,?,?,?,?,?,?)
		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.savePaymentProcessorRule,
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
			}
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorRule.setPaymentProcessorRuleId(id);
		LOGGER.info("Saved Payment Processor - id: " + id);
		
		/*if(paymentProcessor.getPaymentProcessorRules() != null && !paymentProcessor.getPaymentProcessorRules().isEmpty()) {
			LOGGER.debug("Number of PaymentprocessorRules childs items {}"+ paymentProcessor.getPaymentProcessorRules().size());
			for (com.mcmcg.ico.bluefin.model.PaymentProcessorRule paymentProcessorRule : paymentProcessor.getPaymentProcessorRules()) {
				paymentProcessorRule.setPaymentProcessor(paymentProcessor);
			}
			paymentProcessorRulesDAO.createPaymentProcessorRules(paymentProcessor.getPaymentProcessorRules());
		}
*/
		return paymentProcessorRule;
	}

	@Override
	public PaymentProcessorRule findOne(long paymentProcessorRuleId) {
		try {
			return jdbcTemplate.queryForObject(Queries.findPaymentProcessorRuleByID, new Object[] { paymentProcessorRuleId },
					new PaymentProcessorRuleRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public void delete(Long paymentProcessorRuleId) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorRuleByID, new Object[] { paymentProcessorRuleId });

		LOGGER.debug("Deleted Payment Processor Rule by Id: " + paymentProcessorRuleId + ", rows affected = " + rows);
	}

	@Override
	public PaymentProcessorRule updatepaymentProcessorRule(PaymentProcessorRule paymentProcessorRuleToUpdate) {
		LOGGER.info("Updating PaymentProcessorRule##"+(paymentProcessorRuleToUpdate.toString()) );
//UPDATE PaymentProcessor_Rule SET PaymentProcessorID= ?, CardType= ?,MaximumMonthlyAmount=?,NoMaximumMonthlyAmountFlag=?,Priority=?,MonthToDateCumulativeAmount=?,CurrentYear=?,CurrentMonth=? WHERE PaymentProcessorRuleID= ?		
		int rows = jdbcTemplate.update(Queries.updatePaymentProcessorRule,
					new Object[] { 	paymentProcessorRuleToUpdate.getPaymentProcessor().getPaymentProcessorId(), paymentProcessorRuleToUpdate.getCardType().name(), paymentProcessorRuleToUpdate.getMaximumMonthlyAmount(), 
							paymentProcessorRuleToUpdate.getNoMaximumMonthlyAmountFlag(), paymentProcessorRuleToUpdate.getPriority(),paymentProcessorRuleToUpdate.getPaymentProcessorRuleId()
								 });
		LOGGER.info("Updated PaymentProcessorRule with ID: " + paymentProcessorRuleToUpdate.getPaymentProcessorRuleId() + ", rows affected = " + rows);
		return paymentProcessorRuleToUpdate;
	}

	@Override
	public List<PaymentProcessorRule> findByPaymentProcessor(Long paymentProcessorId) {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.findPaymentProcessorRulesByPaymentProcessorID,
						new Object[] { paymentProcessorId },
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

		if (paymentProcessorRules != null) {
			LOGGER.debug("Found payment processor statuscode for : ");
		} else {
			LOGGER.debug("Found payment processor rule not found for payment processor id: " + paymentProcessorId);
		}

		return paymentProcessorRules;
	}

	@Override
	public List<PaymentProcessorRule> findAll() {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>) jdbcTemplate
				.query(Queries.findAllProcessorRules,
						new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(
								new PaymentProcessorRuleRowMapper()));

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