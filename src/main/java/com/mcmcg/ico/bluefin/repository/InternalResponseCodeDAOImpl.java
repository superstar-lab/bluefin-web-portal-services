package com.mcmcg.ico.bluefin.repository;

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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
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
	public InternalResponseCode findByInternalResponseCodeAndTransactionTypeName(String internalResponseCode,
			String transactionTypeName) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDBYINTERNALRESPONSECODEANDTRANSACTIONTYPENAME, new Object[] { internalResponseCode,transactionTypeName },
					new InternalResponseCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found internal response code= {} Transaction Type= {}",internalResponseCode,transactionTypeName,e);
        	}
			return null;
		}
	
	}

	@Override
	public List<InternalResponseCode> findByTransactionTypeNameOrderByInternalResponseCodeAsc(
			String transactionTypeName) {
		LOGGER.debug("Fetching Internal Response codes for transaction type={}",transactionTypeName);
		List<InternalResponseCode> list ;
		if ("ALL".equalsIgnoreCase(transactionTypeName)) { 
			list = sortInternalResponseCode( jdbcTemplate.query( Queries.FINDALLINTERNALRESPONSECODE, new InternalResponseCodeRowMapper() ) );
			
		}else{
			list= jdbcTemplate.query(
					Queries.FINDALLINTERNALRESPONSECODEBYTRANSACTIONTYPE,
					new Object[] {  transactionTypeName }, new InternalResponseCodeRowMapper()  );
		}
		
		LOGGER.debug("Number of rows ={} ",list.size());
		return list;
	}
	
	private List<InternalResponseCode> sortInternalResponseCode(List<InternalResponseCode> fetchedInternalStatusCodeList){
		if (fetchedInternalStatusCodeList != null && !fetchedInternalStatusCodeList.isEmpty()) {
			LinkedHashMap<String, InternalResponseCode> result = new LinkedHashMap<>();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("size of fetchedInternalStatusCode_List : {} ",fetchedInternalStatusCodeList.size());
			}
			for(InternalResponseCode internalStatusCode : fetchedInternalStatusCodeList){
				
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
	public InternalResponseCode save(InternalResponseCode internalResponseCode) {
		KeyHolder holder = new GeneratedKeyHolder();
		internalResponseCode.setDateCreated(DateTime.now(DateTimeZone.UTC));
		internalResponseCode.setDateModified(DateTime.now(DateTimeZone.UTC));
		DateTime utc1 = internalResponseCode.getDateCreated().withZone(DateTimeZone.UTC);
		DateTime utc2 = internalResponseCode.getDateModified().withZone(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
			PreparedStatement ps = connection.prepareStatement(Queries.SAVEINTERNALRESPONSECODE,
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
		LOGGER.debug("Saved tInternalResponseCode - id: {}", id);

		if (internalResponseCode.getPaymentProcessorInternalResponseCodes() != null && !internalResponseCode.getPaymentProcessorInternalResponseCodes().isEmpty()) {
			LOGGER.debug("Number of childs items =  {}",internalResponseCode.getPaymentProcessorInternalResponseCodes().size());
			// in this case we need to create child items also.
			for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : internalResponseCode.getPaymentProcessorInternalResponseCodes()) {
				paymentProcessorInternalResponseCode.setInternalResponseCode(internalResponseCode);
			}
			paymentProcessorInternalResponseCodeDAO.createPaymentProcessorInternalStatusCode(internalResponseCode.getPaymentProcessorInternalResponseCodes());
		}
		return internalResponseCode;
	}

	@Override
	public InternalResponseCode findOne(long internalResponseCodeId) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDONEINTERNALRESPONSECODE, new Object[] { internalResponseCodeId },
					new InternalResponseCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for internal response code id= {}",internalResponseCodeId,e);
        	}
			return null;
		}
	}

	@Override
	public void delete(InternalResponseCode internalResponseCode) {
		paymentProcessorInternalResponseCodeDAO.deleteByInternalResponseCode(internalResponseCode.getInternalResponseCodeId());
		int rows = jdbcTemplate.update(Queries.DELETEINTERNALRESPONSECODE, new Object[] { internalResponseCode.getInternalResponseCodeId() });
		LOGGER.info("Number of InternalResponseCode deleted = {}",rows);
	}

	@Override
	public List<InternalResponseCode> findAll() {
		ArrayList<InternalResponseCode> list = (ArrayList<InternalResponseCode>) jdbcTemplate.query(
				Queries.FINDALLINTERNALRESPONSECODE, new InternalResponseCodeRowMapper());
		LOGGER.debug("Number of rows: ={} ",list.size());
		return list;
	}
	class InternalResponseCodeRowMapper implements RowMapper<InternalResponseCode> {

		@Override
		public InternalResponseCode mapRow(ResultSet rs, int row) throws SQLException {
			InternalResponseCode  internalResponseCode = new InternalResponseCode();
			internalResponseCode.setDateCreated(getItemDate(rs.getString("DateCreated"), BluefinWebPortalConstants.FULLDATEFORMAT));
			internalResponseCode.setInternalResponseCodeValue(rs.getString("InternalResponseCode"));
			internalResponseCode.setInternalResponseCodeDescription(rs.getString("InternalResponseCodeDescription"));
			internalResponseCode.setInternalResponseCodeId(rs.getLong("InternalResponseCodeID"));
			internalResponseCode.setLastModifiedBy(rs.getString("ModifiedBy"));
			internalResponseCode.setDateModified(getItemDate(rs.getString("DatedModified"), BluefinWebPortalConstants.FULLDATEFORMAT));
			internalResponseCode.setTransactionTypeName(rs.getString("TransactionType"));
		    		
			
			return internalResponseCode;
		}
	}
	@Override
	public InternalResponseCode update(InternalResponseCode internalResponseCode) {

		LOGGER.info("Updating Internal Response Code");
		DateTime utc4 = internalResponseCode.getDateModified() != null ? internalResponseCode.getDateModified().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC); 
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc4));

		int rows = jdbcTemplate.update(Queries.UPDATEINTERNALRESPONSECODE,
					new Object[] { 	internalResponseCode.getInternalResponseCodeValue(), internalResponseCode.getInternalResponseCodeDescription(), internalResponseCode.getLastModifiedBy(), 
							internalResponseCode.getTransactionTypeName(), dateModified, internalResponseCode.getInternalResponseCodeId() });

		LOGGER.debug("Updated InternalStatusCode with ID: {} , rows affected = {}", internalResponseCode.getInternalResponseCodeId(),rows);
		if (internalResponseCode.getPaymentProcessorInternalResponseCodes() != null && !internalResponseCode.getPaymentProcessorInternalResponseCodes().isEmpty()) {
			LOGGER.debug("Number of childs items to update {}",internalResponseCode.getPaymentProcessorInternalResponseCodes().size());
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
		LOGGER.debug("internalStatusCode  : {}",internalStatusCode);
		if (internalStatusCode != null){
			List<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodeList = paymentProcessorInternalResponseCodeDAO.paymentProcessorInternalResponseCodeId(internalResponseCodeId);
			LOGGER.debug("paymentProcessorInternalResponseCodeList size : {}",paymentProcessorInternalResponseCodeList.size());
			for (PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode : paymentProcessorInternalResponseCodeList) {
				PaymentProcessorResponseCode paymentProcessorResponseCode = paymentProcessorResponseCodeDAO.findOne(paymentProcessorInternalResponseCode.getPaymentProcessorResponseCode().getPaymentProcessorResponseCodeId());
				paymentProcessorInternalResponseCode.setPaymentProcessorResponseCode(paymentProcessorResponseCode);
			}
			internalStatusCode.setPaymentProcessorInternalResponseCodes(paymentProcessorInternalResponseCodeList);
		}
		return internalStatusCode;
	}

}
