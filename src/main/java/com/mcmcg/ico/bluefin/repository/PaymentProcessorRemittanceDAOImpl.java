package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorRemittanceDAOImpl implements PaymentProcessorRemittanceDAO {

	private static final Logger logger = LoggerFactory.getLogger(PaymentProcessorRemittanceDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	CustomSaleTransactionDAO customSaleTransactionDAO;

	@Override
	public PaymentProcessorRemittance findByProcessorTransactionId(String transactionId) {
		PaymentProcessorRemittance paymentProcessorRemittance;

		ArrayList<PaymentProcessorRemittance> list = (ArrayList<PaymentProcessorRemittance>) jdbcTemplate.query(
				Queries.FINDPAYMENTPROCESSORREMITTANCEBYPROCESSORTRANSACTIONID, new Object[] { transactionId },
				new RowMapperResultSetExtractor<PaymentProcessorRemittance>(new PaymentProcessorRemittanceRowMapper()));
		logger.debug("paymentProcessorRemittance size ={} ",list.size());
		paymentProcessorRemittance = DataAccessUtils.singleResult(list);

		if (paymentProcessorRemittance != null) {
			logger.debug("Found PaymentProcessorRemittance for transactionId ={} ", transactionId);
		} else {
			logger.debug("PaymentProcessorRemittance not found for transactionId={} ", transactionId);
		}

		return paymentProcessorRemittance;
	}
	
}

class PaymentProcessorRemittanceRowMapper implements RowMapper<PaymentProcessorRemittance> {

	@Override
	public PaymentProcessorRemittance mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance();
		paymentProcessorRemittance.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
		Timestamp ts;
		if (rs.getString(BluefinWebPortalConstants.DATECREATED) != null){
			ts = Timestamp.valueOf(rs.getString(BluefinWebPortalConstants.DATECREATED));
			paymentProcessorRemittance.setDateCreated(new DateTime(ts));
		}
		paymentProcessorRemittance.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
		paymentProcessorRemittance.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
		paymentProcessorRemittance.setPaymentMethod(rs.getString("PaymentMethod"));
		paymentProcessorRemittance.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
		paymentProcessorRemittance.setTransactionType(rs.getString("TransactionType"));
		if (rs.getString(BluefinWebPortalConstants.TRANSACTIONTIME) != null) {
			ts = Timestamp.valueOf(rs.getString(BluefinWebPortalConstants.TRANSACTIONTIME));
			paymentProcessorRemittance.setTransactionTime(new DateTime(ts));
		}
		paymentProcessorRemittance.setAccountId(rs.getString("AccountID"));
		paymentProcessorRemittance.setApplication(rs.getString("Application"));
		paymentProcessorRemittance.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
		paymentProcessorRemittance.setMerchantId(rs.getString("MerchantID"));
		paymentProcessorRemittance.setTransactionSource(rs.getString("TransactionSource"));
		paymentProcessorRemittance.setFirstName(rs.getString("FirstName"));
		paymentProcessorRemittance.setLastName(rs.getString("LastName"));
		paymentProcessorRemittance.setRemittanceCreationDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.REMITTANCECREATIONDATE)));
		paymentProcessorRemittance.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorRemittance.setReProcessStatus(rs.getString("ReProcessStatus"));
		paymentProcessorRemittance.setEtlRunId(rs.getLong("ETL_RUNID"));

		return paymentProcessorRemittance;
	}
}

class PaymentProcessorRemittanceExtractor implements ResultSetExtractor<List<RemittanceSale>> {

