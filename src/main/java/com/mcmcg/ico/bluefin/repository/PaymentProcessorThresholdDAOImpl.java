/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.model.PaymentProcessorThreshold;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mbaliyan
 *
 */
@Repository
public class PaymentProcessorThresholdDAOImpl implements PaymentProcessorThresholdDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorThresholdDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public PaymentProcessorThreshold save(PaymentProcessorThreshold paymentProcessorThreshold) {
		KeyHolder holder = new GeneratedKeyHolder();

		DateTime utc1 = paymentProcessorThreshold.getCreatedDate() != null
				? paymentProcessorThreshold.getCreatedDate().withZone(DateTimeZone.UTC)
				: DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dateCreatedDateFormat = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dateCreatedDateFormat.print(utc1));
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(Queries.SAVEPAYMENTPROCESSORTHRESHOLD,
					Statement.RETURN_GENERATED_KEYS);
			ps.setBigDecimal(1, paymentProcessorThreshold.getCreditAmountThreshold()); 
			ps.setBigDecimal(2, paymentProcessorThreshold.getDebitAmountThreshold());
			ps.setTimestamp(3, dateCreated); 
			ps.setString(4, paymentProcessorThreshold.getLastModifiedBy());
			return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorThreshold.setPaymentProcessorThresholdId(id);
		LOGGER.debug("Saved Payment Processor - id ={} ", id);
		return paymentProcessorThreshold;
	}

	@Override
	public PaymentProcessorThreshold updatepaymentProcessorThreshold(
			PaymentProcessorThreshold paymentProcessorThreshold) {
		LOGGER.debug("Updating PaymentProcessorThreshold = {}", paymentProcessorThreshold);
		DateTime utc1 = paymentProcessorThreshold.getCreatedDate() != null
				? paymentProcessorThreshold.getCreatedDate().withZone(DateTimeZone.UTC)
				: DateTime.now(DateTimeZone.UTC);

		DateTimeFormatter dateCreatedDateFormat = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dateCreatedDateFormat.print(utc1));
		int rows = jdbcTemplate.update(Queries.UPDATEPAYMENTPROCESSORTHRESHOLD,
				new Object[] { paymentProcessorThreshold.getCreditAmountThreshold(),
						paymentProcessorThreshold.getDebitAmountThreshold(), dateCreated,
						paymentProcessorThreshold.getLastModifiedBy(),
						paymentProcessorThreshold.getPaymentProcessorThresholdId() });
		LOGGER.debug("Updated PaymentProcessorThreshold with ID ={} , rows affected ={} ",
				paymentProcessorThreshold.getPaymentProcessorThresholdId(), rows);
		return paymentProcessorThreshold;
	}

}

class PaymentProcessorThresholdRowMapper implements RowMapper<PaymentProcessorThreshold> {
	@Override
	public PaymentProcessorThreshold mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorThreshold paymentProcessorThreshold = new PaymentProcessorThreshold();
		paymentProcessorThreshold.setPaymentProcessorThresholdId(rs.getLong("PaymentProcessorthresholdId"));
		paymentProcessorThreshold.setCreditAmountThreshold(rs.getBigDecimal("CreditThreshold"));
		paymentProcessorThreshold.setDebitAmountThreshold(rs.getBigDecimal("DebitThreshold"));
		return paymentProcessorThreshold;
	}
}