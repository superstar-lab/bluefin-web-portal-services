package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorInternalResponseCodeDAOImpl implements PaymentProcessorInternalResponseCodeDAO {
	private final Logger logger = LoggerFactory.getLogger(PaymentProcessorInternalResponseCodeDAOImpl.class);
	
	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodeId(
			long internalResponseCodeId) {
		ArrayList<PaymentProcessorInternalResponseCode> list = (ArrayList<PaymentProcessorInternalResponseCode>) jdbcTemplate.query(
				Queries.PAYMENTPROCESSORINTERNALRESPONSECODEID, new Object[] { internalResponseCodeId}, new PaymentProcessorInternalResponseCodeRowMapper());
		logger.debug("Number of rows ={} ",list.size());

		return list;
	}
	
	
	public DateTime getItemDate(String date, String pattern) {
		try {
			return DateTimeFormat.forPattern(pattern).parseDateTime(date).withZone(DateTimeZone.UTC);
		} catch (Exception e) {
			if ( logger.isDebugEnabled() ) {
        		logger.debug("Failed to convert item date",e);
        	}
			return null;
		}
	}
	
	@Override
	public void createPaymentProcessorInternalStatusCode(
			Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		insertBatch(new ArrayList<PaymentProcessorInternalResponseCode>(paymentProcessorInternalResponseCodes));
	}
	
	private void insertBatch(final List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes){
		jdbcTemplate.batchUpdate(Queries.SAVEPAYMENTPROCESSORINTERNALRESPONSECODE, new PaymentProcessorInternalResponseCodeInsertBatch(paymentProcessorInternalResponseCodes));
	}
	
	@Override
	public List<PaymentProcessorInternalResponseCode> findPaymentProcessorInternalResponseCodeListByInternalResponseCodeId(Long internalResponseCode) {
		List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = jdbcTemplate.query(Queries.FINDALLPAYMENTPROCESSORINTERNALRESPONSECODEBYINTERNALRESPCODEID, new Object[]{internalResponseCode},
				new PaymentProcessorInternalResponseCodeRowMapper());
		logger.debug("size ={} ",paymentProcessorInternalResponseCodes.size());
		return paymentProcessorInternalResponseCodes;
	}
	
	@Override
	public void deleteByInternalResponseCode(Long internalResponseCode) {
		int rows = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORINTERNALRESPONSECODE, new Object[] {internalResponseCode});
		logger.info("Number of childs items deleted = {} for internalResponseCodeId={}",rows,internalResponseCode);
	}
	
	@Override
	public List<Long> findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode(Long internalResponseCode) {
		return jdbcTemplate.queryForList(Queries.FINDPAYMENTPROCESSORRESPONSECODEIDSBYINTERNALRESPONSECODE, new Object[]{internalResponseCode},
				Long.class);
	}
	
	@Override
	public void deletePaymentProcessorResponseCodeIds(List<Long> ids ){
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", ids);
		executeQueryToDeleteRecords(Queries.DELETEPAYMENTPROCESSORRESPONSECODEIDS,valuesToDelete);
	}
	
	private void executeQueryToDeleteRecords(String deleteQuery,Map<String, List<Long>> idsToDelete){
		logger.debug("Finally deleteing records, Query= {}", deleteQuery );
		logger.debug("idsToDelete={}",idsToDelete);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		int noOfRowsDeleted = namedJDBCTemplate.update(deleteQuery,idsToDelete);
		logger.debug("Number of rows deleted={}",noOfRowsDeleted);
	}
	
	@Override
	public void delete(Long paymentProcessorInternalResponseCodeId) {
		logger.debug("Deleting child items for internalResponseCodeId ={}",paymentProcessorInternalResponseCodeId);
		int noOfRowsDeleted = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORINTERNALRESPONSECODE, paymentProcessorInternalResponseCodeId);
		logger.debug("Number of childs items deleted {} internalStatusCodeId {}",noOfRowsDeleted,paymentProcessorInternalResponseCodeId);		
	}
	
	@Override
	public void savePaymentProcessorInternalResponseCodes(
			Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		insertBatch(new ArrayList<PaymentProcessorInternalResponseCode>(paymentProcessorInternalResponseCodes));
	}
	
	@Override
	public void deletePaymentProcessorInternalResponseCodeForPaymentProcessor(Long paymentProcessorId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Delete Payment processr status code for paymentprocessorid= {}",paymentProcessorId);
		}
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = fetchInternalResponseCodeIdsUsedForPaymentProcessor(paymentProcessorId);
		logger.debug("Number of Internal Status Code Ids= {} , for paymentprocessid= {}",idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.size(),paymentProcessorId );
		Set<Entry<Long,List<Long>>> allEntries = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.entrySet();
		List<Long> paymentProcessorInternalStatusCodeIds = new ArrayList<>();
		List<Long> internalStatusCodeIds = new ArrayList<>();
			
		if (allEntries != null) {
			for (Entry<Long,List<Long>> entry : allEntries ) {
				internalStatusCodeIds.add(entry.getKey());
				paymentProcessorInternalStatusCodeIds.addAll(entry.getValue());
			}
		}
		if (!paymentProcessorInternalStatusCodeIds.isEmpty()) {
			deletePaymentProcessorInternalResponseCodeIds(paymentProcessorInternalStatusCodeIds);
			logger.debug("PaymentProcessorInternalStatusCodeIds deletion completed");
		}
		if (!internalStatusCodeIds.isEmpty()) {
			deleteInternalResponseCodeIds(internalStatusCodeIds);
			logger.debug("InternalStatusCodeIds deletion completed");
		}
	}
	
	@Override
	public void deleteInternalResponseCodeIds(List<Long> internalStatusCodeIds) {
		logger.debug("Delete Internal Status Code_IDs={}",internalStatusCodeIds);
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", internalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.DELETEINTERNALRESPONSECODES,valuesToDelete);
	}
	
	private Map<Long,List<Long>> fetchInternalResponseCodeIdsUsedForPaymentProcessor(Long paymentProcessId){
		logger.debug("Fetching Internal Response Code Ids for paymentprocessorid={}",paymentProcessId);
		String query = " select InternalResponseCodeId,PaymentProcessorInternalResponseCodeID from PaymentProcessor_InternalResponseCode " +
				" where PaymentProcessorResponseCodeID in ( " +
			" select PaymentProcessorResponseCodeID from PaymentProcessorResponseCode_Lookup " +
			" where PaymentProcessorID = ? ) ";
		Map<Long,List<Long>> idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode = new HashMap<>();
		jdbcTemplate.query(query, new Object[]{paymentProcessId}, rs->{
				Long internalStatusCodeId;
				Long paymentProcessorInternalStatusId;
				while(rs.next()){
					internalStatusCodeId = rs.getLong("InternalStatusCodeId");
					paymentProcessorInternalStatusId = rs.getLong("PaymentProcessorInternalStatusCodeID");
					List<Long> paymentProcessorInternalStatusCodeIds = idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode.get(internalStatusCodeId);
					if (paymentProcessorInternalStatusCodeIds == null) {
						paymentProcessorInternalStatusCodeIds = new ArrayList<>();
						idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode.put(internalStatusCodeId,paymentProcessorInternalStatusCodeIds);
					}
					paymentProcessorInternalStatusCodeIds.add(paymentProcessorInternalStatusId);
				}
				return idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode;
		});
		return idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode;
	}

	@Override
	public void deletePaymentProcessorInternalResponseCodeIds(List<Long> paymentProcessorInternalStatusCodeIds) {
			logger.debug("Delete Payment Processor Internal Status Code_IDs={}", paymentProcessorInternalStatusCodeIds);
			Map<String, List<Long>> valuesToDelete = new HashMap<>();
			valuesToDelete.put("ids", paymentProcessorInternalStatusCodeIds);
			executeQueryToDeleteRecords(Queries.DELETEPAYMENTPROCESSORINTERNALRESPONSECODES,valuesToDelete);
		
	}
	
	@Override
	public Set<Long> fetchInternalResponseCodeIdsMappedForPaymentProcessorResponseCodeIds(List<Long> paymentProcessorResponseCodeIds){
		if (paymentProcessorResponseCodeIds != null && !paymentProcessorResponseCodeIds.isEmpty()) {
			Map<String, List<Long>> valuesToCheck = new HashMap<>();
			valuesToCheck.put("ids", paymentProcessorResponseCodeIds);
			NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
			List<Long> idsFetched = namedJDBCTemplate.queryForList(Queries.INTERNALRESPONSECODEIDSMAPPEDWITHPAYMENTPROCESSORRESPONSECODEIDS,valuesToCheck,Long.class);
			if (idsFetched != null && !idsFetched.isEmpty()) {
				// Need to return only unique ids
				Set<Long> idsFetchedAndReturnedUnique = new HashSet<>();
				idsFetchedAndReturnedUnique.addAll(idsFetched);
				return idsFetchedAndReturnedUnique;
			}
		}
		return Collections.emptySet();
	}
}