	@Override
	public List<RemittanceSale> extractData(ResultSet rs) throws SQLException {

		ArrayList<RemittanceSale> list = new ArrayList<>();

		while (rs.next()) {

			RemittanceSale remittanceSale = new RemittanceSale();

			PaymentProcessorRemittance ppr = new PaymentProcessorRemittance();
			ppr.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
			Timestamp ts;
			if (rs.getString(BluefinWebPortalConstants.DATECREATED) != null){
				ts = Timestamp.valueOf(rs.getString(BluefinWebPortalConstants.DATECREATED));
				ppr.setDateCreated(new DateTime(ts));
			}
			// Mitul overrides this with ReconciliationStatus_ID
			ppr.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
			ppr.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
			ppr.setPaymentMethod(rs.getString("PaymentMethod"));
			ppr.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
			ppr.setTransactionType(rs.getString("TransactionType"));
			ppr.setTransactionTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.TRANSACTIONTIME)));
			ppr.setAccountId(rs.getString("AccountID"));
			ppr.setApplication(rs.getString("Application"));
			ppr.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
			// Mitul overrides this with MID
			ppr.setMerchantId(rs.getString("MerchantID"));
			ppr.setTransactionSource(rs.getString("TransactionSource"));
			ppr.setFirstName(rs.getString("FirstName"));
			ppr.setLastName(rs.getString("LastName"));
			if (rs.getString(BluefinWebPortalConstants.REMITTANCECREATIONDATE) != null){
				ts = Timestamp.valueOf(rs.getString(BluefinWebPortalConstants.REMITTANCECREATIONDATE));
				ppr.setRemittanceCreationDate(new DateTime(ts));
			}
			ppr.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
			// Final value (ORDER BY)
			ppr.setMerchantId(rs.getString("MID"));
			// Final value (ORDER BY)
			ppr.setReconciliationStatusId(rs.getLong("ReconciliationStatus_ID"));
			remittanceSale.setPaymentProcessorRemittance(ppr);

			SaleTransaction st = new SaleTransaction();
			// Mitul overrides this with Processor_Name
			st.setProcessor(rs.getString("ProcessorName"));
			st.setSaleTransactionId(rs.getLong("SaleTransactionID"));
			st.setFirstName(rs.getString("SaleFirstName"));
			st.setLastName(rs.getString("SaleLastName"));
			st.setProcessUser(rs.getString("SaleProcessUser"));
			st.setTransactionType(rs.getString("SaleTransactionType"));
			st.setAddress1(rs.getString("SaleAddress1"));
			st.setAddress2(rs.getString("SaleAddress2"));
			st.setCity(rs.getString("SaleCity"));
			st.setState(rs.getString("SaleState"));
			st.setPostalCode(rs.getString("SalePostalCode"));
			st.setCountry(rs.getString("SaleCountry"));
			st.setCardNumberFirst6Char(rs.getString("SaleCardNumberFirst6Char"));
			st.setCardNumberLast4Char(rs.getString("SaleCardNumberLast4Char"));
			st.setCardType(rs.getString("SaleCardType"));
			st.setExpiryDate(rs.getTimestamp("SaleExpiryDate"));
			st.setToken(rs.getString("SaleToken"));
			st.setChargeAmount(rs.getBigDecimal("SaleChargeAmount"));
			st.setLegalEntityApp(rs.getString("SaleLegalEntityApp"));
			st.setAccountId(rs.getString("SaleAccountId"));
			st.setApplicationTransactionId(rs.getString("SaleApplicationTransactionID"));
			st.setMerchantId(rs.getString("SaleMerchantID"));
			st.setProcessor(rs.getString("SaleProcessor"));
			st.setApplication(rs.getString("SaleApplication"));
			st.setOrigin(rs.getString("SaleOrigin"));
			st.setProcessorTransactionId(rs.getString("SaleProcessorTransactionID"));
			st.setTransactionDateTime(new DateTime(rs.getTimestamp("SaleTransactionDateTime")));
			st.setTestMode(rs.getShort("SaleTestMode"));
			st.setApprovalCode(rs.getString("SaleApprovalCode"));
			st.setTokenized(rs.getShort("SaleTokenized"));
			st.setPaymentProcessorStatusCode(rs.getString("SalePaymentProcessorStatusCode"));
			st.setPaymentProcessorStatusCodeDescription(rs.getString("SalePaymentProcessorStatusCodeDescription"));
			st.setPaymentProcessorResponseCode(rs.getString("SalePaymentProcessorResponseCode"));
			st.setPaymentProcessorResponseCodeDescription(rs.getString("SalePaymentProcessorResponseCodeDescription"));
			st.setInternalStatusCode(rs.getString("SaleInternalStatusCode"));
			st.setInternalStatusDescription(rs.getString("SaleInternalStatusDescription"));
			st.setInternalResponseCode(rs.getString("SaleInternalResponseCode"));
			st.setInternalResponseDescription(rs.getString("SaleInternalResponseDescription"));
			st.setPaymentProcessorInternalStatusCodeId(rs.getLong("SalePaymentProcessorInternalStatusCodeID"));
			st.setPaymentProcessorInternalResponseCodeId(rs.getLong("SalePaymentProcessorInternalResponseCodeID"));
			st.setDateCreated(new DateTime(rs.getTimestamp("SaleDateCreated")));
			st.setPaymentProcessorRuleId(rs.getLong("SalePaymentProcessorRuleID"));
			st.setRulePaymentProcessorId(rs.getLong("SaleRulePaymentProcessorID"));
			st.setRuleCardType(rs.getString("SaleRuleCardType"));
			st.setRuleMaximumMonthlyAmount(rs.getBigDecimal("SaleRuleMaximumMonthlyAmount"));
			st.setRuleNoMaximumMonthlyAmountFlag(rs.getShort("SaleRuleNoMaximumMonthlyAmountFlag"));
			st.setRulePriority(rs.getShort("SaleRulePriority"));
			st.setAccountPeriod(rs.getString("SaleAccountPeriod"));
			st.setDesk(rs.getString("SaleDesk"));
			st.setInvoiceNumber(rs.getString("SaleInvoiceNumber"));
			st.setUserDefinedField1(rs.getString("SaleUserDefinedField1"));
			st.setUserDefinedField2(rs.getString("SaleUserDefinedField2"));
			st.setUserDefinedField3(rs.getString("SaleUserDefinedField3"));
			st.setReconciliationStatusId(rs.getLong("SaleReconciliationStatusID"));
			st.setReconciliationDate(new DateTime(rs.getTimestamp("SaleReconciliationDate")));
			st.setBatchUploadId(rs.getLong("SaleBatchUploadID"));
			st.setIsVoided(rs.getInt("SaleIsVoided"));
			st.setIsRefunded(rs.getInt("SaleIsRefunded"));
			// Final value (ORDER BY)
			st.setProcessor(rs.getString("Processor_Name"));
			remittanceSale.setSaleTransaction(st);

			list.add(remittanceSale);
		}

		return list;
	}
}