package com.mcmcg.ico.bluefin.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.bindb.service.TransationBinDBDetailsService;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.VoidTransaction;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class VoidTransactionDAOImpl implements VoidTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoidTransactionDAOImpl.class);

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private TransationBinDBDetailsService transationBinDBDetailsService;
	
	@Override
	public VoidTransaction findByApplicationTransactionId(String transactionId) {
		ArrayList<VoidTransaction> list = (ArrayList<VoidTransaction>) jdbcTemplate.query(
				Queries.FINDVOIDTRANSACTIONBYAPPLICATIONTRANSACTIONID, new Object[] { transactionId },
				new RowMapperResultSetExtractor<VoidTransaction>(new VoidTransactionRowMapper()));
		LOGGER.debug("VoidTransaction size ={} ",list.size());
		VoidTransaction voidTransaction = DataAccessUtils.singleResult(list);

		if (voidTransaction != null) {
			LOGGER.debug("Found VoidTransaction for transactionId={} ", transactionId);
			String saleTransactionId = voidTransaction.getSaleTransactionId();
			if (saleTransactionId != null) {
				saleTransactionId = saleTransactionId.trim();
				if (saleTransactionId.length() > 0) {
					voidTransaction = findByApplicationTransactionIdInSale(saleTransactionId, voidTransaction);
				}
			} else {
				LOGGER.debug("SaleTransactionId found invalid");
			}
		} else {
			LOGGER.debug("VoidTransaction not found for transactionId ={} ", transactionId);
		}

		return voidTransaction;
	}

	public VoidTransaction findByApplicationTransactionIdInSale(String saleTransactionId, VoidTransaction voidTransaction) {
		ArrayList<SaleTransaction> saleTransactions = (ArrayList<SaleTransaction>) jdbcTemplate.query(
				Queries.FINDSALETRANSACTIONBYSALETRANSACTIONID, new Object[] { saleTransactionId },
				new RowMapperResultSetExtractor<SaleTransaction>(new SaleTransactionRowMapper()));
		LOGGER.debug("Number of Sale transactions: {}", saleTransactions.size());
		SaleTransaction saleTransaction = DataAccessUtils.singleResult(saleTransactions);
		if (saleTransaction != null) {
			LOGGER.debug("Record found for sale transactionId: {}", saleTransactionId);
			saleTransaction.setBinDBDetails(transationBinDBDetailsService.fetchBinDBDetail(saleTransaction.getCardNumberFirst6Char()));
			voidTransaction.setBinDBDetails(saleTransaction.getBinDBDetails());
			voidTransaction.setSaleTransaction(saleTransaction);
		} else {
			LOGGER.debug("Record not found for transactionId: {} ", saleTransactionId);
		}
		
		return voidTransaction;
	}
}

class VoidTransactionRowMapper implements RowMapper<VoidTransaction> {

	@Override
	public VoidTransaction mapRow(ResultSet rs, int row) throws SQLException {
		VoidTransaction voidTransaction = new VoidTransaction();
		voidTransaction.setVoidTransactionId(rs.getLong("VoidTransactionID"));
		voidTransaction.setSaleTransactionId(rs.getString("SaleTransactionID"));
		voidTransaction.setApprovalCode(rs.getString("ApprovalCode"));
		voidTransaction.setProcessor(rs.getString("Processor"));
		voidTransaction.setMerchantId(rs.getString("merchantID"));
		voidTransaction.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
		voidTransaction.setTransactionDateTime(new DateTime(rs.getTimestamp("TransactionDateTime")));
		voidTransaction.setApplicationTransactionId(rs.getString("ApplicationTransactionID"));
		voidTransaction.setApplication(rs.getString("Application"));
		voidTransaction.setProcessUser(rs.getString("pUser"));
		voidTransaction.setOriginalSaleTransactionId(rs.getString("OriginalSaleTransactionID"));
		voidTransaction.setPaymentProcessorStatusCode(rs.getString("PaymentProcessorStatusCode"));
		voidTransaction.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusCodeDescription"));
		voidTransaction.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
		voidTransaction
				.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
		voidTransaction.setInternalStatusCode(rs.getString("InternalStatusCode"));
		voidTransaction.setInternalStatusDescription(rs.getString("InternalStatusDescription"));
		voidTransaction.setInternalResponseCode(rs.getString("InternalResponseCode"));
		voidTransaction.setInternalResponseDescription(rs.getString("InternalResponseDescription"));
		voidTransaction.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
		voidTransaction.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
		Timestamp ts = Timestamp.valueOf(rs.getString("DateCreated"));
		voidTransaction.setDateCreated(new DateTime(ts));
		voidTransaction.setTransactionType(rs.getString("TransactionType"));
		return voidTransaction;
	}
}