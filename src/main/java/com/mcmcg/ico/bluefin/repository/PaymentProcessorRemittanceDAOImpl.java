package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepositoryCustom;
import com.mcmcg.ico.bluefin.repository.sql.Queries;
import com.mcmcg.ico.bluefin.service.util.QueryUtil;

@Repository
public class PaymentProcessorRemittanceDAOImpl implements PaymentProcessorRemittanceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*@Autowired
	TransactionRepositoryCustom transactionRepositoryCustom;*/

	@Override
	public PaymentProcessorRemittance findByProcessorTransactionId(String transactionId) {
		PaymentProcessorRemittance paymentProcessorRemittance = null;

		ArrayList<PaymentProcessorRemittance> list = (ArrayList<PaymentProcessorRemittance>) jdbcTemplate.query(
				Queries.findPaymentProcessorRemittanceByProcessorTransactionId, new Object[] { transactionId },
				new RowMapperResultSetExtractor<PaymentProcessorRemittance>(new PaymentProcessorRemittanceRowMapper()));
		paymentProcessorRemittance = DataAccessUtils.singleResult(list);

		if (paymentProcessorRemittance != null) {
			LOGGER.debug("Found PaymentProcessorRemittance for transactionId: " + transactionId);
		} else {
			LOGGER.debug("PaymentProcessorRemittance not found for transactionId: " + transactionId);
		}

		return paymentProcessorRemittance;
	}

	@Override
	public Page<RemittanceSale> findRemittanceSaleRefundTransactions(String search, PageRequest pageRequest,
			boolean negate) throws ParseException {

		// Parameters to set in query with examples:
		// remittanceCreationDate1 - 2016-10-20 00:00:00
		// remittanceCreationDate2 - 2016-10-20 23:59:59
		// processorNameParam - PAYSCOUT
		// merchantIdParam - [mcmilms]
		// reconciliationStatusIdParam - 1

		Map<String, String> parameterMap = QueryUtil.convertSearchToMap(search);
		String sql = Queries.remittanceSaleTransaction;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("remittanceCreationDateParam1", parameterMap.get("remittanceCreationDateStart"));
		parameters.put("remittanceCreationDateParam2", parameterMap.get("remittanceCreationDateEnd"));
		parameters.put("processorNameParam", parameterMap.get("processorName"));
		parameters.put("merchantIdParam", parameterMap.get("merchantId"));
		parameters.put("reconciliationStatusIdParam", Long.valueOf(parameterMap.get("reconciliationStatusId")));

		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			LOGGER.debug("parameter: " + entry.getKey() + ", value: " + entry.getValue());
		}

		List<RemittanceSale> list = (List<RemittanceSale>) jdbcTemplate.query(sql,
				new PaymentProcessorRemittanceExtractor(), parameters);

		LOGGER.debug("Number of rows: " + list.size());

		int countResult = list.size();
		int pageNumber = pageRequest.getPageNumber();
		int pageSize = pageRequest.getPageSize();

		List<RemittanceSale> onePage = new ArrayList<RemittanceSale>();
		int index = pageSize * pageNumber;
		int increment = pageSize;
		// Check upper bound to avoid IndexOutOfBoundsException
		if ((index + increment) > countResult) {
			int adjustment = (index + increment) - countResult;
			increment -= adjustment;
		}
		for (int i = index; i < (index + increment); i++) {
			onePage.add(list.get(i));
		}

		Page<RemittanceSale> pageList = new PageImpl<RemittanceSale>(onePage, pageRequest, countResult);

		return pageList;
	}

	@Override
	public List<RemittanceSale> findRemittanceSaleRefundTransactionsReport(String search) throws ParseException {

		// Parameters to set in query with examples:
		// remittanceCreationDate1 - 2016-10-20 00:00:00
		// remittanceCreationDate2 - 2016-10-20 23:59:59
		// processorNameParam - PAYSCOUT
		// merchantIdParam - [mcmilms]
		// reconciliationStatusIdParam - 1

		Map<String, String> parameterMap = QueryUtil.convertSearchToMap(search);
		String sql = Queries.remittanceSaleTransaction;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("remittanceCreationDateParam1", parameterMap.get("remittanceCreationDateStart"));
		parameters.put("remittanceCreationDateParam2", parameterMap.get("remittanceCreationDateEnd"));
		parameters.put("processorNameParam", parameterMap.get("processorName"));
		parameters.put("merchantIdParam", parameterMap.get("merchantId"));
		parameters.put("reconciliationStatusIdParam", Long.valueOf(parameterMap.get("reconciliationStatusId")));

		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			LOGGER.debug("parameter: " + entry.getKey() + ", value: " + entry.getValue());
		}

		List<RemittanceSale> list = (List<RemittanceSale>) jdbcTemplate.query(sql,
				new PaymentProcessorRemittanceExtractor(), parameters);

		LOGGER.debug("Number of rows: " + list.size());

		return list;
	}

	@Override
	public PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) throws ParseException {
		/*PaymentProcessorRemittance paymentProcessorRemittance = transactionRepositoryCustom.
				findRemittanceSaleRefundTransactionsDetail(transactionId, transactionType, processorTransactionType);
		return paymentProcessorRemittance;*/
		return null;
	}
}

