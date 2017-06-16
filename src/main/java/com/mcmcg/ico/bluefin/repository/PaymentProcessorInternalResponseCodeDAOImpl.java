package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorInternalResponseCodeDAOImpl implements PaymentProcessorInternalResponseCodeDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorInternalResponseCodeDAOImpl.class);
	private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
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
	
	
	public static DateTime getItemDate(String date, String pattern) {
		try {
			return DateTimeFormat.forPattern(pattern).parseDateTime(date).withZone(DateTimeZone.UTC);
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void createPaymentProcessorInternalStatusCode(
			Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		insertBatch(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>(paymentProcessorInternalResponseCodes));
	}
	
	private void insertBatch(final List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes){
		jdbcTemplate.batchUpdate(Queries.savePaymentProcessorInternalResponseCode, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = paymentProcessorInternalResponseCodes.get(i);
				DateTime utc1 = paymentProcessorInternalResponseCode.getCreatedDate() != null ? paymentProcessorInternalResponseCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
				LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: insertBatch() : Creating child item , InternalResponseCodeId="+(paymentProcessorInternalResponseCode.getPaymentProcessorInternalResponseCodeId()));//+ " , PaymentProcessorStatusCodeId="+paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
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
		  });
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
		List<Long> paymentProcessors = jdbcTemplate.queryForList(Queries.findPaymentProcessorInternalResponseCodeIdsByInternalResponseCode, new Object[]{internalResponseCode},
				Long.class);
		return paymentProcessors;
	}
	
	@Override
	public void deletePaymentProcessorResponseCodeIds(List<Long> ids ){
		Map<String, List<Long>> valuesToDelete = new HashMap<String,List<Long>>();
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
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeForPaymentProcessor() : Delete Payment processr status code for paymentprocessorid="+paymentProcessorId);
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = fetchInternalResponseCodeIdsUsedForPaymentProcessor(paymentProcessorId);
		LOGGER.debug("deletePaymentProcessorInternalResponseCodeForPaymentProcessor : Number of Internal Status Code Ids="+ ( idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.size() ) + " for paymentprocessid="+paymentProcessorId );
		boolean idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCodeFetched = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode != null ? true : false;
		if (idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCodeFetched) {
			Set<Entry<Long,List<Long>>> allEntries = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.entrySet();
			List<Long> paymentProcessorInternalStatusCodeIds = new ArrayList<Long>();
			List<Long> internalStatusCodeIds = new ArrayList<Long>();
			
			if (allEntries != null) {
				for (Entry<Long,List<Long>> entry : allEntries ) {
					internalStatusCodeIds.add(entry.getKey());
					paymentProcessorInternalStatusCodeIds.addAll(entry.getValue());
				}
			}
			if (!paymentProcessorInternalStatusCodeIds.isEmpty()) {
				deletePaymentProcessorInternalResponseCodeIds(paymentProcessorInternalStatusCodeIds);//;;;(paymentProcessorInternalStatusCodeIds);
				LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeForPaymentProcessor() : PaymentProcessorInternalStatusCodeIds deletion completed");
			}
			if (!internalStatusCodeIds.isEmpty()) {
				deleteInternalResponseCodeIds(internalStatusCodeIds);
				LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeForPaymentProcessor() : InternalStatusCodeIds deletion completed");
			}
		} else {
			LOGGER.debug("Payment processor internal response code Ids found zero for payment processor id="+paymentProcessorId);
		}
	}
	
	@Override
	public void deleteInternalResponseCodeIds(List<Long> internalStatusCodeIds) {
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deleteInternalResponseCodeIds() : Delete Internal Status Code_IDs="+(internalStatusCodeIds));
		Map<String, List<Long>> valuesToDelete = new HashMap<String,List<Long>>();
		valuesToDelete.put("ids", internalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.deleteInternalResponseCodes,valuesToDelete);
	}
	
	private Map<Long,List<Long>> fetchInternalResponseCodeIdsUsedForPaymentProcessor(Long paymentProcessId){
		LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: fetchInternalResponseCodeIdsUsedForPaymentProcessor() : Fetching Internal Response Code Ids for paymentprocessorid="+paymentProcessId);
		String query = " select InternalResponseCodeId,PaymentProcessorInternalResponseCodeID from PaymentProcessor_InternalResponseCode " +
				" where PaymentProcessorResponseCodeID in ( " +
			" select PaymentProcessorResponseCodeID from PaymentProcessorResponseCode_Lookup " +
			" where PaymentProcessorID = ? ) ";
		Map<Long,List<Long>> idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode = new HashMap<Long,List<Long>>();
		jdbcTemplate.query(query, new Object[]{paymentProcessId}, new ResultSetExtractor<Map<Long,List<Long>>>(){
			@Override
			public Map<Long,List<Long>> extractData(ResultSet rs) throws SQLException,DataAccessException {
				Long internalStatusCodeId = null;
				Long paymentProcessorInternalStatusId = null;
				while(rs.next()){
					internalStatusCodeId = rs.getLong("InternalStatusCodeId");
					paymentProcessorInternalStatusId = rs.getLong("PaymentProcessorInternalStatusCodeID");
					List<Long> paymentProcessorInternalStatusCodeIds = idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode.get(internalStatusCodeId);
					if (paymentProcessorInternalStatusCodeIds == null) {
						paymentProcessorInternalStatusCodeIds = new ArrayList<Long>();
						idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode.put(internalStatusCodeId,paymentProcessorInternalStatusCodeIds);
					}
					paymentProcessorInternalStatusCodeIds.add(paymentProcessorInternalStatusId);
				}
				return idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode;
			}
		});
		return idsOfInternalResponseCodeAndPaymentProcessorInternalStatusCode;
	}

	@Override
	public void deletePaymentProcessorInternalResponseCodeIds(List<Long> paymentProcessorInternalStatusCodeIds) {
			LOGGER.debug("PaymentProcessorInternalResponseCodeDAOImpl :: deletePaymentProcessorInternalResponseCodeIds() : Delete Payment Processor Internal Status Code_IDs="+(paymentProcessorInternalStatusCodeIds));
			Map<String, List<Long>> valuesToDelete = new HashMap<String,List<Long>>();
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
