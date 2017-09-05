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
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class SaleTransactionDAOImpl implements SaleTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SaleTransactionDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<SaleTransaction> findAll() {
		List<SaleTransaction> list = jdbcTemplate.query(Queries.FINDALLSALETRANSACTIONS,
				new SaleTransactionRowMapper());
		LOGGER.debug("Number of records: {}",list.size());
		return list;
	}

	@Override
	public SaleTransaction findByApplicationTransactionId(String transactionId) {
		return findTransaction(Queries.FINDSALETRANSACTIONBYAPPLICATIONTRANSACTIONID,transactionId);
	}

	@Override
	public SaleTransaction findByProcessorTransactionId(String transactionId) {
		return findTransaction(Queries.FINDSALETRANSACTIONBYPROCESSORTRANSACTIONID,transactionId);
	}

	@Override
	public List<SaleTransaction> findByBatchUploadId(Long batchUploadId) {
		ArrayList<SaleTransaction> list = (ArrayList<SaleTransaction>) jdbcTemplate.query(
				Queries.FINDSALETRANSACTIONBYBATCHUPLOADID, new Object[] { batchUploadId },
				new RowMapperResultSetExtractor<SaleTransaction>(new SaleTransactionRowMapper()));
		LOGGER.debug("Number of batch uploads: {}", list.size());
		return list;
	}
	
	public SaleTransaction findTransaction(String queryToExecute,String transactionId){
		ArrayList<SaleTransaction> list = (ArrayList<SaleTransaction>) jdbcTemplate.query(
				queryToExecute, new Object[] { transactionId },
				new RowMapperResultSetExtractor<SaleTransaction>(new SaleTransactionRowMapper()));
		LOGGER.debug("Number of transactions: {}", list.size());
		SaleTransaction saleTransaction = DataAccessUtils.singleResult(list);

		if (saleTransaction != null) {
			LOGGER.debug("Record found for transactionId: {}", transactionId);
		} else {
			LOGGER.debug("Record not found for transactionId: {} ", transactionId);
		}

		return saleTransaction;
	}
}

class SaleTransactionRowMapper implements RowMapper<SaleTransaction> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SaleTransactionRowMapper.class);
	@Override
	public SaleTransaction mapRow(ResultSet rs, int row) throws SQLException {
		SaleTransaction saleTransaction = new SaleTransaction();
		saleTransaction.setSaleTransactionId(rs.getLong("SaleTransactionID"));
		saleTransaction.setFirstName(rs.getString("FirstName"));
		saleTransaction.setLastName(rs.getString("LastName"));
		saleTransaction.setProcessUser(rs.getString("ProcessUser"));
		saleTransaction.setTransactionType(rs.getString("TransactionType"));
		saleTransaction.setAddress1(rs.getString("Address1"));
		saleTransaction.setAddress2(rs.getString("Address2"));
		saleTransaction.setCity(rs.getString("City"));
		saleTransaction.setState(rs.getString("State"));
		saleTransaction.setPostalCode(rs.getString("PostalCode"));
		saleTransaction.setCountry(rs.getString("Country"));
		saleTransaction.setCardNumberFirst6Char(rs.getString("CardNumberFirst6Char"));
		saleTransaction.setCardNumberLast4Char("XXXX-XXXX-XXXX-"+rs.getString("CardNumberLast4Char"));
		saleTransaction.setCardType(rs.getString("CardType"));
		try {
			saleTransaction.setExpiryDate(rs.getTimestamp("ExpiryDate"));
		} catch (SQLException sqlEcp){
			LOGGER.debug("Invalid value found for expiry date , App Transaction Id= {} , Exp Message={}",saleTransaction.getApplicationTransactionId(),sqlEcp.getMessage(),sqlEcp);
		}
		saleTransaction.setToken(rs.getString("Token"));
		saleTransaction.setChargeAmount(rs.getBigDecimal("ChargeAmount"));
		saleTransaction.setLegalEntityApp(rs.getString("LegalEntityApp"));
		saleTransaction.setAccountId(rs.getString("AccountId"));
		saleTransaction.setApplicationTransactionId(rs.getString("ApplicationTransactionID"));
		saleTransaction.setMerchantId(rs.getString("MerchantID"));
		saleTransaction.setProcessor(rs.getString("Processor"));
		saleTransaction.setApplication(rs.getString("Application"));
		saleTransaction.setOrigin(rs.getString("Origin"));
		saleTransaction.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
		Timestamp ts;
		if(rs.getString("TransactionDateTime") != null) {
			ts = Timestamp.valueOf(rs.getString("TransactionDateTime"));
			saleTransaction.setTransactionDateTime(new DateTime(ts));
		}
		saleTransaction.setTestMode(rs.getShort("TestMode"));
		saleTransaction.setApprovalCode(rs.getString("ApprovalCode"));
		saleTransaction.setTokenized(rs.getShort("Tokenized"));
		saleTransaction.setPaymentProcessorStatusCode(rs.getString("PaymentProcessorStatusCode"));
		saleTransaction.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusCodeDescription"));
		saleTransaction.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
		saleTransaction
				.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		saleTransaction.setInternalStatusCode(rs.getString("InternalStatusCode"));
		saleTransaction.setInternalStatusDescription(rs.getString("InternalStatusDescription"));
		saleTransaction.setInternalResponseCode(rs.getString("InternalResponseCode"));
		saleTransaction.setInternalResponseDescription(rs.getString("InternalResponseDescription"));
		saleTransaction.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
		saleTransaction.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
		if(rs.getString("DateCreated") != null) {
			ts = Timestamp.valueOf(rs.getString("DateCreated"));
			saleTransaction.setDateCreated(new DateTime(ts));
		}
		saleTransaction.setPaymentProcessorRuleId(rs.getLong("PaymentProcessorRuleID"));
		saleTransaction.setRulePaymentProcessorId(rs.getLong("RulePaymentProcessorID"));
		saleTransaction.setRuleCardType(rs.getString("RuleCardType"));
		saleTransaction.setRuleMaximumMonthlyAmount(rs.getBigDecimal("RuleMaximumMonthlyAmount"));
		saleTransaction.setRuleNoMaximumMonthlyAmountFlag(rs.getShort("RuleNoMaximumMonthlyAmountFlag"));
		saleTransaction.setRulePriority(rs.getShort("RulePriority"));
		saleTransaction.setAccountPeriod(rs.getString("AccountPeriod"));
		saleTransaction.setDesk(rs.getString("Desk"));
		saleTransaction.setInvoiceNumber(rs.getString("InvoiceNumber"));
		saleTransaction.setUserDefinedField1(rs.getString("UserDefinedField1"));
		saleTransaction.setUserDefinedField2(rs.getString("UserDefinedField2"));
		saleTransaction.setUserDefinedField3(rs.getString("UserDefinedField3"));
		saleTransaction.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
		if(rs.getString("ReconciliationDate") != null) {
			ts = Timestamp.valueOf(rs.getString("ReconciliationDate"));
			saleTransaction.setReconciliationDate(new DateTime(ts));
		}
		saleTransaction.setBatchUploadId(rs.getLong("BatchUploadID"));
		saleTransaction.setEtlRunId(rs.getLong("ETL_RUNID"));

		return saleTransaction;
	}
}