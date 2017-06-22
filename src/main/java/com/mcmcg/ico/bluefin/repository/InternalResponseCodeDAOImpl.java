package com.mcmcg.ico.bluefin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.InternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorResponseCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class InternalResponseCodeDAOImpl implements InternalResponseCodeDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(InternalResponseCodeDAOImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private PaymentProcessorInternalResponseCodeDAO paymentProcessorInternalResponseCodeDAO;
	
	@Autowired
	private PaymentProcessorResponseCodeDAO paymentProcessorResponseCodeDAO;
	
	@Override
	public com.mcmcg.ico.bluefin.model.InternalResponseCode findByInternalResponseCodeAndTransactionTypeName(String internalResponseCode,
			String transactionTypeName) {
		try {
			return jdbcTemplate.queryForObject(Queries.findByInternalResponseCodeAndTransactionTypeName, new Object[] { internalResponseCode,transactionTypeName },
					new InternalResponseCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found internal response code= {} Transaction Type= {}",internalResponseCode,transactionTypeName,e);
        	}
			return null;
		}
	
	}

	@Override
	public List<com.mcmcg.ico.bluefin.model.InternalResponseCode> findByTransactionTypeNameOrderByInternalResponseCodeAsc(
			String transactionTypeName) {
		LOGGER.debug("Fetching Internal Response codes for transaction type="+transactionTypeName);
		List<com.mcmcg.ico.bluefin.model.InternalResponseCode> list ;
		if ("ALL".equalsIgnoreCase(transactionTypeName)) { 
			list = sortInternalResponseCode( jdbcTemplate.query( Queries.findAllInternalResponseCode, new InternalResponseCodeRowMapper() ) );
			
		}else{
			list= (ArrayList<com.mcmcg.ico.bluefin.model.InternalResponseCode>) jdbcTemplate.query(
					Queries.findAllInternalResponseCodeByTransactionType,
					new Object[] {  transactionTypeName }, new InternalResponseCodeRowMapper()  );
		}
		
		LOGGER.debug("InternalResponseCodeDAOImpl :: findByTransactionTypeNameOrderByInternalResponseCodeAsc() : Number of rows: "+list.size());
		return list;
	}
	
	private List<InternalResponseCode> sortInternalResponseCode(List<InternalResponseCode> fetchedInternalStatusCode_List){
		if (fetchedInternalStatusCode_List != null && !fetchedInternalStatusCode_List.isEmpty()) {
			LinkedHashMap<String, InternalResponseCode> result = new LinkedHashMap<>();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("InternalResponseCodeDAOImpl :: sortInternalResponseCode() : size of fetchedInternalStatusCode_List : {} ",fetchedInternalStatusCode_List.size());
			}
			for(InternalResponseCode internalStatusCode : fetchedInternalStatusCode_List){
				
				final String description = internalStatusCode.getInternalResponseCodeDescription();
				if (result.get(description) == null) {
					result.put(description, internalStatusCode);
				}
				
			}
			return new ArrayList<>(result.values());
		}
		return new ArrayList<>();
	}
	public static DateTime getItemDate(String date, String pattern) {
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
	public com.mcmcg.ico.bluefin.model.InternalResponseCode save(com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode) {
		KeyHolder holder = new GeneratedKeyHolder();
		internalResponseCode.setCreatedDate(DateTime.now(DateTimeZone.UTC));
		internalResponseCode.setModifiedDate(DateTime.now(DateTimeZone.UTC));
		DateTime utc1 = internalResponseCode.getCreatedDate().withZone(DateTimeZone.UTC);
		DateTime utc2 = internalResponseCode.getModifiedDate().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
			PreparedStatement ps = connection.prepareStatement(Queries.saveInternalResponseCode,
						Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, internalResponseCode.getInternalResponseCodeValue()); // PermissionName
			ps.setString(2, internalResponseCode.getInternalResponseCodeDescription()); // Description
			ps.setString(3, internalResponseCode.getLastModifiedBy()); // DateCreated
			ps.setTimestamp(4, dateModified); // DateModified
			ps.setString(5, internalResponseCode.getTransactionTypeName()); // ModifiedBy
			ps.setTimestamp(6, dateCreated);
			return ps;
		}, holder);
		
		Long id = holder.getKey().longValue();
		internalResponseCode.setInternalResponseCodeId(id);
		LOGGER.debug("Saved tInternalResponseCode - id: " + id);

		if (internalResponseCode.getPaymentProcessorInternalResponseCodes() != null && !internalResponseCode.getPaymentProcessorInternalResponseCodes().isEmpty()) {
			LOGGER.debug("InternalResponseCodeDAOImpl :: save() : Number of childs items {}"+internalResponseCode.getPaymentProcessorInternalResponseCodes().size());
			// in this case we need to create child items also.
			for (com.mcmcg.ico.bluefin.model.PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : internalResponseCode.getPaymentProcessorInternalResponseCodes()) {
				paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
			}
			paymentProcessorInternalResponseCodeDAO.createPaymentProcessorInternalStatusCode(internalResponseCode.getPaymentProcessorInternalResponseCodes());
		}
		return internalResponseCode;
	}

	@Override
	public com.mcmcg.ico.bluefin.model.InternalResponseCode findOne(long internalResponseCodeId) {
		try {
			return jdbcTemplate.queryForObject(Queries.findOneInternalResponseCode, new Object[] { internalResponseCodeId },
					new InternalResponseCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for internal response code id= {}",internalResponseCodeId,e);
        	}
			return null;
		}
	}

	@Override
	public void delete(com.mcmcg.ico.bluefin.model.InternalResponseCode internalResponseCode) {
		paymentProcessorInternalResponseCodeDAO.deleteByInternalResponseCode(internalResponseCode.getInternalResponseCodeId());
		int rows = jdbcTemplate.update(Queries.deleteInternalResponseCode, new Object[] { internalResponseCode.getInternalResponseCodeId() });
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Deleted InternalResponseCode with InternalResponseCodeid: {} , rows affected = {} ", internalResponseCode .getInternalResponseCodeId(), rows);
		}
	}

	@Override
	public List<com.mcmcg.ico.bluefin.model.InternalResponseCode> findAll() {
		ArrayList<com.mcmcg.ico.bluefin.model.InternalResponseCode> list = (ArrayList<com.mcmcg.ico.bluefin.model.InternalResponseCode>) jdbcTemplate.query(
				Queries.findAllInternalResponseCode, new InternalResponseCodeRowMapper());
		LOGGER.debug("InternalResponseCodeDAOImpl :: findAll() : Number of rows: "+list.size());
		return list;
	}
	class InternalResponseCodeRowMapper implements RowMapper<com.mcmcg.ico.bluefin.model.InternalResponseCode> {

		@Override
		public com.mcmcg.ico.bluefin.model.InternalResponseCode mapRow(ResultSet rs, int row) throws SQLException {
			com.mcmcg.ico.bluefin.model.InternalResponseCode  internalResponseCode = new com.mcmcg.ico.bluefin.model.InternalResponseCode();
			internalResponseCode.setCreatedDate(getItemDate(rs.getString("DateCreated"), "YYYY-MM-dd HH:mm:ss.SSS"));
			internalResponseCode.setInternalResponseCodeValue(rs.getString("InternalResponseCode"));
			internalResponseCode.setInternalResponseCodeDescription(rs.getString("InternalResponseCodeDescription"));
			internalResponseCode.setInternalResponseCodeId(rs.getLong("InternalResponseCodeID"));
			internalResponseCode.setLastModifiedBy(rs.getString("ModifiedBy"));
			internalResponseCode.setModifiedDate(getItemDate(rs.getString("DatedModified"), "YYYY-MM-dd HH:mm:ss.SSS"));
			internalResponseCode.setTransactionTypeName(rs.getString("TransactionType"));
		    		
			
			return internalResponseCode;
		}
	}
	@Override
	public InternalResponseCode update(InternalResponseCode internalResponseCode) {

		LOGGER.info("Updating Internal Response Code##");
		DateTime utc4 = internalResponseCode.getModifiedDate() != null ? internalResponseCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC); 
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc4));

		int rows = jdbcTemplate.update(Queries.updateInternalResponseCode,
					new Object[] { 	internalResponseCode.getInternalResponseCodeValue(), internalResponseCode.getInternalResponseCodeDescription(), internalResponseCode.getLastModifiedBy(), 
							internalResponseCode.getTransactionTypeName(), dateModified, internalResponseCode.getInternalResponseCodeId() });

		LOGGER.debug("InternalResponseCodeDAOImpl :: update() : Updated InternalStatusCode with ID: " + internalResponseCode.getInternalResponseCodeId() + ", rows affected = " + rows);
		if (internalResponseCode.getPaymentProcessorInternalResponseCodes() != null && !internalResponseCode.getPaymentProcessorInternalResponseCodes().isEmpty()) {
			LOGGER.debug("InternalResponseCodeDAOImpl :: update() : Number of childs items to update {}"+internalResponseCode.getPaymentProcessorInternalResponseCodes().size());
			// in this case we need to create child items also.
			for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : internalResponseCode.getPaymentProcessorInternalResponseCodes()) {
				paymentProcessorInternalResponseCode.setInternalResponseCodeId(internalResponseCode.getInternalResponseCodeId());
				paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
				
			}
			paymentProcessorInternalResponseCodeDAO.delete(internalResponseCode.getInternalResponseCodeId());
			LOGGER.info("Old Child Items deleted successfully");
			for (Iterator<PaymentProcessorInternalResponseCode> iterator = internalResponseCode.getPaymentProcessorInternalResponseCodes().iterator(); iterator.hasNext();) {
				PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode = iterator.next();
				if (paymentProcessorInternalResponseCode.getPaymentProcessorInternalResponseCodeId() != null) {
					// removing old items from collection
					iterator.remove();
				}
			}
			paymentProcessorInternalResponseCodeDAO.savePaymentProcessorInternalResponseCodes(internalResponseCode.getPaymentProcessorInternalResponseCodes());
			LOGGER.info("New Child Items created successfully");
		} else {
			LOGGER.info("No childs items to update");
		}
		return internalResponseCode;
	
	}

	@Override
	public InternalResponseCode findOneWithChilds(Long internalResponseCodeId) {
		InternalResponseCode internalStatusCode = findOne(internalResponseCodeId);
		LOGGER.debug("InternalResponseCodeDAOImpl :: findOneWithChilds() : internalStatusCode  : "+internalStatusCode);
		if (internalStatusCode != null){
			List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodeList = paymentProcessorInternalResponseCodeDAO.paymentProcessorInternalResponseCodeId(internalResponseCodeId);
			LOGGER.debug("InternalResponseCodeDAOImpl :: findOneWithChilds() : paymentProcessorInternalResponseCodeList size : "+paymentProcessorInternalResponseCodeList.size());
			for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : paymentProcessorInternalResponseCodeList) {
				PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.findOne(paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
			}
			internalStatusCode.setPaymentProcessorInternalResponseCodes(paymentProcessorInternalResponseCodeList);
		}
		return internalStatusCode;
	}

}
