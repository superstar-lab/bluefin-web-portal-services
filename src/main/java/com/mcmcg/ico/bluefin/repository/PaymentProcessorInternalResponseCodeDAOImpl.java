package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorInternalResponseCodeDAOImpl implements PaymentProcessorInternalResponseCodeDAO {
	private final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorInternalResponseCodeDAOImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodeId(
			long internalResponseCodeId) {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>) jdbcTemplate.query(
				Queries.paymentProcessorInternalResponseCodeId, new Object[] { internalResponseCodeId}, new PaymentProcessorInternalResponseCodeRowMapper());
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: paymentProcessorInternalResponseCodeId() : Number of rows: "+list.size());
		return list;
	}
	
	
	public DateTime getItemDate(String date, String pattern) {
		try {
			return DateTimeFormat.forPattern(pattern).parseDateTime(date).withZone(DateTimeZone.UTC);
		} catch (Exception e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("Failed to convert item date",e);
        	}
			return null;
		}
	}
	
	@Override
	public void createPaymentProcessorInternalStatusCode(
			Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		insertBatch(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>(paymentProcessorInternalResponseCodes));
	}
	
	private void insertBatch(final List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes){
		jdbcTemplate.batchUpdate(Queries.savePaymentProcessorInternalResponseCode, new PaymentProcessorInternalResponseCodeInsertBatch(paymentProcessorInternalResponseCodes));
	}
	
	@Override
	public List<PaymentProcessorInternalResponseCode> findPaymentProcessorInternalResponseCodeListByInternalResponseCodeId(Long internalResponseCode) {
		List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = jdbcTemplate.query(Queries.findAllPaymentProcessorInternalResponseCodeByInternalRespCodeId, new Object[]{internalResponseCode},
				new PaymentProcessorInternalResponseCodeRowMapper());
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: findPaymentProcessorInternalResponseCodeListByInternalResponseCodeId() : size : "+paymentProcessorInternalResponseCodes.size());
		return paymentProcessorInternalResponseCodes;
	}
	
	@Override
	public void deleteByInternalResponseCode(Long internalResponseCode) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorInternalResponseCode, new Object[] {internalResponseCode});
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deleteByInternalResponseCode() :Number of PaymentProcessorInternalResponseCode deleted = "+rows);
	}
	
	@Override
	public List<Long> findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode(Long internalResponseCode) {
		return jdbcTemplate.queryForList(Queries.findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode, new Object[]{internalResponseCode},
				Long.class);
	}
	
	@Override
	public void deletePaymentProcessorResponseCodeIds(List<Long> ids ){
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", ids);
		executeQueryToDeleteRecords(Queries.deletePaymentProcessorResponseCodeIds,valuesToDelete);
	}
	
	private void executeQueryToDeleteRecords(String deleteQuery,Map<String, List<Long>> idsToDelete){
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: executeQueryToDeleteRecords() : Finally deleteing records, Query="+deleteQuery+ " , idsToDelete="+idsToDelete);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		int noOfRowsDeleted = namedJDBCTemplate.update(deleteQuery,idsToDelete);
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: executeQueryToDeleteRecords() : Number of rows deleted="+(noOfRowsDeleted));
	}
	
	@Override
	public void delete(Long paymentProcessorInternalResponseCodeId) {
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: delete() : Deleting child items for internalResponseCodeId {}",paymentProcessorInternalResponseCodeId);
		int noOfRowsDeleted = jdbcTemplate.update(Queries.deletePaymentProcessorInternalResponseCode, paymentProcessorInternalResponseCodeId);
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: delete() : Number of childs items deleted {} internalStatusCodeId {}",noOfRowsDeleted,paymentProcessorInternalResponseCodeId);		
	}
	
	@Override
	public void savePaymentProcessorInternalResponseCodes(
			Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		insertBatch(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>(paymentProcessorInternalResponseCodes));
	}
	
	@Override
	public void deletePaymentProcessorInternalResponseCodeForPaymentProcessor(Long paymentProcessorId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeForPaymentProcessor() : Delete Payment processr status code for paymentprocessorid= {}",paymentProcessorId);
		}
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = fetchInternalResponseCodeIdsUsedForPaymentProcessor(paymentProcessorId);
		LOGGER.debug("deletePaymentProcessorInternalResponseCodeForPaymentProcessor : Number of Internal Status Code Ids= {} , for paymentprocessid= {}",idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.size(),paymentProcessorId );
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
			LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeForPaymentProcessor() : PaymentProcessorInternalStatusCodeIds deletion completed");
		}
		if (!internalStatusCodeIds.isEmpty()) {
			deleteInternalResponseCodeIds(internalStatusCodeIds);
			LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeForPaymentProcessor() : InternalStatusCodeIds deletion completed");
		}
	}
	
	@Override
	public void deleteInternalResponseCodeIds(List<Long> internalStatusCodeIds) {
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deleteInternalResponseCodeIds() : Delete Internal Status Code_IDs="+(internalStatusCodeIds));
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", internalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.deleteInternalResponseCodes,valuesToDelete);
	}
	
	private Map<Long,List<Long>> fetchInternalResponseCodeIdsUsedForPaymentProcessor(Long paymentProcessId){
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: fetchInternalResponseCodeIdsUsedForPaymentProcessor() : Fetching Internal Response Code Ids for paymentprocessorid="+paymentProcessId);
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
			LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeIds() : Delete Payment Processor Internal Status Code_IDs="+(paymentProcessorInternalStatusCodeIds));
			Map<String, List<Long>> valuesToDelete = new HashMap<>();
			valuesToDelete.put("ids", paymentProcessorInternalStatusCodeIds);
			executeQueryToDeleteRecords(Queries.deletePaymentProcessorInternalResponseCodes,valuesToDelete);
		
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
	final List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes;
	private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	public PaymentProcessorInternalResponseCodeInsertBatch(List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes){
		this.paymentProcessorInternalResponseCodes = paymentProcessorInternalResponseCodes;
	}
	@Override
	public void setValues(PreparedStatement ps, int i) throws SQLException {
		com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = paymentProcessorInternalResponseCodes.get(i);
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
