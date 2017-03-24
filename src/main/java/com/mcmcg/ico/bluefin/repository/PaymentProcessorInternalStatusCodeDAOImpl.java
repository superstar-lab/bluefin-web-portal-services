package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorInternalStatusCodeDAOImpl implements PaymentProcessorInternalStatusCodeDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorInternalStatusCodeDAOImpl.class);
	private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public List<PaymentProcessorInternalStatusCode> findAllForInternalStatusCodeId(Long internalStatusCodeId){
		return jdbcTemplate.query( Queries.findAllPaymentProcessorInternalStatusCodeForInternalStatusCodeId, new Object[] {internalStatusCodeId},
				new PaymentProcessorInternalStatusCodeRowMapper());
	}
	
	class PaymentProcessorInternalStatusCodeRowMapper implements RowMapper<PaymentProcessorInternalStatusCode> {
	
		@Override
		public PaymentProcessorInternalStatusCode mapRow(ResultSet rs, int row) throws SQLException {
			PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
			paymentProcessorInternalStatusCode.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
			paymentProcessorInternalStatusCode.setInternalStatusCodeId(rs.getLong("InternalStatusCodeID"));
			paymentProcessorInternalStatusCode.setPaymentProcessorStatusCodeId(rs.getLong("PaymentProcessorStatusCodeID"));
			paymentProcessorInternalStatusCode.setLastModifiedBy(rs.getString("ModifiedBy"));
			paymentProcessorInternalStatusCode.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
			return paymentProcessorInternalStatusCode;
		}
	}
	
	class PaymentProcessorInternalStatusCodeAndInternalStatusIdsRowMapper implements RowMapper<PaymentProcessorInternalStatusCode> {
		@Override
		public PaymentProcessorInternalStatusCode mapRow(ResultSet rs, int row) throws SQLException {
			PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
			paymentProcessorInternalStatusCode.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
			paymentProcessorInternalStatusCode.setInternalStatusCodeId(rs.getLong("InternalStatusCodeID"));
			return paymentProcessorInternalStatusCode;
		}
	}
	
	private void insertBatch(final List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes){
		jdbcTemplate.batchUpdate(Queries.savePaymentProcessorInternalStatusCode, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = paymentProcessorInternalStatusCodes.get(i);
				DateTime utc1 = paymentProcessorInternalStatusCode.getCreatedDate() != null ? paymentProcessorInternalStatusCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
				LOGGER.info("Creating child item , InternalStatusCodeId="+(paymentProcessorInternalStatusCode.getInternalStatusCodeId()) + " , PaymentProcessorStatusCodeId="+paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
				ps.setLong(1, paymentProcessorInternalStatusCode.getInternalStatusCodeId());
				ps.setLong(2, paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
				ps.setTimestamp(3, dateCreated);
				ps.setString(4, paymentProcessorInternalStatusCode.getLastModifiedBy() );
			}

			@Override
			public int getBatchSize() {
				return paymentProcessorInternalStatusCodes.size();
			}
		  });
	}
