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

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

/**
 * @author mmishra
 *
 */
@Repository
public class PaymentProcessorMerchantDAOImpl implements PaymentProcessorMerchantDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorMerchantDAOImpl.class);
	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<PaymentProcessorMerchant> findPaymentProccessorMerchantByProcessorId(Long paymentProcessorId) {
		List<PaymentProcessorMerchant> list = (ArrayList<PaymentProcessorMerchant>) jdbcTemplate.query(
				Queries.FINDPAYMENTPROCESSORMERCHANTSBYID, new Object[] { paymentProcessorId },
				new RowMapperResultSetExtractor<PaymentProcessorMerchant>(new PaymentProcessorMerchantRowMapper()));

		LOGGER.debug("Number of rows={} ", list.size());
		return list;
	}

	@Override
	public void deletPaymentProcessorMerchantByProcID(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORMERCHANTBYPROCID, new Object[] {paymentProcessorId});

		LOGGER.debug("Deleted Payment Processor Merchant by Payment Processor Id={} , rows affected = {}", paymentProcessorId , rows);
	}

	@Override
	public void deletePaymentProcessorRules(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORMERCHANTS, new Object[] { paymentProcessorId });
		LOGGER.debug("Deleted Payment Processor Merchants for PaymentProcessor Id ={} , rows affected = {}" , paymentProcessorId
				, rows);
	}
	
	@Override
	public void createPaymentProcessorMerchants(Collection<PaymentProcessorMerchant> paymentProcessorMerchants) {
		insertBatch(new ArrayList<PaymentProcessorMerchant>(paymentProcessorMerchants) );
	}

	private void insertBatch(final List<PaymentProcessorMerchant> paymentProcessorMerchants){
		jdbcTemplate.batchUpdate(Queries.SAVEPAYMENTPROCESSORMARCHENT, new PaymentProcessorInsertBatchPreparedStatement(paymentProcessorMerchants));
	}

}

class PaymentProcessorInsertBatchPreparedStatement implements BatchPreparedStatementSetter {
	
	private final List<PaymentProcessorMerchant> paymentProcessorMerchants;
	private static final DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
	
	public PaymentProcessorInsertBatchPreparedStatement(List<PaymentProcessorMerchant> paymentProcessorMerchants){
		this.paymentProcessorMerchants = paymentProcessorMerchants;
	}
	@Override
	public void setValues(PreparedStatement ps, int i) throws SQLException {
		PaymentProcessorMerchant paymentProcessorMerchant = paymentProcessorMerchants.get(i);
		DateTime utc1 = paymentProcessorMerchant.getCreatedDate() != null ? paymentProcessorMerchant.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		DateTime utc2 = paymentProcessorMerchant.getModifiedDate() != null ? paymentProcessorMerchant.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		Timestamp modifyDate = Timestamp.valueOf(dtf.print(utc2));
		ps.setLong(1, paymentProcessorMerchant.getPaymentProcessorId());
		ps.setShort(2, paymentProcessorMerchant.getTestOrProd());
		ps.setString(3,paymentProcessorMerchant.getMerchantId() );
		ps.setTimestamp(4, dateCreated);
		ps.setTimestamp(5, modifyDate);
		ps.setLong(6, paymentProcessorMerchant.getLegalEntityAppId());
	}
	@Override
	public int getBatchSize() {
		return paymentProcessorMerchants.size();
	}
}

class PaymentProcessorMerchantRowMapper implements RowMapper<PaymentProcessorMerchant> {
	@Override
	public PaymentProcessorMerchant mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorMerchant paymentProcessorMerchant = new PaymentProcessorMerchant();
		paymentProcessorMerchant.setMerchantId(rs.getString("MerchantID"));
		paymentProcessorMerchant.setPaymentProcessorMechantId(rs.getLong("PaymentProcessorMerchantID"));
		paymentProcessorMerchant.setLastModifiedBy(rs.getString("ModifiedBy"));
		paymentProcessorMerchant.setTestOrProd(rs.getShort("TestOrProd"));
		paymentProcessorMerchant.setModifiedDate(new DateTime(rs.getTimestamp("DatedModified")));
		paymentProcessorMerchant.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
		paymentProcessorMerchant.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		return paymentProcessorMerchant;
	}
}