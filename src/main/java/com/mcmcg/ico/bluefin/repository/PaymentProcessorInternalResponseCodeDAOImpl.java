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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
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
	public com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode save(
			com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode) {
			KeyHolder holder = new GeneratedKeyHolder();
		
		DateTime utc1 = paymentProcessorInternalResponseCode.getCreatedDate().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));

		jdbcTemplate.update(new PreparedStatementCreator() {

			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(Queries.savePaymentProcessorInternalResponseCode,
						Statement.RETURN_GENERATED_KEYS);
				ps.setString(1,paymentProcessorInternalResponseCode.getLastModifiedBy()); // ModifiedBy
				ps.setTimestamp(6, dateCreated);
				return ps;
				
			}
		}, holder);

		Long id = holder.getKey().longValue();
		paymentProcessorInternalResponseCode.setPaymentProcessorInternalResponseCodeId(id);
		LOGGER.debug("Saved paymentProcessorInternalResponseCode - id: " + id);

		return paymentProcessorInternalResponseCode;
	}
	
	@Override
	public com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode findOne(long paymentProcessorInternalResponseCodeId) {
		try {
			return jdbcTemplate.queryForObject(Queries.findOnePaymentProcessorInternalResponseCode, new Object[] { paymentProcessorInternalResponseCodeId },
					new PaymentProcessorInternalResponseCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public void delete(com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorPaymentProcessorResponseCodeId, new Object[] { paymentProcessorInternalResponseCode.getPaymentProcessorInternalResponseCodeId()});

		LOGGER.debug("Deleted PaymentProcessorResponseCode with PaymentProcessorResponseCodeId: " + paymentProcessorInternalResponseCode.getPaymentProcessorInternalResponseCodeId()+ ", rows affected = " + rows);

		//return rows;
		
		
	}
	
	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> findAll() {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>) jdbcTemplate.query(
				Queries.findAllPaymentProcessorInternalResponseCode, new PaymentProcessorInternalResponseCodeRowMapper());
		LOGGER.debug("Number of rows: ");
		return list;
	}
	
	@Override
	public List<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodeId(
			long internalResponseCodeId) {
		ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>) jdbcTemplate.query(
				Queries.paymentProcessorInternalResponseCodeId, new Object[] { internalResponseCodeId}, new PaymentProcessorInternalResponseCodeRowMapper());
		LOGGER.debug("Number of rows: ");
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
				LOGGER.info("Creating child item , InternalResponseCodeId="+(paymentProcessorInternalResponseCode.getPaymentProcessorInternalResponseCodeId()));//+ " , PaymentProcessorStatusCodeId="+paymentProcessorInternalStatusCode.getPaymentProcessorStatusCodeId());
				ps.setLong(1, paymentProcessorInternalResponseCode.getPaymentProcessorInternalResponseCodeId());
				ps.setLong(2, paymentProcessorInternalResponseCode.getInternalResponseCodeId());
				ps.setTimestamp(3, dateCreated);
			}

			@Override
			public int getBatchSize() {
				return paymentProcessorInternalResponseCodes.size();
			}
		  });
	}
	
	@Override
	public List<PaymentProcessorInternalResponseCode> findPaymentProcessorInternalResponseCodeListById(
			Long internalResponseCode) {
		List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes = jdbcTemplate.query(Queries.findAllPaymentProcessorInternalResponseCode, new Object[]{internalResponseCode},
				new PaymentProcessorInternalResponseCodeRowMapper());
		return paymentProcessorInternalResponseCodes;
	}
	
	@Override
	public void deleteByInternalResponseCode(Long internalResponseCode) {
		int rows = jdbcTemplate.update(Queries.deletePaymentProcessorInternalResponseCode, new Object[] {internalResponseCode});
		
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
		LOGGER.info("Finally deleteing records, Query="+deleteQuery+ " , idsToDelete="+idsToDelete);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		int noOfRowsDeleted = namedJDBCTemplate.update(deleteQuery,idsToDelete);
		LOGGER.info("Number of rows deleted="+(noOfRowsDeleted));
	}
	
	@Override
	public void delete(Long paymentProcessorInternalResponseCodeId) {
		LOGGER.info("Deleting child items for internalResponseCodeId {}",paymentProcessorInternalResponseCodeId);
		int noOfRowsDeleted = jdbcTemplate.update(Queries.deletePaymentProcessorInternalResponseCode, paymentProcessorInternalResponseCodeId);
		LOGGER.info("Number of childs items deleted {} internalStatusCodeId {}",noOfRowsDeleted,paymentProcessorInternalResponseCodeId);		
	}
	
	@Override
	public void savePaymentProcessorInternalResponseCodes(
			Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		insertBatch(new ArrayList<com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode>(paymentProcessorInternalResponseCodes));
	}
}
//SELECT PaymentProcessorInternalResponseCodeID, PaymentProcessorResponseCodeID,InternalResponseCodeID FROM PaymentProcessor_InternalResponseCode
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