class PaymentProcessorRemittanceRowMapper implements RowMapper<PaymentProcessorRemittance> {

	@Override
	public PaymentProcessorRemittance mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance();
		paymentProcessorRemittance.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
		paymentProcessorRemittance.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
		paymentProcessorRemittance.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
		paymentProcessorRemittance.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
		paymentProcessorRemittance.setPaymentMethod(rs.getString("PaymentMethod"));
		paymentProcessorRemittance.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
		paymentProcessorRemittance.setTransactionType(rs.getString("TransactionType"));
		paymentProcessorRemittance.setTransactionTime(new DateTime(rs.getTimestamp("TransactionTime")));
		paymentProcessorRemittance.setAccountId(rs.getString("AccountID"));
		paymentProcessorRemittance.setApplication(rs.getString("Application"));
		paymentProcessorRemittance.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
		paymentProcessorRemittance.setMerchantId(rs.getString("MerchantID"));
		paymentProcessorRemittance.setTransactionSource(rs.getString("TransactionSource"));
		paymentProcessorRemittance.setFirstName(rs.getString("FirstName"));
		paymentProcessorRemittance.setLastName(rs.getString("LastName"));
		paymentProcessorRemittance.setRemittanceCreationDate(new DateTime(rs.getTimestamp("RemittanceCreationDate")));
		paymentProcessorRemittance.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
		paymentProcessorRemittance.setReProcessStatus(rs.getString("ReProcessStatus"));
		paymentProcessorRemittance.setEtlRunId(rs.getLong("ETL_RUNID"));

		return paymentProcessorRemittance;
	}
}

class PaymentProcessorRemittanceExtractor implements ResultSetExtractor<List<RemittanceSale>> {

	@Override
	public List<RemittanceSale> extractData(ResultSet rs) throws SQLException, DataAccessException {

		ArrayList<RemittanceSale> list = new ArrayList<RemittanceSale>();

		while (rs.next()) {

			RemittanceSale remittanceSale = new RemittanceSale();

			PaymentProcessorRemittance ppr = new PaymentProcessorRemittance();
			ppr.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
			ppr.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
			// Mitul overrides this with ReconciliationStatus_ID
			ppr.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
			ppr.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
			ppr.setPaymentMethod(rs.getString("PaymentMethod"));
			ppr.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
			ppr.setTransactionType(rs.getString("TransactionType"));
			ppr.setTransactionTime(new DateTime(rs.getTimestamp("TransactionTime")));
			ppr.setAccountId(rs.getString("AccountID"));
			ppr.setApplication(rs.getString("Application"));
			ppr.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
			// Mitul overrides this with MID
			ppr.setMerchantId(rs.getString("MerchantID"));
			ppr.setTransactionSource(rs.getString("TransactionSource"));
			ppr.setFirstName(rs.getString("FirstName"));
			ppr.setLastName(rs.getString("LastName"));
			ppr.setRemittanceCreationDate(new DateTime(rs.getTimestamp("RemittanceCreationDate")));
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