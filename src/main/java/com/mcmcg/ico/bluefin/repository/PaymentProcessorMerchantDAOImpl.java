/**
 * 
 */
package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

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
		List<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant>) jdbcTemplate.query(
				Queries.findPaymentProcessorMerchantsById, new Object[] { paymentProcessorId },
				new RowMapperResultSetExtractor<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant>(new PaymentProcessorMerchantRowMapper()));

		LOGGER.debug("Number of rows: " + list.size());
		return list;
	}

	@Override
	public void deletPaymentProcessorMerchantByProcID(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorMerchantByProcId, new Object[] {paymentProcessorId});

		LOGGER.debug("Deleted Payment Processor Merchant by Payment Processor Id: " + paymentProcessorId + ", rows affected = " + rows);
	}

	@Override
	public void deletePaymentProcessorRules(Long paymentProcessorId) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorMerchants, new Object[] { paymentProcessorId });
		LOGGER.debug("Deleted Payment Processor Merchants for PaymentProcessor Id: " + paymentProcessorId
				+ ", rows affected = " + rows);
	}

}

class PaymentProcessorMerchantRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> {
	@Override
	//	SELECT PaymentProcessorMerchantID, LegalEntityAppID, PaymentProcessorID, 
	//TestOrProd, MerchantID, DateCreated, DatedModified, ModifiedBy FROM PaymentProcessor_Merchant
	public com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant mapRow(ResultSet rs, int row) throws SQLException {
		com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant paymentProcessorMerchant = new com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant();
		paymentProcessorMerchant.setMerchantId(rs.getString("MerchantID"));
		paymentProcessorMerchant.setPaymentProcessorMechantId(rs.getLong("PaymentProcessorMerchantID"));
		paymentProcessorMerchant.setLastModifiedBy(rs.getString("ModifiedBy"));
		paymentProcessorMerchant.setTestOrProd(rs.getShort("TestOrProd"));
		paymentProcessorMerchant.setModifiedDate(new DateTime(rs.getTimestamp("DatedModified")));
		com.mcmcg.ico.bluefin.model.LegalEntityApp legalEntityApp = new com.mcmcg.ico.bluefin.model.LegalEntityApp();
		legalEntityApp.setLegalEntityAppId(rs.getLong("LegalEntityAppID"));
		paymentProcessorMerchant.setLegalEntityApp(legalEntityApp);
		return paymentProcessorMerchant;
	}
}