//
//	private void updateBatch(final List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes){
//		jdbcTemplate.batchUpdate(Queries.savePaymentProcessorInternalStatusCode, new BatchPreparedStatementSetter() {
//			
//			@Override
//			public void setValues(PreparedStatement ps, int i) throws SQLException {
//				PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = paymentProcessorInternalStatusCodes.get(i);
//				DateTime utc1 = paymentProcessorInternalStatusCode.getCreatedDate() != null ? paymentProcessorInternalStatusCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
//				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
//				LOGGER.info("Creating child item , InternalStatusCodeId="+(paymentProcessorInternalStatusCode.getInternalStatusCodeId()) + " , PaymentProcessorStatusCodeId="+paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
//				ps.setLong(1, paymentProcessorInternalStatusCode.getInternalStatusCodeId());
//				ps.setLong(2, paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
//				ps.setTimestamp(3, dateCreated);
//				ps.setString(4, paymentProcessorInternalStatusCode.getLastModifiedBy() );
//			}
//
//			@Override
//			public int getBatchSize() {
//				return paymentProcessorInternalStatusCodes.size();
//			}
//		  });
//	}
	
	@Override
	public void save(PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode) {
		List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes = new ArrayList<PaymentProcessorInternalStatusCode>();
		paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);
		save(paymentProcessorInternalStatusCodes);
	}
	
	@Override
	public void save(List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes) {
		insertBatch(paymentProcessorInternalStatusCodes);
	}

	@Override
	public void delete(Long internalStatusCodeId) {
		LOGGER.info("Deleting child items for internalStatusCodeId {}",internalStatusCodeId);
		int noOfRowsDeleted = jdbcTemplate.update(Queries.deletePaymentProcessorInternalStatusCode, internalStatusCodeId);
		LOGGER.info("Number of childs items deleted {} internalStatusCodeId {}",noOfRowsDeleted,internalStatusCodeId);
	}

	@Override
	public void deletePaymentProcessorInternalStatusCodeForPaymentProcessor(Long paymentProcessorId) {
		LOGGER.info("Delete Payment processr status code for paymentprocessorid="+paymentProcessorId);
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = fetchInternalStatusCodeIdsUsedForPaymentProcessor(paymentProcessorId);
		LOGGER.info("Number of Internal Status Code Ids="+ ( idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.size() ) + " for paymentprocessid="+paymentProcessorId );
		if (idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode != null && !idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.isEmpty()) {
			Set<Entry<Long,List<Long>>> allEntries = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.entrySet();
			List<Long> paymentProcessorInternalStatusCodeIds = new ArrayList<Long>();
			List<Long> internalStatusCodeIds = new ArrayList<Long>();
			if (allEntries != null && !allEntries.isEmpty()) {
				for (Entry<Long,List<Long>> entry : allEntries ) {
					internalStatusCodeIds.add(entry.getKey());
					paymentProcessorInternalStatusCodeIds.addAll(entry.getValue());
				}
			}
			if (!paymentProcessorInternalStatusCodeIds.isEmpty()) {
				deletePaymentProcessorInternalStatusCodeIds(paymentProcessorInternalStatusCodeIds);
				LOGGER.info("PaymentProcessorInternalStatusCodeIds deletion completed");
			}
			if (!internalStatusCodeIds.isEmpty()) {
				deleteInternalStatusCodeIds(internalStatusCodeIds);
				LOGGER.info("InternalStatusCodeIds deletion completed");
			}
		}
	}
	
	private Map<Long,List<Long>> fetchInternalStatusCodeIdsUsedForPaymentProcessor(Long paymentProcessId){
		LOGGER.info("Fetching Internal Status Code Ids for paymentprocessorid="+paymentProcessId);
		String query = " select InternalStatusCodeId,PaymentProcessorInternalStatusCodeID from PaymentProcessor_InternalStatusCode " +
				" where PaymentProcessorStatusCodeID in ( " +
			" select PaymentProcessorStatusCodeID from PaymentProcessorStatusCode_Lookup " +
			" where PaymentProcessorID = ? ) ";
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = new HashMap<Long,List<Long>>();
		jdbcTemplate.query(query, new Object[]{paymentProcessId}, new ResultSetExtractor<Map<Long,List<Long>>>(){
			@Override
			public Map<Long,List<Long>> extractData(ResultSet rs) throws SQLException,DataAccessException {
				Long internalStatusCodeId = null;
				Long paymentProcessorInternalStatusId = null;
				while(rs.next()){
					internalStatusCodeId = rs.getLong("InternalStatusCodeId");
					paymentProcessorInternalStatusId = rs.getLong("PaymentProcessorInternalStatusCodeID");
					List<Long> paymentProcessorInternalStatusCodeIds = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.get(internalStatusCodeId);
					if (paymentProcessorInternalStatusCodeIds == null) {
						paymentProcessorInternalStatusCodeIds = new ArrayList<Long>();
						idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.put(internalStatusCodeId,paymentProcessorInternalStatusCodeIds);
					}
					paymentProcessorInternalStatusCodeIds.add(paymentProcessorInternalStatusId);
				}
				return idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode;
			}
		});
		return idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode;
	}

	@Override
	public void deletePaymentProcessorInternalStatusCodeIds(List<Long> paymentProcessorInternalStatusCodeIds) {
		LOGGER.info("Delete Payment Processor Internal Status Code_IDs="+(paymentProcessorInternalStatusCodeIds));
		Map<String, List<Long>> valuesToDelete = new HashMap<String,List<Long>>();
		valuesToDelete.put("ids", paymentProcessorInternalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.deletePaymentProcessorInternalStatusCodes,valuesToDelete);
	}

	@Override
	public void deleteInternalStatusCodeIds(List<Long> internalStatusCodeIds) {
		LOGGER.info("Delete Internal Status Code_IDs="+(internalStatusCodeIds));
		Map<String, List<Long>> valuesToDelete = new HashMap<String,List<Long>>();
		valuesToDelete.put("ids", internalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.deleteInternalStatusCodes,valuesToDelete);
	}
	
	private void executeQueryToDeleteRecords(String deleteQuery,Map<String, List<Long>> idsToDelete){
		LOGGER.info("Finally deleteing records, Query="+deleteQuery+ " , idsToDelete="+idsToDelete);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		int noOfRowsDeleted = namedJDBCTemplate.update(deleteQuery,idsToDelete);
		LOGGER.info("Number of rows deleted="+(noOfRowsDeleted));
	}
	
	@Override
	public List<Long> findPaymentProcessorStatusCodeIdsForInternalStatusCodeId(Long internalStatusCodeId){
		return jdbcTemplate.queryForList(Queries.findPaymentProcessorStatusCodeIds,new Object[]{internalStatusCodeId},Long.class);
	}

	@Override
	public void deletePaymentProcessorStatusCodeIds(List<Long> paymentProcessorStatusCodeIds) {
		Map<String, List<Long>> valuesToDelete = new HashMap<String,List<Long>>();
		valuesToDelete.put("ids", paymentProcessorStatusCodeIds);
		executeQueryToDeleteRecords(Queries.deletePaymentProcessorStatusCodeIds,valuesToDelete);
	}

	@Override
	public void deletePaymentProcessorStatusCodeIds(Long internalStatusCodeId) {
		List<Long> paymentProcessorStatusCodeIds = findPaymentProcessorStatusCodeIdsForInternalStatusCodeId(internalStatusCodeId);
		if (paymentProcessorStatusCodeIds != null && !paymentProcessorStatusCodeIds.isEmpty()) {
			deletePaymentProcessorStatusCodeIds(paymentProcessorStatusCodeIds);
		}
	}
	
	
}
