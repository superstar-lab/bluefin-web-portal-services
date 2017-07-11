package com.mcmcg.ico.bluefin.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.mcmcg.ico.bluefin.model.InternalStatusCode;
import com.mcmcg.ico.bluefin.model.PaymentProcessorInternalStatusCode;
import com.mcmcg.ico.bluefin.repository.sql.Queries;

@Repository
public class InternalStatusCodeDAOImpl implements InternalStatusCodeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalStatusCodeDAOImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private PaymentProcessorInternalStatusCodeDAO paymentProcessorInternalStatusCodeDAO;
	
	@Override
	public InternalStatusCode findByInternalStatusCodeAndTransactionTypeName(String internalStatusCode,
			String transactionTypeName) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDBYINTERNALSTATUSCODEANDTRANSACTIONTYPE, new Object[] { internalStatusCode,transactionTypeName },
					new InternalStatusCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for internal status code = {} , Transaction Type {}",internalStatusCode,transactionTypeName,e);
        	}
			return null;
		}
	}

	@Override
	public List<InternalStatusCode> findByTransactionTypeNameOrderByInternalStatusCodeAsc(String transactionTypeName) {
		LOGGER.debug("Fetching Internal status codes for transaction type={}",transactionTypeName);
		List<InternalStatusCode> fetchedInternalStatusCodeByTransactionTypeList;
		if ("ALL".equalsIgnoreCase(transactionTypeName)) { 
			fetchedInternalStatusCodeByTransactionTypeList = sortInternalStatusCode( jdbcTemplate.query( Queries.FINDALLINTERNALSTATUSCODE, new InternalStatusCodeRowMapper() ) );
		} else {
			fetchedInternalStatusCodeByTransactionTypeList = jdbcTemplate.query( Queries.FINDALLINTERNALSTATUSCODEBYTRANSACTIONTYPE, new Object[] {transactionTypeName },
					new InternalStatusCodeRowMapper());
		}
		
		int fetchedInternalStatusCodeByTransactionTypeListSize = fetchedInternalStatusCodeByTransactionTypeList != null ? fetchedInternalStatusCodeByTransactionTypeList.size() : 0;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Total number of internal status code size = {} for transaction type=",fetchedInternalStatusCodeByTransactionTypeListSize,transactionTypeName);
		}
		return fetchedInternalStatusCodeByTransactionTypeList;
	}

	@Override
	public InternalStatusCode save(InternalStatusCode internalStatusCode) {
		LOGGER.info("Creating Internal status code");
		
		KeyHolder holder = new GeneratedKeyHolder();
		
		DateTime utc1 = internalStatusCode.getCreatedDate() != null ? internalStatusCode.getCreatedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTime utc2 =  internalStatusCode.getModifiedDate() != null ? internalStatusCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC);
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateCreated = Timestamp.valueOf(dtf.print(utc1));
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc2));

		jdbcTemplate.update(connection->{
			PreparedStatement ps = connection.prepareStatement(Queries.SAVEINTERNALSTATUSCODE, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, internalStatusCode.getInternalStatusCodeValue());
			ps.setString(2, internalStatusCode.getInternalStatusCodeDescription());
			ps.setString(3, internalStatusCode.getLastModifiedBy());
			ps.setString(4, internalStatusCode.getInternalStatusCategoryAbbr());
			ps.setString(5, internalStatusCode.getInternalStatusCategory());
			ps.setString(6, internalStatusCode.getTransactionTypeName());
			ps.setTimestamp(7, dateCreated);
			ps.setTimestamp(8, dateModified);
			return ps;
		}, holder);

		Long id = holder.getKey().longValue();
		internalStatusCode.setInternalStatusCodeId(id);
		LOGGER.debug("Saved Internal Status Code ={} ", id);

		if (internalStatusCode.getPaymentProcessorInternalStatusCodes() != null && !internalStatusCode.getPaymentProcessorInternalStatusCodes().isEmpty()) {
			LOGGER.debug("Number of childs items {}",internalStatusCode.getPaymentProcessorInternalStatusCodes().size());
			// in this case we need to create child items also.
			for (PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode : internalStatusCode.getPaymentProcessorInternalStatusCodes()) {
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCode.getInternalStatusCodeId());
			}
			paymentProcessorInternalStatusCodeDAO.save(internalStatusCode.getPaymentProcessorInternalStatusCodes());
		}
		return internalStatusCode;
		
	}

	@Override
	public InternalStatusCode update(InternalStatusCode internalStatusCode) {
		LOGGER.debug("Updating Internal Status Code ={}", internalStatusCode.getInternalStatusCodeId());
		DateTime utc4 = internalStatusCode.getModifiedDate() != null ? internalStatusCode.getModifiedDate().withZone(DateTimeZone.UTC) : DateTime.now(DateTimeZone.UTC); 
		DateTimeFormatter dtf = DateTimeFormat.forPattern(BluefinWebPortalConstants.FULLDATEFORMAT);
		Timestamp dateModified = Timestamp.valueOf(dtf.print(utc4));
		
		int rows = jdbcTemplate.update(Queries.UPDATEINTERNALSTATUSCODE,
					new Object[] { 	internalStatusCode.getInternalStatusCodeValue(), internalStatusCode.getInternalStatusCodeDescription(), internalStatusCode.getLastModifiedBy(), 
									internalStatusCode.getInternalStatusCategoryAbbr(), internalStatusCode.getInternalStatusCategory(), internalStatusCode.getTransactionTypeName(),
									dateModified, internalStatusCode.getInternalStatusCodeId()
								 });

		LOGGER.debug("Updated PaymentProcessor, No of Rows Updated ={}", rows , " and Updated InternalStatusCode with ID: =",internalStatusCode.getInternalStatusCodeId());
		if (internalStatusCode.getPaymentProcessorInternalStatusCodes() != null && !internalStatusCode.getPaymentProcessorInternalStatusCodes().isEmpty()) {
			LOGGER.debug("Number of childs items to update ={}" ,internalStatusCode.getPaymentProcessorInternalStatusCodes().size());
			// in this case we need to create child items also.
			for (PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode : internalStatusCode.getPaymentProcessorInternalStatusCodes()) {
				paymentProcessorInternalStatusCode.setInternalStatusCodeId(internalStatusCode.getInternalStatusCodeId());
				LOGGER.debug("PaymentProcessorInternalStatusCode_ChildItem={}",paymentProcessorInternalStatusCode);
			}
			paymentProcessorInternalStatusCodeDAO.delete(internalStatusCode.getInternalStatusCodeId());
			LOGGER.debug("Old Child Items deleted successfully");
			for (Iterator<PaymentProcessorInternalStatusCode> iterator = internalStatusCode.getPaymentProcessorInternalStatusCodes().iterator(); iterator.hasNext();) {
				PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode = iterator.next();
				if (paymentProcessorInternalStatusCode.getPaymentProcessorInternalStatusCodeId() != null) {
					// removing old items from collection
					iterator.remove();
				}
			}
			
			paymentProcessorInternalStatusCodeDAO.save(internalStatusCode.getPaymentProcessorInternalStatusCodes());
			LOGGER.debug("New Child Items created successfully");
		} else {
			LOGGER.debug("No childs items to update");
		}
		return internalStatusCode;
	}

	class InternalStatusCodeRowMapper implements RowMapper<InternalStatusCode> {

		@Override
		public InternalStatusCode mapRow(ResultSet rs, int row) throws SQLException {
			InternalStatusCode internalStatusCode = new InternalStatusCode();
			internalStatusCode.setInternalStatusCodeId(rs.getLong("InternalStatusCodeID"));
			internalStatusCode.setInternalStatusCodeValue(rs.getString("InternalStatusCode"));
			internalStatusCode.setInternalStatusCodeDescription(rs.getString("InternalStatusCodeDescription"));
			internalStatusCode.setLastModifiedBy(rs.getString("ModifiedBy"));
			internalStatusCode.setInternalStatusCategoryAbbr(rs.getString("InternalStatusCategoryAbbr"));
			internalStatusCode.setInternalStatusCategory(rs.getString("InternalStatusCategory"));
			internalStatusCode.setModifiedDate(new DateTime(rs.getTimestamp("DatedModified"))); 
			internalStatusCode.setTransactionTypeName(rs.getString("TransactionType"));
			internalStatusCode.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
			return internalStatusCode;
		}
	}
	
	class InternalStatusCodeComparator implements Comparator<InternalStatusCode> {

		@Override
		public int compare(InternalStatusCode internalStatusCodeObj1, InternalStatusCode internalStatusCodeObj2) {
			if (internalStatusCodeObj1 != null && internalStatusCodeObj2 != null && internalStatusCodeObj1.getInternalStatusCodeId() != null 
					&& internalStatusCodeObj2.getInternalStatusCodeId() != null) {
				return internalStatusCodeObj1.getInternalStatusCodeId().compareTo(internalStatusCodeObj2.getInternalStatusCodeId()); 
			}
			return -1;
		}
	}
	
	private List<InternalStatusCode> sortInternalStatusCode(List<InternalStatusCode> fetchedInternalStatusCodeList){
		if (fetchedInternalStatusCodeList != null && !fetchedInternalStatusCodeList.isEmpty()) {
			LinkedHashMap<String, InternalStatusCode> result = new LinkedHashMap<>();
			for(InternalStatusCode internalStatusCode : fetchedInternalStatusCodeList){
				final String description = internalStatusCode.getInternalStatusCodeDescription();
				if (result.get(description) == null) {
					result.put(description, internalStatusCode);
				}
				
			}
			return new ArrayList<>(result.values());
		}
		return new ArrayList<>();
	}

	@Override
	public InternalStatusCode findOne(Long internalStatusCodeId) {
		try {
			return jdbcTemplate.queryForObject(Queries.FINDINTERNALSTATUSCODEBYID, new Object[] { internalStatusCodeId },
					new InternalStatusCodeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			if ( LOGGER.isDebugEnabled() ) {
        		LOGGER.debug("No record found for internal status code id = {}",internalStatusCodeId,e);
        	}
			return null;
		}
	}

	@Override
	public void delete(Long internalStatusCodeId) {
		paymentProcessorInternalStatusCodeDAO.delete(internalStatusCodeId);
		LOGGER.debug("Deleting InternalStatusCodeId ={}",internalStatusCodeId);
		jdbcTemplate.update(Queries.DELETEINTERNALSTATUSCODE, internalStatusCodeId);
		LOGGER.debug("Record deleted successfully InternalStatusCodeId ={}",internalStatusCodeId);
	}

	@Override
	public InternalStatusCode findOneWithChilds(Long internalStatusCodeId) {
		InternalStatusCode internalStatusCode = findOne(internalStatusCodeId);
		LOGGER.debug("internalStatusCode ={} ",internalStatusCode);
		if (internalStatusCode != null){
			internalStatusCode.setPaymentProcessorInternalStatusCodes(paymentProcessorInternalStatusCodeDAO.findAllForInternalStatusCodeId(internalStatusCodeId));
		}
		return internalStatusCode;
	}
}