class PaymentProcessorInternalResponseCodeRowMapper implements RowMapper<PaymentProcessorInternalResponseCode> {
	@Override
	public PaymentProcessorInternalResponseCode mapRow(ResultSet rs, int row) throws SQLException {
		PaymentProcessorInternalResponseCode paymentProcessor = new PaymentProcessorInternalResponseCode();
		paymentProcessor.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
		PaymentProcessorResponseCode paymentProcessorResponseCode = new PaymentProcessorResponseCode();
		
		paymentProcessorResponseCode.setPaymentProcessorResponseCodeId(rs.getLong("PaymentProcessorResponseCodeID"));
		paymentProcessor.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
		paymentProcessor.setInternalResponseCodeId(rs.getLong("InternalResponseCodeID"));
		paymentProcessor.setLastModifiedBy(rs.getString("ModifiedBy"));
		paymentProcessor.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
		
		return paymentProcessor;
	}

}

class PaymentProcessorInternalResponseCodeInsertBatch implements BatchPreparedStatementSetter {
	final List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes;
	private static final DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
	public PaymentProcessorInternalResponseCodeInsertBatch(List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes){
		this.paymentProcessorInternalResponseCodes = paymentProcessorInternalResponseCodes;
	}
	@Override
	public void setValues(PreparedStatement ps, int i) throws SQLException {
		PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = paymentProcessorInternalResponseCodes.get(i);
		DateTime utc1 = paymentProcessorInternalResponseCode.getCreatedDate() != null ? paymentProcessorInternalResponseCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		if (paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode() != null && paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId()!= null) {
			ps.setLong(1, paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
		} else {
			ps.setLong(1, paymentProcessorInternalResponseCode.getPaymentProcessorResponseCodeId());
		}
		if (paymentProcessorInternalResponseCode.getInternalResponseCode() != null && paymentProcessorInternalResponseCode.getInternalResponseCode().getInternalResponseCodeId()!= null) {
			ps.setLong(2, paymentProcessorInternalResponseCode.getInternalResponseCode().getInternalResponseCodeId());
		} else {
			ps.setLong(2, paymentProcessorInternalResponseCode.getInternalResponseCodeId());
		}
		ps.setLong(2, paymentProcessorInternalResponseCode.getInternalResponseCode().getInternalResponseCodeId());
		ps.setTimestamp(3, dateCreated);
	}

	@Override
	public int getBatchSize() {
		return paymentProcessorInternalResponseCodes.size();
	} 
} 
