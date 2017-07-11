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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class PaymentProcessorInternalStatusCodeDAOImpl implements PaymentProcessorInternalStatusCodeDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(PaymentProcessorInternalStatusCodeDAOImpl.class);
	private final DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<PaymentProcessorInternalStatusCode> findAllForInternalStatusCodeId(Long internalStatusCodeId){
		List<PaymentProcessorInternalStatusCode> list = jdbcTemplate.query( Queries.FINDALLPAYMENTPROCESSORINTERNALSTATUSCODEFORINTERNALSTATUSCODEID, new Object[] {internalStatusCodeId},
				new PaymentProcessorInternalStatusCodeRowMapper());
		if (logger.isDebugEnabled()) {
			logger.debug("PaymentProcessorInternalStatusCode list size : "+list.size());
		}
		return list;
	}
	
	class PaymentProcessorInternalStatusCodeRowMapper implements RowMapper<PaymentProcessorInternalStatusCode> {
	
		@Override
		public PaymentProcessorInternalStatusCode mapRow(ResultSet rs, int row) throws SQLException {
			PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = new PaymentProcessorInternalStatusCode();
			paymentProcessorInternalStatusCode.setPaymentProcessorInternalStatusCodeId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORINTERNALSTATUSCODEID));
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
			paymentProcessorInternalStatusCode.setPaymentProcessorInternalStatusCodeId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORINTERNALSTATUSCODEID));
			paymentProcessorInternalStatusCode.setInternalStatusCodeId(rs.getLong("InternalStatusCodeID"));
			return paymentProcessorInternalStatusCode;
		}
	}
	
	private void insertBatch(final List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes){
		jdbcTemplate.batchUpdate(Queries.SAVEPAYMENTPROCESSORINTERNALSTATUSCODE, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = paymentProcessorInternalStatusCodes.get(i);
				DateTime utc1 = paymentProcessorInternalStatusCode.getCreatedDate() != null ? paymentProcessorInternalStatusCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
				Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
				logger.debug("Creating child item , InternalStatusCodeId=" , paymentProcessorInternalStatusCode.getInternalStatusCodeId(), " , PaymentProcessorStatusCodeId=" ,paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
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
	
	@Override
	public void save(List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes) {
		insertBatch(paymentProcessorInternalStatusCodes);
	}

	@Override
	public void delete(Long internalStatusCodeId) {
		logger.debug(" Deleting child items for internalStatusCodeId {}",internalStatusCodeId);
		int noOfRowsDeleted = jdbcTemplate.update(Queries.DELETEPAYMENTPROCESSORINTERNALSTATUSCODE, internalStatusCodeId);
		logger.debug("Number of childs items deleted {} internalStatusCodeId {}",noOfRowsDeleted,internalStatusCodeId);
	}

	@Override
	public void deletePaymentProcessorInternalStatusCodeForPaymentProcessor(Long paymentProcessorId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Delete Payment processr status code for paymentprocessorid="+paymentProcessorId);
		}
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = fetchInternalStatusCodeIdsUsedForPaymentProcessor(paymentProcessorId);
		if (logger.isDebugEnabled()) {
			logger.debug("Number of Internal Status Code Ids="+ ( idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.size() ) + " for paymentprocessid="+paymentProcessorId );
		}
		Set<Entry<Long,List<Long>>> allEntries = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.entrySet();
		if (logger.isDebugEnabled()) {
			logger.debug("allEntries size : "+allEntries.size()); 
		}
		List<Long> paymentProcessorInternalStatusCodeIds = new ArrayList<>();
		List<Long> internalStatusCodeIds = new ArrayList<>();
		for (Entry<Long,List<Long>> entry : allEntries ) {
			internalStatusCodeIds.add(entry.getKey());
			paymentProcessorInternalStatusCodeIds.addAll(entry.getValue());
		}
		if (!paymentProcessorInternalStatusCodeIds.isEmpty()) {
			deletePaymentProcessorInternalStatusCodeIds(paymentProcessorInternalStatusCodeIds);
			logger.info("PaymentProcessorInternalStatusCodeIds deletion completed");
		}
		if (!internalStatusCodeIds.isEmpty()) {
			deleteInternalStatusCodeIds(internalStatusCodeIds);
			logger.info("InternalStatusCodeIds deletion completed");
		}
	}
	
	private Map<Long,List<Long>> fetchInternalStatusCodeIdsUsedForPaymentProcessor(Long paymentProcessId){
		if(logger.isDebugEnabled()) {
			logger.debug("Fetching Internal Status Code Ids for paymentprocessorid={}",paymentProcessId);
		}
		String query = Queries.FETCHINTERNALSTATUSCODEUSEDFORPAYMENTPROCESSOR;
		Map<Long,List<Long>> idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode = new HashMap<>();
		jdbcTemplate.query(query, new Object[]{paymentProcessId}, rs->{
				Long internalStatusCodeId;
				Long paymentProcessorInternalStatusId;
				while(rs.next()){
					internalStatusCodeId = rs.getLong("InternalStatusCodeId");
					paymentProcessorInternalStatusId = rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORINTERNALSTATUSCODEID);
					List<Long> paymentProcessorInternalStatusCodeIds = idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.get(internalStatusCodeId);
					if (paymentProcessorInternalStatusCodeIds == null) {
						paymentProcessorInternalStatusCodeIds = new ArrayList<>();
						idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.put(internalStatusCodeId,paymentProcessorInternalStatusCodeIds);
					}
					paymentProcessorInternalStatusCodeIds.add(paymentProcessorInternalStatusId);
				}
				return idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode;
		});
		if(logger.isDebugEnabled())
			logger.debug(String.format("idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode size : %s",idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode.size()));
		
		return idsOfInternalStatusCodeAndPaymentProcessorInternalStatusCode;
	}

	@Override
	public void deletePaymentProcessorInternalStatusCodeIds(List<Long> paymentProcessorInternalStatusCodeIds) {
		if (logger.isDebugEnabled()) {
			logger.debug("Delete Payment Processor Internal Status Code_IDs={}",paymentProcessorInternalStatusCodeIds);
		}
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", paymentProcessorInternalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.DELETEPAYMENTPROCESSORINTERNALSTATUSCODES,valuesToDelete);
	}

	@Override
	public void deleteInternalStatusCodeIds(List<Long> internalStatusCodeIds) {
		if (logger.isDebugEnabled()) {
			logger.debug("Delete Internal Status Code_IDs={}",internalStatusCodeIds);
		}
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", internalStatusCodeIds);
		executeQueryToDeleteRecords(Queries.DELETEINTERNALSTATUSCODES,valuesToDelete);
	}
	
	private void executeQueryToDeleteRecords(String deleteQuery,Map<String, List<Long>> idsToDelete){
		if (logger.isDebugEnabled()) {
			logger.debug("Finally deleteing records, idsToDelete= {}",idsToDelete);
		}
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		int noOfRowsDeleted = namedJDBCTemplate.update(deleteQuery,idsToDelete);
		if (logger.isDebugEnabled()) {
			logger.debug("Number of rows deleted= {}",noOfRowsDeleted);
		}
	}
	
	@Override
	public List<Long> findPaymentProcessorStatusCodeIdsForInternalStatusCodeId(Long internalStatusCodeId){
		return jdbcTemplate.queryForList(Queries.FINDPAYMENTPROCESSORSTATUSCODEIDS,new Object[]{internalStatusCodeId},Long.class);
	}

	@Override
	public void deletePaymentProcessorStatusCodeIds(List<Long> paymentProcessorStatusCodeIds) {
		if (logger.isDebugEnabled()) {
			logger.debug("paymentProcessorStatusCodeIds size ={} ",paymentProcessorStatusCodeIds.size());
		}
		Map<String, List<Long>> valuesToDelete = new HashMap<>();
		valuesToDelete.put("ids", paymentProcessorStatusCodeIds);
		executeQueryToDeleteRecords(Queries.DELETEPAYMENTPROCESSORSTATUSCODEIDS,valuesToDelete);
	}
	
}
