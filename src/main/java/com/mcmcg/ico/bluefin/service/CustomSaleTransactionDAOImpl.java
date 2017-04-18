package com.mcmcg.ico.bluefin.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.ReconciliationStatus;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PropertyDAO;
import com.mcmcg.ico.bluefin.repository.ReconciliationStatusDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

@Repository
public class CustomSaleTransactionDAOImpl implements CustomSaleTransactionDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomSaleTransactionDAOImpl.class);

	private static final String EQUALS = " = ";
	private static final String LOE = " <= ";
	private static final String GOE = " >= ";
	
	public CustomSaleTransactionDAOImpl(){
		loadSaleTransactionMappings();
	}
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private HashMap<String, String> predicatesHashMapping = new HashMap<String, String>();
	
	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;
	
	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;
	
	@Autowired
	private PropertyDAO propertyDAO;
	
	/**
	 * Loads the predicates mapping the elements in the saletransaction entity
	 */
	private void loadSaleTransactionMappings() {
		LOGGER.info("Loading Predicates");
		predicatesHashMapping.put("saleTransactionId", ":prefix.SaleTransactionID = :saleTransactionIdParam1");
		predicatesHashMapping.put("transactionId",
				"(:prefix.ApplicationTransactionID = :transactionIdParam1 OR :prefix.ProcessorTransactionID = :transactionIdParam1)");
		predicatesHashMapping.put("merchantId", ":prefix.MerchantID = :merchantIdParam1");
		predicatesHashMapping.put("transactionType", ":prefix.TransactionType = :transactionTypeParam1");
		predicatesHashMapping.put("processorName", ":prefix.Processor = :processorNameParam1");
		predicatesHashMapping.put("internalStatusCode", ":prefix.InternalStatusCode = :internalStatusCodeParam1");
		predicatesHashMapping.put("internalStatusDescription",
				":prefix.InternalStatusDescription = :internalStatusDescriptionParam1");
		predicatesHashMapping.put("transactionDateTime",
				":prefix.TransactionDateTime :atributeOperator :transactionDateTimeParam1");
		predicatesHashMapping.put("amount", ":prefix.ChargeAmount :atributeOperator :amountParam1");
		predicatesHashMapping.put("firstName", ":prefix.FirstName LIKE :firstNameParam1");
		predicatesHashMapping.put("lastName", ":prefix.LastName LIKE :lastNameParam1");
		predicatesHashMapping.put("cardType", ":prefix.CardType = :cardTypeParam1");
		predicatesHashMapping.put("legalEntity", ":prefix.LegalEntityApp IN (:legalEntityParam1)");
		predicatesHashMapping.put("accountNumber", ":prefix.AccountId = :accountNumberParam1");
		predicatesHashMapping.put("application", ":prefix.Application = :applicationParam1");
		predicatesHashMapping.put("processUser", ":prefix.ProcessUser = :processUserParam1");
		predicatesHashMapping.put("batchUploadId", ":prefix.BatchUploadID = :batchUploadIdParam1"); // This is ONLY for sale
		predicatesHashMapping.put("pUser", ":prefix.pUser = :pUserParam1"); // This is ONLY for void and refund 
		predicatesHashMapping.put("accountPeriod", ":prefix.AccountPeriod = :accountPeriodParam1");
		predicatesHashMapping.put("desk", ":prefix.Desk = :deskParam1");
		predicatesHashMapping.put("invoiceNumber", ":prefix.InvoiceNumber = :invoiceNumberParam1");
		predicatesHashMapping.put("paymentFrequency", "lower(:prefix.Origin) IN (:paymentFrequencyParam1)");
		// Payment Processor Remittance
		predicatesHashMapping.put("paymentProcessorId", ":prefix.PaymentProcessorID = :paymentProcessorIdParam1");
		predicatesHashMapping.put("processorName", ":prefix.Processor = :processorNameParam1");
		predicatesHashMapping.put("reconciliationStatusId",
				":prefix.ReconciliationStatusID = :reconciliationStatusIdParam1");
		predicatesHashMapping.put("remittanceCreationDate",
				":prefix.RemittanceCreationDate :atributeOperator :remittanceCreationDateParam1");
		predicatesHashMapping.put("processorTransactionId",
				":prefix.ProcessorTransactionID = :processorTransactionIdParam1");
		
		LOGGER.debug("After populate="+predicatesHashMapping);
	}
	
	@Override
	public List<SaleTransaction> findTransactionsReport(String search) throws ParseException {
		LOGGER.info("Executing findTransactionsReport , Search Value {}",search);
		HashMap<String, String> dynamicParametersMap = new HashMap<String, String> ();
		String query = getQueryByCriteria(search,dynamicParametersMap);
		LOGGER.debug("Dynamic Query {}", query);
		
		Map<String, CustomQuery> queriesMap = createQueries(query, null,dynamicParametersMap);
		CustomQuery result = queriesMap.get("result");
		String finalQueryToExecute = result.getFinalQueryToExecute();
		int transactionsReportMaxSize=getIntValue(propertyDAO.getPropertyValue("TRANSACTIONS_REPORT_MAX_SIZE"));
		if (transactionsReportMaxSize > 0) {
			finalQueryToExecute = finalQueryToExecute + " LIMIT " + transactionsReportMaxSize;
		}
		LOGGER.info("Query to execute="+finalQueryToExecute);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		List<SaleTransaction> tr = namedJDBCTemplate.query(finalQueryToExecute,result.getParametersMap(),new SaleTransactionRowMapper());
		LOGGER.info("Total number of rows="+( tr != null ? tr.size() :0 ) );
		return tr;
	}
	
	private int getIntValue(String valueToConvert){
		try {
			return Integer.parseInt(valueToConvert); 
		} catch (NumberFormatException nfe) {
			
		}
		return -1;
	}
	
	@Override
	public List<RemittanceSale> findRemittanceSaleRefundTransactionsReport(String search,boolean negate)
			throws ParseException {
		LOGGER.info("Executing findRemittanceSaleRefundTransactionsReport , Search Value {}",search);
		String query = getNativeQueryForRemittanceSaleRefund(search,negate);
		CustomQuery queryObj = new CustomQuery(query);
		query = queryObj.getFinalQueryToExecute();
		
		int transactionsReportMaxSize = getIntValue(propertyDAO.getPropertyValue("TRANSACTIONS_REPORT_MAX_SIZE"));
		if (transactionsReportMaxSize > 0) {
			query = query + " LIMIT " + transactionsReportMaxSize;
		}
		LOGGER.info("RRR***-Result Data Query to execute:"+query);
		@SuppressWarnings("unchecked")
		List<RemittanceSale> tr = jdbcTemplate.query(query,new PaymentProcessorRemittanceExtractor());
		LOGGER.info("Total number of rows="+( tr != null ? tr.size() :0 ));
		return tr;
	}

	@Override
	public PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) throws ParseException {
		LOGGER.info("Executing findRemittanceSaleRefundTransactionsDetail, Transaction_Id {} , Transaction Type {} , Processor Transaction Type {}",transactionId,transactionType,processorTransactionType);
		String query = getNativeQueryForRemittanceSaleRefundDetail(transactionId, transactionType,processorTransactionType);
		PaymentProcessorRemittance ppr = null;
		if (query != null && query.length() > 0) {
			LOGGER.info("Detail Page Query: {}", query);
			ppr = fetchPaymentProcessorRemittanceCustomMappingResult_Single(query); 
		}
		return ppr;
	}
	
	@Override
	public Page<SaleTransaction> findTransaction(String search, PageRequest page) throws ParseException {
		LOGGER.info("Executing findTransaction, Search  Value {} , page{} ",search,page); 
		HashMap<String, String> dynamicParametersMap = new HashMap<String, String> ();
		String query = getQueryByCriteria(search,dynamicParametersMap);
		LOGGER.debug("Query="+(query));
		Map<String, CustomQuery> queriesMap = createQueries(query, page,dynamicParametersMap);
		CustomQuery result = queriesMap.get("result");
		CustomQuery queryTotal = queriesMap.get("queryTotal");
		int pageNumber = ( page != null ? page.getPageNumber() : 0 );
		int pageSize = ( page != null ? page.getPageSize() : 0 );
		if ( result != null ) {
			result.setPagination(true);
			result.setPageSize(pageSize);
			result.setPageNumber(pageNumber);
		}
		String queryTotal_FinalQueryToExecute = queryTotal.getFinalQueryToExecute();
		LOGGER.info("TTT***-Count Query to execute:"+queryTotal_FinalQueryToExecute);
		// Set the paging for the created select
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		Integer countResult = namedJDBCTemplate.query(queryTotal_FinalQueryToExecute, queryTotal.getParametersMap(), new ResultSetExtractor<Integer>() {
			@Override
			public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
				Integer finalCount = null;
				while (rs.next()) {
					finalCount = rs.getInt(1);
					LOGGER.debug("finalCount=" + finalCount);
					break;
				}
				return finalCount;
			}
		});
		LOGGER.debug("QueryTotal_Count Result=" + countResult);

		String result_FinalQueryToExecute = result.getFinalQueryToExecute();
		LOGGER.info("TTT***-Result Data Query to execute:"+(result_FinalQueryToExecute));
		LOGGER.info("TTT***-Query Parameter Map-placeholder:"+result.getParametersMap());
		List<SaleTransaction> tr = namedJDBCTemplate.query(result_FinalQueryToExecute,result.getParametersMap(),new SaleTransactionRowMapper());
		LOGGER.info("TTT***-Count Rows Result {}, Data Query Result {}",countResult,( tr != null ? tr.size() :0 ) );
		if (tr == null) {
			tr = new ArrayList<SaleTransaction>();
		}
		Page<SaleTransaction> list = new PageImpl<SaleTransaction>(tr,page,countResult);
		return list;
	}

	@Override
	public Page<PaymentProcessorRemittance> findRemittanceSaleRefundTransactions(String search,PageRequest page,boolean negate) throws ParseException  {
		LOGGER.info("Executing findRemittanceSaleRefundTransactions, Search  Value {} , Page {}, negate {}",search,page,negate); 
		// Creates the query for the total and for the retrieved data
		String query = getNativeQueryForRemittanceSaleRefund(search,negate);
		LOGGER.debug("Query Prepared="+query);
		CustomQuery queryObj = new CustomQuery(query);
		if ( queryObj != null && page != null ) {
			queryObj.setPagination(true);
			queryObj.setPageSize(page.getPageSize());
			queryObj.setPageNumber(page.getPageNumber());
		}
		query = queryObj.getFinalQueryToExecute();
		queryObj.setPagination(false);
		String queryForCount = queryObj.getFinalQueryToExecute();
		int astrikIndex = queryForCount.indexOf("*");
		if (astrikIndex != -1) {
			String beforeAsktrik = queryForCount.substring(0,astrikIndex);
			String afterAsktrik = queryForCount.substring(astrikIndex+1);
			queryForCount = beforeAsktrik + " COUNT(*) " + afterAsktrik;
		}
		
		LOGGER.info("RRD***-Result Data Query to execute:"+query);
		LOGGER.info("RRD***-Count Query to execute:"+(queryForCount));
		
		// Brings the data and transform it into a Page value list
		@SuppressWarnings("unchecked")
		List<PaymentProcessorRemittance> tr = fetchPaymentProcessorRemittanceCustomMappingResult(query);
		if (tr == null) {
			tr = new ArrayList<PaymentProcessorRemittance>();
		}
		int countResult = jdbcTemplate.queryForObject(queryForCount, Integer.class);
		LOGGER.info("RRD***-Count Rows Result {}, Data Query Result {}",countResult,( tr != null ? tr.size() :0 ) );
		Page<PaymentProcessorRemittance> list = new PageImpl<PaymentProcessorRemittance>(tr, page, countResult);
		return list;
	}
	
	private String getQueryByCriteria(String search,HashMap<String, String> dynamicParametersMap) {
		StringBuilder querySb = new StringBuilder();
		querySb.append(" SELECT * FROM (");

		switch (getTransactionType(search).toLowerCase()) {
		case "void":
			querySb.append(getSelectForVoidTransaction(search,dynamicParametersMap));
			break;
		case "refund":
			querySb.append(getSelectForRefundTransaction(search,dynamicParametersMap));
			break;
		case "all":
			querySb.append(getSelectForSaleTransaction(search,dynamicParametersMap));
			querySb.append(" UNION ");
			querySb.append(getSelectForVoidTransaction(search,dynamicParametersMap));
			querySb.append(" UNION ");
			querySb.append(getSelectForRefundTransaction(search,dynamicParametersMap));
			break;
		case "sale":
		case "tokenize":
		default:
			querySb.append(getSelectForSaleTransaction(search,dynamicParametersMap));
			break;
		}
		querySb.append(" ) RESULTINFO ");

		return querySb.toString();
	}
	
	/**
	 * Creates the select for table SALE_TRANSACTION
	 * 
	 * @param search
	 * @return String with the select of the sale transaction table
	 */
	private String getSelectForSaleTransaction(String search,HashMap<String, String> dynamicParametersMap) {
		StringBuilder querySb = new StringBuilder();

		// create select from transaction type
		querySb.append(
				" SELECT MAINSALE.SaleTransactionID,MAINSALE.TransactionType,MAINSALE.LegalEntityApp,MAINSALE.AccountId,MAINSALE.ApplicationTransactionID,MAINSALE.ProcessorTransactionID,")
				.append("MAINSALE.MerchantID,MAINSALE.TransactionDateTime,MAINSALE.CardNumberFirst6Char,MAINSALE.CardNumberLast4Char,")
				.append("MAINSALE.CardType,MAINSALE.ChargeAmount,MAINSALE.ExpiryDate,MAINSALE.FirstName,MAINSALE.LastName,")
				.append("MAINSALE.Address1,MAINSALE.Address2,MAINSALE.City,MAINSALE.State,MAINSALE.PostalCode,MAINSALE.Country,")
				.append("MAINSALE.TestMode,MAINSALE.Token,MAINSALE.Tokenized,MAINSALE.PaymentProcessorResponseCode,MAINSALE.PaymentProcessorResponseCodeDescription,")
				.append("MAINSALE.ApprovalCode,MAINSALE.InternalResponseCode,MAINSALE.InternalResponseDescription,MAINSALE.InternalStatusCode,")
				.append("MAINSALE.InternalStatusDescription,MAINSALE.PaymentProcessorStatusCode,MAINSALE.PaymentProcessorStatusCodeDescription,")
				.append("MAINSALE.PaymentProcessorRuleId,MAINSALE.RulePaymentProcessorId,MAINSALE.RuleCardType,MAINSALE.RuleMaximumMonthlyAmount,")
				.append("MAINSALE.RuleNoMaximumMonthlyAmountFlag,MAINSALE.RulePriority,MAINSALE.ProcessUser,MAINSALE.Processor,")
				.append("MAINSALE.Application,MAINSALE.Origin,MAINSALE.AccountPeriod,MAINSALE.Desk,MAINSALE.InvoiceNumber,MAINSALE.UserDefinedField1,")
				.append("MAINSALE.UserDefinedField2,MAINSALE.UserDefinedField3,MAINSALE.DateCreated,")
				.append("(SELECT Count(*) FROM Void_Transaction WHERE Saletransactionid = MAINSALE.Saletransactionid AND InternalStatusCode = '1') AS IsVoided,")
				.append("(SELECT Count(*) FROM Refund_Transaction WHERE Saletransactionid = MAINSALE.Saletransactionid AND InternalStatusCode = '1') AS IsRefunded, ")
				.append("MAINSALE.PaymentProcessorInternalStatusCodeID, MAINSALE.PaymentProcessorInternalResponseCodeID, MAINSALE.ReconciliationStatusID, MAINSALE.ReconciliationDate, MAINSALE.BatchUploadID ")
				.append("FROM Sale_Transaction MAINSALE ");

		querySb.append(createWhereStatement(search, "MAINSALE",dynamicParametersMap));

		return querySb.toString();
	}
	
	/**
	 * Creates the WHERE element in the select, it will verify each element in
	 * the search string. Specials cases are taken into account, like
	 * transactionId, this element will create an OR with the attributes
	 * applicationTransactionId and processorTransactionId if found
	 * 
	 * @param search
	 * @param prefix
	 * @return where element that is going to be attached to the select element
	 */
	private String createWhereStatement(String search, String prefix,HashMap<String, String> dynamicParametersMap) {
		StringJoiner statement = new StringJoiner(" AND ");

		if (search != null && !search.isEmpty()) {
			Pattern pattern = Pattern.compile(QueryDSLUtil.SEARCH_REGEX);
			Matcher matcher = pattern.matcher(search + QueryDSLUtil.SEARCH_DELIMITER_CHAR);

			while (matcher.find()) {
				final String attribute = matcher.group(1);
				final String operator = matcher.group(2);
				String value = matcher.group(3);
				String attributeParam = attribute + "Param1";
				String predicate = getPropertyPredicate(attribute);

				if (!prefix.equalsIgnoreCase("MAINSALE") && skipFilter(attribute, prefix)) {
					continue;
				}

				// For payment processor remittance, remittanceCreationDate is
				// not a filter, for these prefixes.
				if (prefix.equals("MAINSALE") || prefix.equals("SALEINNERVOID") || prefix.equals("SALEINNERREFUND")) {
					if (attribute.equalsIgnoreCase("remittanceCreationDate")) {
						continue;
					}
				}

				// Special scenarios, be careful when you change this
				if (attribute.equalsIgnoreCase("processUser")
						&& (prefix.equalsIgnoreCase("REFUND") || prefix.equalsIgnoreCase("VOID"))) {
					// Special case for pUser in VOID and REFUND tables
					predicate = getPropertyPredicate("pUser");
					attributeParam = "pUserParam1";
				} else if (attribute.equalsIgnoreCase("transactionDateTime") || attribute.equalsIgnoreCase("amount")
						|| attribute.equalsIgnoreCase("remittanceCreationDate")) {
					// Specific cases for transactionDateTime, amount
					predicate = predicate.replace(":atributeOperator", getOperation(operator));
					if (dynamicParametersMap.containsKey(attribute + "Param1")) {
						attributeParam = attribute + "Param2";
						predicate = predicate.replace(attribute + "Param1", attributeParam);
					}
				} else if (attribute.equalsIgnoreCase("paymentProcessorId")) {
					if (prefix.equals("MAINSALE") || prefix.equals("REFUND") || prefix.equals("VOID")
							|| prefix.equals("SALEINNERVOID") || prefix.equals("SALEINNERREFUND")) {
						// Processor name, not ID, is used in sale, refund, and
						// void tables.
						attributeParam = attributeParam.replaceAll("paymentProcessorId", "processorName");
						PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(Long.parseLong(value));
						value = ( paymentProcessor != null ? paymentProcessor.getProcessorName() : null);
						predicate = predicate.replace("PaymentProcessorID", "Processor");
						predicate = predicate.replace(attribute, "processorName");
					}
				} else if (attribute.equalsIgnoreCase("paymentFrequency")) {
					// Specific case for paymentFrequency, when paymentFrequency
					// is NOT 'Recurring' then we need to search by all the
					// values except 'Recurring'
					value = getOriginFromPaymentFrequency(value.toLowerCase()).toString().toLowerCase();
				} else if (prefix.equals("ppr") && attribute.equalsIgnoreCase("processorName")) {
					PaymentProcessor paymentProcessor = paymentProcessorDAO.getPaymentProcessorByProcessorName(value);
					Long paymentProcessorId = ( paymentProcessor != null ? paymentProcessor.getPaymentProcessorId() : null );
					value = String.valueOf(paymentProcessorId);
				}

				statement.add(predicate.replace(":prefix", prefix));
				dynamicParametersMap.put(attributeParam, value);
			}
		}
		return statement.length() == 0 ? "" : " WHERE " + statement.toString();

	}
	
	private List<String> getOriginFromPaymentFrequency(String paymentFrequency) {
		LOGGER.info("Fetching Origins PaymentFrequency{} ",paymentFrequency);
		@SuppressWarnings("unchecked")
		List<String> origins =jdbcTemplate.queryForList("SELECT Origin FROM OriginPaymentFrequency_Lookup where PaymentFrequency = lower('"	+ paymentFrequency + "')",String.class);
		return origins;
	}
	
	private class CustomQuery {
		public int getPageNumber() {
			return pageNumber;
		}
		public void setPageNumber(int pageNumber) {
			this.pageNumber = pageNumber;
		}
		public int getPageSize() {
			return pageSize;
		}
		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}
		public boolean isPagination() {
			return pagination;
		}
		public void setPagination(boolean pagination) {
			this.pagination = pagination;
		}
		
		private String queryAsString;
		private Map<String,Object> parametersMap = new HashMap<String,Object>();
		private Sort sort;
		private int pageNumber;
		private int pageSize;
		private boolean pagination;
		
		public Map<String,Object> getParametersMap(){
			return this.parametersMap;
		}
		public String getQueryAsString() {
			return queryAsString;
		}

		public void setQueryAsString(String queryAsString) {
			this.queryAsString = queryAsString;
		}
		
		public CustomQuery(String queryAsStringVal){
			this.queryAsString = queryAsStringVal;
		}
		
		public void setParameter(String paramName,Object paramVal){
			parametersMap.put(paramName, paramVal);
		}
		
		public String getFinalQueryToExecute(){
			String query = this.queryAsString + ( this.sort != null ? addSort(this.sort) : "" );
			if ( isPagination() ) {
				if ( pageSize < 1 ) {
					// in case request param contains page size 0 or negative then use default value = 15
					pageSize = 15;
				}
				if ( pageNumber < 0 ) {
					pageNumber = 0;
				}
				query = query + " LIMIT " + ( pageSize * pageNumber ) + "," + pageSize;
			}
			return query;
		}
		
		public Sort getSort() {
			return sort;
		}
		public void setSort(Sort sort) {
			this.sort = sort;
		}
		
	}
	
	/**
	 * Creates the sort value according with the sort object given
	 * 
	 * @param sort
	 * @return String with the sort for the query
	 */
	private String addSort(Sort sort) {
		if (sort == null) {
			return StringUtils.EMPTY;
		}
		StringBuilder result = new StringBuilder(" ORDER BY ");
		Iterator<Order> list = sort.iterator();
		Order order = null;
		while (list.hasNext()) {
			order = list.next();
			String predicate = getPropertyPredicate(order.getProperty());
			result.append(predicate.substring(predicate.indexOf(":prefix.") + 8, predicate.indexOf(" ")));
			result.append(" ");
			result.append(order.getDirection().toString());
			if (list.hasNext()) {
				result.append(", ");
			} else {
				result.append(" ");
			}
		}
		return result.toString();
	}
	
	public Map<String, CustomQuery> createQueries(String query, PageRequest page,HashMap<String, String> dynamicParametersMap) throws ParseException {
		CustomQuery queryTotal_CustomQuery = new CustomQuery("SELECT COUNT(finalCount.ApplicationTransactionID) FROM (" + query + ") finalCount");
		CustomQuery result_CustomQuery = new CustomQuery(query);
		result_CustomQuery.setSort(page != null ? page.getSort() : null);
		LOGGER.debug("Dynamic Parameters {}", dynamicParametersMap);
		// Sets all parameters to the Query result
		for (Map.Entry<String, String> entry : dynamicParametersMap.entrySet()) {
			if (entry.getKey().contains("amountParam")) {
				result_CustomQuery.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
				queryTotal_CustomQuery.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
			} else if (entry.getKey().contains("transactionDateTimeParam")
					|| (entry.getKey().contains("remittanceCreationDate"))) {
				if (!validFormatDate(entry.getValue())) {
					throw new CustomNotFoundException(
							"Unable to process find transaction, due an error with date formatting");
				}
				// Special case for the dates
				result_CustomQuery.setParameter(entry.getKey(), entry.getValue());
				queryTotal_CustomQuery.setParameter(entry.getKey(), entry.getValue());
			} else if (entry.getKey().contains("legalEntityParam")
					|| entry.getKey().contains("paymentFrequencyParam")) {
				// Special case for legal entity
				String value = entry.getValue().replace("[", "").replace("]", "").replace(" ", "");
				result_CustomQuery.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
				queryTotal_CustomQuery.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
			} else {
				result_CustomQuery.setParameter(entry.getKey(), entry.getValue());
				queryTotal_CustomQuery.setParameter(entry.getKey(), entry.getValue());
			}
		}
		dynamicParametersMap.clear();
		Map<String, CustomQuery> queriesMap = new HashMap<String, CustomQuery>();
		
		queriesMap.put("result", result_CustomQuery);
		queriesMap.put("queryTotal", queryTotal_CustomQuery);
		return queriesMap;
	}
	
	/**
	 * Gives format to the dates
	 * 
	 * @param dateInString
	 * @return date with the right format to be entered in the parameters hash
	 * @throws ParseException
	 */
	private Boolean validFormatDate(String dateInString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return formatter.parse(dateInString) != null;
	}
	/**
	 * Returns a valid operator according with the parameter given, by default
	 * equal character will be returned
	 * 
	 * @param operator
	 * @return string with the operation
	 */
	private String getOperation(String operator) {
		if (operator.equalsIgnoreCase(">")) {
			return GOE;
		}

		if (operator.equalsIgnoreCase("<")) {
			return LOE;
		}

		return EQUALS;
	}
	
	public boolean skipFilter(String attribute, String prefix) {
		// For payment processor remittance, processorName is a filter,
		// so this should not be skipped.
		if ((prefix.equals("st") && attribute.equalsIgnoreCase("processorName"))
				|| (prefix.equals("ppr") && attribute.equalsIgnoreCase("processorName"))) {
			return false;
		}
		// For payment processor remittance, legalEntity and batchUploadId are
		// not a filters.
		if (prefix.equals("ppr")) {
			if (attribute.equalsIgnoreCase("legalEntity") || attribute.equalsIgnoreCase("batchUploadId")) {
				return true;
			}
		}
		if (attribute.equalsIgnoreCase("transactionType")) {
			return true;
		}
		if (prefix.equals("REFUND") || prefix.equals("VOID")) {
			if (attribute.equalsIgnoreCase("accountNumber") || attribute.equalsIgnoreCase("amount")
					|| attribute.equalsIgnoreCase("cardType") || attribute.equalsIgnoreCase("legalEntity")
					|| attribute.equalsIgnoreCase("firstName") || attribute.equalsIgnoreCase("lastName")
					|| attribute.equalsIgnoreCase("accountPeriod") || attribute.equalsIgnoreCase("desk")
					|| attribute.equalsIgnoreCase("invoiceNumber") || attribute.equalsIgnoreCase("paymentFrequency")
					|| attribute.equalsIgnoreCase("reconciliationStatusId")
					|| attribute.equalsIgnoreCase("remittanceCreationDate")
					|| attribute.equalsIgnoreCase("batchUploadId")) {
				return true;
			}
		} else if (attribute.equalsIgnoreCase("transactionId") || attribute.equalsIgnoreCase("internalStatusCode")
				|| attribute.equalsIgnoreCase("transactionDateTime") || attribute.equalsIgnoreCase("processorName")) {
			// This are special cases where we don't need to apply this filters
			// for the inner sale tables, because we will never get the sale
			// transaction
			return true;
		}

		return false;
	}
	
	/**
	 * Gives the name of the element in the entity with the ones in the data
	 * base (native elements)
	 * 
	 * @param property
	 * @return Native name of the element passed by parameter
	 */
	private String getPropertyPredicate(String property) {
		String predicate = predicatesHashMapping.get(property);
		if (predicate == null) {
			LOGGER.error("Property not found, unable to parse {}", property);
			throw new CustomBadRequestException(String.format("Property not found, unable to parse [%s]", property));
		}
		return predicate;
	}
	
	/**
	 * Creates the select for table REFUND_TRANSACTION
	 * 
	 * @param search
	 * @return String with the select of the refund transaction table
	 */
	private String getSelectForRefundTransaction(String search,HashMap<String, String> dynamicParametersMap ) {
		StringBuilder querySb = new StringBuilder();

		querySb.append(
				" SELECT REFUND.SaleTransactionID,'REFUND' AS TransactionType,REFUNDSALE.LegalEntityApp,REFUNDSALE.AccountId,REFUND.ApplicationTransactionID,REFUND.ProcessorTransactionID,")
				.append("REFUND.MerchantID,REFUND.TransactionDateTime,REFUNDSALE.CardNumberFirst6Char,REFUNDSALE.CardNumberLast4Char,")
				.append("REFUNDSALE.CardType,REFUNDSALE.ChargeAmount,REFUNDSALE.ExpiryDate,REFUNDSALE.FirstName,REFUNDSALE.LastName,")
				.append("REFUNDSALE.Address1,REFUNDSALE.Address2,REFUNDSALE.City,REFUNDSALE.State,REFUNDSALE.PostalCode,REFUNDSALE.Country,")
				.append("REFUNDSALE.TestMode,REFUNDSALE.Token,REFUNDSALE.Tokenized,REFUND.PaymentProcessorResponseCode,REFUND.PaymentProcessorResponseCodeDescription,")
				.append("REFUND.ApprovalCode,REFUND.InternalResponseCode,REFUND.InternalResponseDescription,REFUND.InternalStatusCode,")
				.append("REFUND.InternalStatusDescription,REFUND.PaymentProcessorStatusCode,REFUND.PaymentProcessorStatusCodeDescription,")
				.append("REFUNDSALE.PaymentProcessorRuleId,REFUNDSALE.RulePaymentProcessorId,REFUNDSALE.RuleCardType,REFUNDSALE.RuleMaximumMonthlyAmount,")
				.append("REFUNDSALE.RuleNoMaximumMonthlyAmountFlag,REFUNDSALE.RulePriority,REFUND.pUser AS ProcessUser,REFUND.Processor,")
				.append("REFUND.Application,REFUNDSALE.Origin,REFUNDSALE.AccountPeriod,REFUNDSALE.Desk,REFUNDSALE.InvoiceNumber,REFUNDSALE.UserDefinedField1,")
				.append("REFUNDSALE.UserDefinedField2,REFUNDSALE.UserDefinedField3, REFUND.DateCreated, 0 AS IsVoided, 0 AS IsRefunded, ")
				.append("REFUND.PaymentProcessorInternalStatusCodeID, REFUND.PaymentProcessorInternalResponseCodeID, REFUND.ReconciliationStatusID, REFUND.ReconciliationDate, NULL AS BatchUploadID ")
				.append("FROM Refund_Transaction REFUND ")

				.append(" JOIN (")

				.append(" SELECT SALEINNERREFUND.SaleTransactionID,SALEINNERREFUND.TransactionType,SALEINNERREFUND.LegalEntityApp,SALEINNERREFUND.AccountId,SALEINNERREFUND.ApplicationTransactionID,SALEINNERREFUND.ProcessorTransactionID,")
				.append("SALEINNERREFUND.MerchantID,SALEINNERREFUND.TransactionDateTime,SALEINNERREFUND.CardNumberFirst6Char,SALEINNERREFUND.CardNumberLast4Char,")
				.append("SALEINNERREFUND.CardType,SALEINNERREFUND.ChargeAmount,SALEINNERREFUND.ExpiryDate,SALEINNERREFUND.FirstName,SALEINNERREFUND.LastName,")
				.append("SALEINNERREFUND.Address1,SALEINNERREFUND.Address2,SALEINNERREFUND.City,SALEINNERREFUND.State,SALEINNERREFUND.PostalCode,SALEINNERREFUND.Country,")
				.append("SALEINNERREFUND.TestMode,SALEINNERREFUND.Token,SALEINNERREFUND.Tokenized,SALEINNERREFUND.PaymentProcessorResponseCode,SALEINNERREFUND.PaymentProcessorResponseCodeDescription,")
				.append("SALEINNERREFUND.ApprovalCode,SALEINNERREFUND.InternalResponseCode,SALEINNERREFUND.InternalResponseDescription,SALEINNERREFUND.InternalStatusCode,")
				.append("SALEINNERREFUND.InternalStatusDescription,SALEINNERREFUND.PaymentProcessorStatusCode,SALEINNERREFUND.PaymentProcessorStatusCodeDescription,")
				.append("SALEINNERREFUND.PaymentProcessorRuleId,SALEINNERREFUND.RulePaymentProcessorId,SALEINNERREFUND.RuleCardType,SALEINNERREFUND.RuleMaximumMonthlyAmount,")
				.append("SALEINNERREFUND.RuleNoMaximumMonthlyAmountFlag,SALEINNERREFUND.RulePriority,SALEINNERREFUND.ProcessUser,SALEINNERREFUND.Processor,")
				.append("SALEINNERREFUND.Application,SALEINNERREFUND.Origin,SALEINNERREFUND.AccountPeriod,SALEINNERREFUND.Desk,SALEINNERREFUND.InvoiceNumber,SALEINNERREFUND.UserDefinedField1,")
				.append("SALEINNERREFUND.UserDefinedField2,SALEINNERREFUND.UserDefinedField3,SALEINNERREFUND.DateCreated,SALEINNERREFUND.ReconciliationStatusID,SALEINNERREFUND.ReconciliationDate,SALEINNERREFUND.BatchUploadID ")
				.append("FROM Sale_Transaction SALEINNERREFUND ")

				.append(createWhereStatement(search, "SALEINNERREFUND",dynamicParametersMap))
				.append(" ) REFUNDSALE ON (REFUND.saleTransactionID = REFUNDSALE.saleTransactionID) ")
				.append(createWhereStatement(search, "REFUND",dynamicParametersMap));

		return querySb.toString();
	}
	
	/**
	 * Creates the select for table VOID_TRANSACTION
	 * 
	 * @param search
	 * @return String with the select of the void transaction table
	 */
	private String getSelectForVoidTransaction(String search,HashMap<String, String> dynamicParametersMap) {
		StringBuilder querySb = new StringBuilder();

		querySb.append(
				" SELECT VOID.SaleTransactionID,'VOID' AS TransactionType,VOIDSALE.LegalEntityApp,VOIDSALE.AccountId,VOID.ApplicationTransactionID,VOID.ProcessorTransactionID,")
				.append("VOID.MerchantID,VOID.TransactionDateTime,VOIDSALE.CardNumberFirst6Char,VOIDSALE.CardNumberLast4Char,")
				.append("VOIDSALE.CardType,VOIDSALE.ChargeAmount,VOIDSALE.ExpiryDate,VOIDSALE.FirstName,VOIDSALE.LastName,")
				.append("VOIDSALE.Address1,VOIDSALE.Address2,VOIDSALE.City,VOIDSALE.State,VOIDSALE.PostalCode,VOIDSALE.Country,")
				.append("VOIDSALE.TestMode,VOIDSALE.Token,VOIDSALE.Tokenized,VOID.PaymentProcessorResponseCode,VOID.PaymentProcessorResponseCodeDescription,")
				.append("VOID.ApprovalCode,VOID.InternalResponseCode,VOID.InternalResponseDescription,VOID.InternalStatusCode,")
				.append("VOID.InternalStatusDescription,VOID.PaymentProcessorStatusCode,VOID.PaymentProcessorStatusCodeDescription,")
				.append("VOIDSALE.PaymentProcessorRuleId,VOIDSALE.RulePaymentProcessorId,VOIDSALE.RuleCardType,VOIDSALE.RuleMaximumMonthlyAmount,")
				.append("VOIDSALE.RuleNoMaximumMonthlyAmountFlag,VOIDSALE.RulePriority,VOID.pUser AS ProcessUser,VOID.Processor,")
				.append("VOID.Application,VOIDSALE.Origin,VOIDSALE.AccountPeriod,VOIDSALE.Desk,VOIDSALE.InvoiceNumber,VOIDSALE.UserDefinedField1,")
				.append("VOIDSALE.UserDefinedField2,VOIDSALE.UserDefinedField3,VOID.DateCreated, 0 AS IsVoided, 0 AS IsRefunded, ")
				.append("VOID.PaymentProcessorInternalStatusCodeID, VOID.PaymentProcessorInternalResponseCodeID, NULL AS ReconciliationStatusID, CAST(NULL AS DATETIME) AS ReconciliationDate, NULL AS BatchUploadID ")
				.append("FROM Void_Transaction VOID ")

				.append(" JOIN (")

				.append(" SELECT SALEINNERVOID.SaleTransactionID,SALEINNERVOID.TransactionType,SALEINNERVOID.LegalEntityApp,SALEINNERVOID.AccountId,SALEINNERVOID.ApplicationTransactionID,SALEINNERVOID.ProcessorTransactionID,")
				.append("SALEINNERVOID.MerchantID,SALEINNERVOID.TransactionDateTime,SALEINNERVOID.CardNumberFirst6Char,SALEINNERVOID.CardNumberLast4Char,")
				.append("SALEINNERVOID.CardType,SALEINNERVOID.ChargeAmount,SALEINNERVOID.ExpiryDate,SALEINNERVOID.FirstName,SALEINNERVOID.LastName,")
				.append("SALEINNERVOID.Address1,SALEINNERVOID.Address2,SALEINNERVOID.City,SALEINNERVOID.State,SALEINNERVOID.PostalCode,SALEINNERVOID.Country,")
				.append("SALEINNERVOID.TestMode,SALEINNERVOID.Token,SALEINNERVOID.Tokenized,SALEINNERVOID.PaymentProcessorResponseCode,SALEINNERVOID.PaymentProcessorResponseCodeDescription,")
				.append("SALEINNERVOID.ApprovalCode,SALEINNERVOID.InternalResponseCode,SALEINNERVOID.InternalResponseDescription,SALEINNERVOID.InternalStatusCode,")
				.append("SALEINNERVOID.InternalStatusDescription,SALEINNERVOID.PaymentProcessorStatusCode,SALEINNERVOID.PaymentProcessorStatusCodeDescription,")
				.append("SALEINNERVOID.PaymentProcessorRuleId,SALEINNERVOID.RulePaymentProcessorId,SALEINNERVOID.RuleCardType,SALEINNERVOID.RuleMaximumMonthlyAmount,")
				.append("SALEINNERVOID.RuleNoMaximumMonthlyAmountFlag,SALEINNERVOID.RulePriority,SALEINNERVOID.ProcessUser,SALEINNERVOID.Processor,")
				.append("SALEINNERVOID.Application,SALEINNERVOID.Origin,SALEINNERVOID.AccountPeriod,SALEINNERVOID.Desk,SALEINNERVOID.InvoiceNumber,SALEINNERVOID.UserDefinedField1,")
				.append("SALEINNERVOID.UserDefinedField2,SALEINNERVOID.UserDefinedField3,SALEINNERVOID.DateCreated ")
				.append("FROM Sale_Transaction SALEINNERVOID ")

				.append(createWhereStatement(search, "SALEINNERVOID",dynamicParametersMap))
				.append(" ) VOIDSALE ON (VOID.saleTransactionID = VOIDSALE.saleTransactionID) ")
				.append(createWhereStatement(search, "VOID",dynamicParametersMap));
		return querySb.toString();

	}

	
	/**
	 * Reaches for the transaction type element in the search and returns type.
	 * If VOID is in the element transactionType, VOID will be returned, same
	 * case for SALE and REFUND All will returned if this cases are not found
	 * 
	 * @param search
	 * @return type of transaction
	 */
	private String getTransactionType(String search) {
		final String TRANSACTION_TYPE = "(transactionType)(:|<|>)([\\w]+)";

		String transactionType = "ALL";
		Pattern pattern = Pattern.compile(TRANSACTION_TYPE);
		Matcher matcher = pattern.matcher(search + QueryDSLUtil.SEARCH_DELIMITER_CHAR);
		while (matcher.find()) {
			transactionType = matcher.group(3);
		}

		return transactionType;
	}

	private List<PaymentProcessorRemittance> fetchPaymentProcessorRemittanceCustomMappingResult(String query){
		return jdbcTemplate.query( query,	new PaymentProcessorRemittanceRowMapper());
	}
	
	class PaymentProcessorRemittanceRowMapper implements RowMapper<PaymentProcessorRemittance> {

		@Override
		public PaymentProcessorRemittance mapRow(ResultSet rs, int rowNum) throws SQLException {
			PaymentProcessorRemittance record = new PaymentProcessorRemittance();
			record.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
			record.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
			record.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
			record.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
			record.setPaymentMethod(rs.getString("PaymentMethod"));
			record.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
			record.setTransactionType(rs.getString("TransactionType"));
			record.setTransactionTime(new DateTime(rs.getTimestamp("TransactionTime")));
			record.setAccountId(rs.getString("AccountID"));
			record.setApplication(rs.getString("Application"));
			record.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
			record.setMerchantId(rs.getString("MerchantID"));
			record.setTransactionSource(rs.getString("TransactionSource"));
			record.setFirstName(rs.getString("FirstName"));
			record.setLastName(rs.getString("LastName"));
			record.setRemittanceCreationDate(new DateTime(rs.getTimestamp("RemittanceCreationDate")));
			record.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
			record.setProcessorName(rs.getString("ProcessorName"));
			record.setSaleTransactionId(rs.getLong("SaleTransactionID"));
			record.setSaleFirstName(rs.getString("SaleFirstName"));
			record.setSaleLastName(rs.getString("SaleLastName"));
			record.setSaleProcessUser(rs.getString("SaleProcessUser"));
			record.setSaleTransactionType(rs.getString("SaleTransactionType"));
			record.setSaleAddress1(rs.getString("SaleAddress1"));
			record.setSaleAddress2(rs.getString("SaleAddress2"));
			record.setSaleCity(rs.getString("SaleCity"));
			record.setSaleState(rs.getString("SaleState"));
			record.setSalePostalCode(rs.getString("SalePostalCode"));
			record.setSaleCountry(rs.getString("SaleCountry"));
			record.setSaleCardNumberFirst6Char(rs.getString("SaleCardNumberFirst6Char"));
			record.setSaleCardNumberLast4Char(rs.getString("SaleCardNumberLast4Char"));
			record.setSaleCardType(rs.getString("SaleCardType"));
			record.setSaleExpiryDate(rs.getDate("SaleExpiryDate"));
			record.setSaleToken(rs.getString("SaleToken"));
			record.setSaleChargeAmount(rs.getBigDecimal("SaleChargeAmount"));
			record.setSaleLegalEntityApp(rs.getString("SaleLegalEntityApp"));
			record.setSaleAccountId(rs.getString("SaleAccountId"));
			record.setSaleAccountNumber(rs.getString("SaleAccountId"));
			record.setSaleAmount(rs.getBigDecimal("SaleChargeAmount"));
			record.setSaleApplicationTransactionId(rs.getString("SaleApplicationTransactionID"));
			record.setSaleMerchantId(rs.getString("SaleMerchantID"));
			record.setSaleProcessor(rs.getString("SaleProcessor"));
			record.setSaleApplication(rs.getString("SaleApplication"));
			record.setSaleOrigin(rs.getString("SaleOrigin"));
			record.setSaleProcessorTransactionId(rs.getString("SaleProcessorTransactionID"));
			record.setSaleTransactionDateTime(new DateTime(rs.getTimestamp("SaleTransactionDateTime")));
			record.setSaleTestMode(rs.getShort("SaleTestMode"));
			record.setSaleApprovalCode(rs.getString("SaleApprovalCode"));
			record.setSaleTokenized(rs.getShort("SaleTokenized"));
			record.setSalePaymentProcessorStatusCode(rs.getString("SalePaymentProcessorStatusCode"));
			record.setSalePaymentProcessorStatusCodeDescription(rs.getString("SalePaymentProcessorStatusCodeDescription"));
			record.setSalePaymentProcessorResponseCode(rs.getString("SalePaymentProcessorResponseCode"));
			record.setSalePaymentProcessorResponseCodeDescription(rs.getString("SalePaymentProcessorResponseCodeDescription"));
			record.setSaleInternalStatusCode(rs.getString("SaleInternalStatusCode"));
			record.setSaleInternalStatusDescription(rs.getString("SaleInternalStatusDescription"));
			record.setSaleInternalResponseCode(rs.getString("SaleInternalResponseCode"));
			record.setSaleInternalResponseDescription(rs.getString("SaleInternalResponseDescription"));
			record.setSalePaymentProcessorInternalStatusCodeId(rs.getLong("SalePaymentProcessorInternalStatusCodeID"));
			record.setSalePaymentProcessorInternalResponseCodeId(rs.getLong("SalePaymentProcessorInternalResponseCodeID"));
			record.setSaleCreatedDate(new DateTime(rs.getTimestamp("SaleDateCreated")));
			record.setSalePaymentProcessorRuleId(rs.getLong("SalePaymentProcessorRuleID"));
			record.setSaleRulePaymentProcessorId(rs.getLong("SaleRulePaymentProcessorID"));
			record.setSaleRuleCardType(rs.getString("SaleRuleCardType"));
			record.setSaleRuleMaximumMonthlyAmount(rs.getBigDecimal("SaleRuleMaximumMonthlyAmount"));
			record.setSaleRuleNoMaximumMonthlyAmountFlag(rs.getShort("SaleRuleNoMaximumMonthlyAmountFlag"));
			record.setSaleRulePriority(rs.getShort("SaleRulePriority"));
			record.setSaleAccountPeriod(rs.getString("SaleAccountPeriod"));
			record.setSaleDesk(rs.getString("SaleDesk"));
			record.setSaleInvoiceNumber(rs.getString("SaleInvoiceNumber"));
			record.setSaleUserDefinedField1(rs.getString("SaleUserDefinedField1"));
			record.setSaleUserDefinedField2(rs.getString("SaleUserDefinedField2"));
			record.setSaleUserDefinedField3(rs.getString("SaleUserDefinedField3"));
			record.setSaleReconciliationStatusId(rs.getLong("SaleReconciliationStatusID"));
			record.setSaleReconciliationDate(new DateTime(rs.getTimestamp("SaleReconciliationDate")));
			record.setSaleBatchUploadId(rs.getLong("SaleBatchUploadID"));
			record.setSaleIsVoided(rs.getInt("SaleIsVoided"));
			record.setSaleIsRefunded(rs.getInt("SaleIsRefunded"));
			record.setMerchantId(rs.getString("MID"));
			
			record.setProcessor_Name(rs.getString("Processor_Name"));
			record.setReconciliationStatus_ID(rs.getString("ReconciliationStatus_ID"));
			
			/*RemittanceSale obj = new RemittanceSale();
			obj.setPaymentProcessorRemittance(record);*/
			return record;
		} 
	}
	
	class SaleTransactionRowMapper implements RowMapper<SaleTransaction> {

		@Override
		public SaleTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			SaleTransaction record = new SaleTransaction();
			record.setSaleTransactionId(rs.getLong("SaleTransactionID"));
			record.setTransactionType(rs.getString("TransactionType"));
			record.setLegalEntityApp(rs.getString("LegalEntityApp"));
			record.setAccountId(rs.getString("AccountId"));
			record.setApplicationTransactionId(rs.getString("ApplicationTransactionID"));
			record.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
			record.setMerchantId(rs.getString("MerchantID"));
			Timestamp ts = null;
			if (rs.getString("TransactionDateTime") != null) {
				ts = Timestamp.valueOf(rs.getString("TransactionDateTime"));
				record.setTransactionDateTime(new DateTime(ts));
			}
			record.setCardNumberFirst6Char(rs.getString("CardNumberFirst6Char"));
			record.setCardNumberLast4Char(rs.getString("CardNumberLast4Char"));
			
			record.setCardType(rs.getString("CardType"));
			record.setChargeAmount(rs.getBigDecimal("ChargeAmount"));
			record.setExpiryDate(rs.getDate("ExpiryDate"));
			record.setFirstName(rs.getString("FirstName"));
			record.setLastName(rs.getString("LastName"));
			record.setAddress1(rs.getString("Address1"));
			record.setAddress2(rs.getString("Address2"));
			record.setCity(rs.getString("City"));
			record.setState(rs.getString("State"));
			record.setPostalCode(rs.getString("PostalCode"));
			record.setCountry(rs.getString("Country"));
			record.setTestMode(rs.getShort("TestMode"));
			record.setToken(rs.getString("Token"));
			record.setTokenized(rs.getShort("Tokenized"));
			record.setPaymentProcessorResponseCode(rs.getString("PaymentProcessorResponseCode"));
			record.setPaymentProcessorResponseCodeDescription(rs.getString("PaymentProcessorResponseCodeDescription"));
			record.setApprovalCode(rs.getString("ApprovalCode"));
			record.setInternalResponseCode(rs.getString("InternalResponseCode"));
			record.setInternalResponseDescription(rs.getString("InternalResponseDescription"));
			record.setInternalStatusCode(rs.getString("InternalStatusCode"));
			record.setInternalStatusDescription(rs.getString("InternalStatusDescription"));
			record.setPaymentProcessorStatusCode(rs.getString("PaymentProcessorStatusCode"));
			record.setPaymentProcessorStatusCodeDescription(rs.getString("PaymentProcessorStatusCodeDescription"));
			record.setPaymentProcessorRuleId(rs.getLong("PaymentProcessorRuleId"));
			record.setRulePaymentProcessorId(rs.getLong("RulePaymentProcessorId"));
			record.setRuleCardType(rs.getString("RuleCardType"));
			record.setRuleMaximumMonthlyAmount(rs.getBigDecimal("RuleMaximumMonthlyAmount"));
			record.setRuleNoMaximumMonthlyAmountFlag(rs.getShort("RuleNoMaximumMonthlyAmountFlag"));
			record.setRulePriority(rs.getShort("RulePriority"));
			record.setProcessUser(rs.getString("ProcessUser"));
			record.setProcessor(rs.getString("Processor"));
			record.setApplication(rs.getString("Application"));
			record.setOrigin(rs.getString("Origin"));
			record.setAccountPeriod(rs.getString("AccountPeriod"));
			record.setDesk(rs.getString("Desk"));
			record.setInvoiceNumber(rs.getString("InvoiceNumber"));
			record.setUserDefinedField1(rs.getString("UserDefinedField1"));
			record.setUserDefinedField2(rs.getString("UserDefinedField2"));
			record.setUserDefinedField3(rs.getString("UserDefinedField3"));
			record.setDateCreated(new DateTime(rs.getTimestamp("DateCreated")));
			record.setIsVoided(rs.getInt("IsVoided"));
			record.setIsRefunded(rs.getInt("IsRefunded"));
			record.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
			record.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
			record.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
			record.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
			record.setBatchUploadId(rs.getLong("BatchUploadID"));
			return record;
		} 
	}
	
	private PaymentProcessorRemittance fetchPaymentProcessorRemittanceCustomMappingResult_Single(String query){
		PaymentProcessorRemittance obj = jdbcTemplate.query(query,new ResultSetExtractor<PaymentProcessorRemittance>(){

			@Override
			public PaymentProcessorRemittance extractData(ResultSet rs) throws SQLException, DataAccessException {
				PaymentProcessorRemittance record = null;
				while (rs.next()) {
					record = new PaymentProcessorRemittance();
					record.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
					record.setCreatedDate(new DateTime(rs.getTimestamp("DateCreated")));
					record.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
					record.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
					record.setPaymentMethod(rs.getString("PaymentMethod"));
					record.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
					record.setTransactionType(rs.getString("TransactionType"));
					record.setTransactionTime(new DateTime(rs.getTimestamp("TransactionTime")));
					record.setAccountId(rs.getString("AccountID"));
					record.setApplication(rs.getString("Application"));
					record.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
					record.setMerchantId(rs.getString("MerchantID"));
					record.setTransactionSource(rs.getString("TransactionSource"));
					record.setFirstName(rs.getString("FirstName"));
					record.setLastName(rs.getString("LastName"));
					record.setRemittanceCreationDate(new DateTime(rs.getTimestamp("RemittanceCreationDate")));
					record.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
					record.setProcessorName(rs.getString("ProcessorName"));
					record.setSaleTransactionId(rs.getLong("SaleTransactionID"));
					record.setSaleFirstName(rs.getString("SaleFirstName"));
					record.setSaleLastName(rs.getString("SaleLastName"));
					record.setSaleProcessUser(rs.getString("SaleProcessUser"));
					record.setSaleTransactionType(rs.getString("SaleTransactionType"));
					record.setSaleAddress1(rs.getString("SaleAddress1"));
					record.setSaleAddress2(rs.getString("SaleAddress2"));
					record.setSaleCity(rs.getString("SaleCity"));
					record.setSaleState(rs.getString("SaleState"));
					record.setSalePostalCode(rs.getString("SalePostalCode"));
					record.setSaleCountry(rs.getString("SaleCountry"));
					record.setSaleCardNumberFirst6Char(rs.getString("SaleCardNumberFirst6Char"));
					record.setSaleCardNumberLast4Char(rs.getString("SaleCardNumberLast4Char"));
					record.setSaleCardType(rs.getString("SaleCardType"));
					record.setSaleExpiryDate(rs.getDate("SaleExpiryDate"));
					record.setSaleToken(rs.getString("SaleToken"));
					record.setSaleChargeAmount(rs.getBigDecimal("SaleChargeAmount"));
					record.setSaleLegalEntityApp(rs.getString("SaleLegalEntityApp"));
					record.setSaleAccountId(rs.getString("SaleAccountId"));
					record.setSaleApplicationTransactionId(rs.getString("SaleApplicationTransactionID"));
					record.setSaleMerchantId(rs.getString("SaleMerchantID"));
					record.setSaleProcessor(rs.getString("SaleProcessor"));
					record.setSaleApplication(rs.getString("SaleApplication"));
					record.setSaleOrigin(rs.getString("SaleOrigin"));
					record.setSaleProcessorTransactionId(rs.getString("SaleProcessorTransactionID"));
					record.setSaleTransactionDateTime(new DateTime(rs.getTimestamp("SaleTransactionDateTime")));
					record.setSaleTestMode(rs.getShort("SaleTestMode"));
					record.setSaleApprovalCode(rs.getString("SaleApprovalCode"));
					record.setSaleTokenized(rs.getShort("SaleTokenized"));
					record.setSalePaymentProcessorStatusCode(rs.getString("SalePaymentProcessorStatusCode"));
					record.setSalePaymentProcessorStatusCodeDescription(rs.getString("SalePaymentProcessorStatusCodeDescription"));
					record.setSalePaymentProcessorResponseCode(rs.getString("SalePaymentProcessorResponseCode"));
					record.setSalePaymentProcessorResponseCodeDescription(rs.getString("SalePaymentProcessorResponseCodeDescription"));
					record.setSaleInternalStatusCode(rs.getString("SaleInternalStatusCode"));
					record.setSaleInternalStatusDescription(rs.getString("SaleInternalStatusDescription"));
					record.setSaleInternalResponseCode(rs.getString("SaleInternalResponseCode"));
					record.setSaleInternalResponseDescription(rs.getString("SaleInternalResponseDescription"));
					record.setSalePaymentProcessorInternalStatusCodeId(rs.getLong("SalePaymentProcessorInternalStatusCodeID"));
					record.setSalePaymentProcessorInternalResponseCodeId(rs.getLong("SalePaymentProcessorInternalResponseCodeID"));
					record.setSaleCreatedDate(new DateTime(rs.getTimestamp("SaleDateCreated")));
					record.setSalePaymentProcessorRuleId(rs.getLong("SalePaymentProcessorRuleID"));
					record.setSaleRulePaymentProcessorId(rs.getLong("SaleRulePaymentProcessorID"));
					record.setSaleRuleCardType(rs.getString("SaleRuleCardType"));
					record.setSaleRuleMaximumMonthlyAmount(rs.getBigDecimal("SaleRuleMaximumMonthlyAmount"));
					record.setSaleRuleNoMaximumMonthlyAmountFlag(rs.getShort("SaleRuleNoMaximumMonthlyAmountFlag"));
					record.setSaleRulePriority(rs.getShort("SaleRulePriority"));
					record.setSaleAccountPeriod(rs.getString("SaleAccountPeriod"));
					record.setSaleDesk(rs.getString("SaleDesk"));
					record.setSaleInvoiceNumber(rs.getString("SaleInvoiceNumber"));
					record.setSaleUserDefinedField1(rs.getString("SaleUserDefinedField1"));
					record.setSaleUserDefinedField2(rs.getString("SaleUserDefinedField2"));
					record.setSaleUserDefinedField3(rs.getString("SaleUserDefinedField3"));
					record.setSaleReconciliationStatusId(rs.getLong("SaleReconciliationStatusID"));
					record.setSaleReconciliationDate(new DateTime(rs.getTimestamp("SaleReconciliationDate")));
					record.setSaleBatchUploadId(rs.getLong("SaleBatchUploadID"));
					record.setSaleIsVoided(rs.getInt("SaleIsVoided"));
					record.setSaleIsRefunded(rs.getInt("SaleIsRefunded"));
					record.setMerchantId(rs.getString("MID"));
					record.setProcessor_Name(rs.getString("Processor_Name"));
					record.setReconciliationStatus_ID(rs.getString("ReconciliationStatus_ID"));
					break;
				}
				return record;
			}}
		);
		return obj;
	}
	
	/**
	 * Native SQL query for reconciliation screen. The UI passes processorName
	 * as a string. The UI passes merchantId as a list of strings. The UI passes
	 * reconciliationStatusId as an integer, so it's not necessary to lookup the
	 * name in the table.
	 * 
	 * @param search
	 * 
	 * @return SQL query
	 */
	private String getNativeQueryForRemittanceSaleRefund(String search,boolean negate) {

		String remittanceCreationDateBegin = null;
		String remittanceCreationDateEnd = null;
		String processorName = null;
		String[] merchantIdArray = null;
		String reconciliationStatusId = null;
		int anyOtherParamsIndex = search.indexOf("&");
		if (anyOtherParamsIndex != -1) {
			if (anyOtherParamsIndex < search.length()) {
				search = search.substring(0, anyOtherParamsIndex);
			}
		}
		String[] searchArray = search.split("\\$\\$");
		LOGGER.debug("Search Array Values="+ ( Arrays.asList(searchArray) ) );
		for (String parameter : searchArray) {
			if (parameter.startsWith("remittanceCreationDate>")) {
				String[] parameterArray = parameter.split(">");
				remittanceCreationDateBegin = parameterArray[1];
			}
			if (parameter.startsWith("remittanceCreationDate<")) {
				String[] parameterArray = parameter.split("<");
				remittanceCreationDateEnd = parameterArray[1];
			}
			if (parameter.startsWith("processorName")) {
				String[] parameterArray = parameter.split(":");
				processorName = parameterArray[1];
			}
			if (parameter.startsWith("merchantId")) {
				String temp = parameter.replaceAll("merchantId:", "");
				String values = temp.replaceAll("\\[|\\]", "");
				merchantIdArray = values.split(",");
			}
			if (parameter.startsWith("reconciliationStatusId")) {
				String[] parameterArray = parameter.split(":");
				reconciliationStatusId = parameterArray[1];
			}
		}

		StringBuilder querySb = new StringBuilder();
		String testOrProd = propertyDAO.getPropertyValue("TEST_OR_PROD");
		StringBuilder querySbPart1 = new StringBuilder();
		// Get reconciliationStatudId for "Missing from Remit"
		ReconciliationStatus reconciliationStatus = reconciliationStatusDAO.findByReconciliationStatus("Missing from Remit");
		String statusId = reconciliationStatus != null ? String.valueOf(reconciliationStatus.getReconciliationStatusId()) : "";
		querySbPart1.append(getPaymentProcessorRemittanceAndSaleQuery());
		querySbPart1.append("WHERE ppr.RemittanceCreationDate >= '" + remittanceCreationDateBegin + "' ");
		querySbPart1.append("AND ppr.RemittanceCreationDate <= '" + remittanceCreationDateEnd + "' ");
		querySbPart1.append("AND (ppr.TransactionType = 'SALE') ");
		querySbPart1.append("AND (st.TestMode = " + testOrProd + " OR st.TestMode IS NULL) ");
		querySbPart1.append("UNION ");
		querySbPart1.append(getPaymentProcessorRemittanceAndRefundQuery());
		querySbPart1.append("WHERE ppr.RemittanceCreationDate >= '" + remittanceCreationDateBegin + "' ");
		querySbPart1.append("AND ppr.RemittanceCreationDate <= '" + remittanceCreationDateEnd + "' ");
		querySbPart1.append("AND (ppr.TransactionType = 'REFUND') ");
		querySbPart1.append("AND (st1.TestMode = " + testOrProd + " OR st1.TestMode IS NULL) ");
		LOGGER.debug("query (part 1): " + querySbPart1.toString());

		StringBuilder querySbPart2 = new StringBuilder();
		querySbPart2.append("UNION ");
		querySbPart2.append(getSaleQuery());
		querySbPart2.append("WHERE SALE.TransactionDateTime >= DATE_ADD(CAST('" + remittanceCreationDateBegin
				+ "' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -2 DAY) ");
		querySbPart2.append("AND SALE.TransactionDateTime <= DATE_ADD(CAST('" + remittanceCreationDateBegin
				+ "' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -1 DAY) ");
		querySbPart2.append("AND SALE.InternalStatusCode = 1 ");
		querySbPart2.append("AND (SALE.TransactionType = 'SALE') ");
		querySbPart2.append("AND SALE.ReconciliationStatusID = " + statusId + " ");
		querySbPart2.append("UNION ");
		querySbPart2.append(getRefundQuery());
		querySbPart2.append("WHERE REFUND.TransactionDateTime >= DATE_ADD(CAST('" + remittanceCreationDateBegin
				+ "' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -2 DAY) ");
		querySbPart2.append("AND REFUND.TransactionDateTime <= DATE_ADD(CAST('" + remittanceCreationDateBegin
				+ "' AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -1 DAY) ");
		querySbPart2.append("AND REFUND.InternalStatusCode = 1 ");
		querySbPart2.append("AND REFUND.ReconciliationStatusID = " + statusId + " ");
		LOGGER.debug("query (part 2): " + querySbPart2.toString());

		StringBuilder querySbPart3 = new StringBuilder();

		int numberOfFilters = 0;
		if (remittanceCreationDateBegin != null) {
			numberOfFilters++;
		}
		
		if (processorName != null) {
			numberOfFilters++;
		}
		if (merchantIdArray != null) {
			numberOfFilters++;
		}
		if (reconciliationStatusId != null) {
			numberOfFilters++;
		}

		if (numberOfFilters != 0) {
			querySbPart3.append("ReconDate ");
		}
	
		
		StringBuffer afterWhereClauseSB= new StringBuffer();
		if (processorName != null) {
			afterWhereClauseSB.append(" AND  ReconDate.Processor_Name = '" + processorName + "' ");
		}
		if (merchantIdArray != null) {
			afterWhereClauseSB.append(" AND  (ReconDate.MID IN (");
			for (int i = 0; i < merchantIdArray.length; i++) {
				afterWhereClauseSB.append("'" + merchantIdArray[i] + "'");
				if (i != (merchantIdArray.length - 1)) {
					afterWhereClauseSB.append(", ");
				}
			}
			afterWhereClauseSB.append(")) ");
		}
		if (reconciliationStatusId != null) {
			afterWhereClauseSB.append(" AND  ReconDate.ReconciliationStatus_ID = " + reconciliationStatusId + " ");
		}
		afterWhereClauseSB.replace(0, 4, " ");
		
		if(StringUtils.isNotEmpty(afterWhereClauseSB.toString().trim())){
			querySbPart3.append("  Where ");
			querySbPart3.append(afterWhereClauseSB);
			
		}
		
		querySbPart3.append("ORDER BY Processor_Name ASC, MID ASC, ReconciliationStatus_ID ASC");
		LOGGER.debug("query (part 3): " + querySbPart3.toString());

		if (numberOfFilters != 0) {
			querySb.append("SELECT * FROM (");
		}
		querySb.append(querySbPart1);
		querySb.append(querySbPart2);
		if (numberOfFilters != 0) {
			querySb.append(")");
		}
		querySb.append(querySbPart3);
		
		/**
		 *  Currently this is only used if the user selects 'Not Reconcilied' on
		 *  the UI.
		 *  Change to: WHERE ReconciliationID != 'Reconciled'
		 */
		
		if (negate) {
			return querySb.toString().replaceAll("ReconciliationStatus_ID = 1", "ReconciliationStatus_ID != 1");
		}else{
			return querySb.toString();
		}
		
	}

	
	private String getPaymentProcessorRemittanceAndSaleQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append("SELECT ppr.PaymentProcessorRemittanceID,ppr.DateCreated,ppr.ReconciliationStatusID,");
		querySb.append(
				"ppr.ReconciliationDate,ppr.PaymentMethod,ppr.TransactionAmount,ppr.TransactionType,ppr.TransactionTime,");
		querySb.append(
				"ppr.AccountID,ppr.Application AS Application,ppr.ProcessorTransactionID,ppr.MerchantID,ppr.TransactionSource,ppr.FirstName,");
		querySb.append(
				"ppr.LastName,ppr.RemittanceCreationDate,ppr.PaymentProcessorID,ppl.ProcessorName AS ProcessorName,");
		querySb.append(
				"st.SaleTransactionID AS SaleTransactionID,st.FirstName AS SaleFirstName,st.LastName AS SaleLastName,");
		querySb.append(
				"st.ProcessUser AS SaleProcessUser,st.TransactionType AS SaleTransactionType,st.Address1 AS SaleAddress1,");
		querySb.append(
				"st.Address2 AS SaleAddress2,st.City AS SaleCity,st.State AS SaleState,st.PostalCode AS SalePostalCode,");
		querySb.append(
				"st.Country AS SaleCountry,st.CardNumberFirst6Char AS SaleCardNumberFirst6Char,st.CardNumberLast4Char AS SaleCardNumberLast4Char,");
		querySb.append(
				"st.CardType AS SaleCardType,st.ExpiryDate AS SaleExpiryDate,st.Token AS SaleToken,st.ChargeAmount AS SaleChargeAmount,");
		querySb.append(
				"st.LegalEntityApp AS SaleLegalEntityApp,st.AccountId AS SaleAccountId,st.ApplicationTransactionID AS SaleApplicationTransactionID,");
		querySb.append(
				"st.MerchantID AS SaleMerchantID,st.Processor AS SaleProcessor,st.Application AS SaleApplication,st.Origin AS SaleOrigin,");
		querySb.append(
				"st.ProcessorTransactionID AS SaleProcessorTransactionID,st.TransactionDateTime AS SaleTransactionDateTime,st.TestMode AS SaleTestMode,");
		querySb.append(
				"st.ApprovalCode AS SaleApprovalCode,st.Tokenized AS SaleTokenized,st.PaymentProcessorStatusCode AS SalePaymentProcessorStatusCode,");
		querySb.append("st.PaymentProcessorStatusCodeDescription AS SalePaymentProcessorStatusCodeDescription,");
		querySb.append("st.PaymentProcessorResponseCode AS SalePaymentProcessorResponseCode,");
		querySb.append("st.PaymentProcessorResponseCodeDescription AS SalePaymentProcessorResponseCodeDescription,");
		querySb.append(
				"st.InternalStatusCode AS SaleInternalStatusCode,st.InternalStatusDescription AS SaleInternalStatusDescription,");
		querySb.append(
				"st.InternalResponseCode AS SaleInternalResponseCode,st.InternalResponseDescription AS SaleInternalResponseDescription,");
		querySb.append("st.PaymentProcessorInternalStatusCodeID AS SalePaymentProcessorInternalStatusCodeID,");
		querySb.append(
				"st.PaymentProcessorInternalResponseCodeID AS SalePaymentProcessorInternalResponseCodeID,st.DateCreated AS SaleDateCreated,");
		querySb.append(
				"st.PaymentProcessorRuleID AS SalePaymentProcessorRuleID,st.RulePaymentProcessorID AS SaleRulePaymentProcessorID,");
		querySb.append(
				"st.RuleCardType AS SaleRuleCardType,st.RuleMaximumMonthlyAmount AS SaleRuleMaximumMonthlyAmount,");
		querySb.append(
				"st.RuleNoMaximumMonthlyAmountFlag AS SaleRuleNoMaximumMonthlyAmountFlag,st.RulePriority AS SaleRulePriority,");
		querySb.append(
				"st.AccountPeriod AS SaleAccountPeriod,st.Desk AS SaleDesk,st.InvoiceNumber AS SaleInvoiceNumber,");
		querySb.append(
				"st.UserDefinedField1 AS SaleUserDefinedField1,st.UserDefinedField2 AS SaleUserDefinedField2,st.UserDefinedField3 AS SaleUserDefinedField3,");
		querySb.append(
				"st.ReconciliationStatusID AS SaleReconciliationStatusID,st.ReconciliationDate AS SaleReconciliationDate,st.BatchUploadID AS SaleBatchUploadID,");
		querySb.append("0 AS SaleIsVoided,0 AS SaleIsRefunded,");
		querySb.append(
				"ppr.MerchantID AS MID,ppl.ProcessorName AS Processor_Name,ppr.ReconciliationStatusID AS ReconciliationStatus_ID ");
		querySb.append("FROM PaymentProcessor_Remittance ppr ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
		querySb.append("LEFT JOIN Sale_Transaction st ON (ppr.ProcessorTransactionID = st.ProcessorTransactionID) ");
		return querySb.toString();
	}

	private String getPaymentProcessorRemittanceAndRefundQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append(
				"SELECT ppr.PaymentProcessorRemittanceID,ppr.DateCreated,ppr.ReconciliationStatusID,ppr.ReconciliationDate,");
		querySb.append(
				"ppr.PaymentMethod,ppr.TransactionAmount,ppr.TransactionType,ppr.TransactionTime,ppr.AccountID,ppr.Application AS Application,");
		querySb.append(
				"ppr.ProcessorTransactionID,ppr.MerchantID,ppr.TransactionSource,ppr.FirstName,ppr.LastName,ppr.RemittanceCreationDate,");
		querySb.append(
				"ppr.PaymentProcessorID,ppl.ProcessorName AS ProcessorName,rt.SaleTransactionID AS SaleTransactionID,");
		querySb.append(
				"NULL AS SaleFirstName,NULL AS SaleLastName,NULL AS SaleProcessUser,'REFUND' AS SaleTransactionType,NULL AS SaleAddress1,");
		querySb.append(
				"NULL AS SaleAddress2,NULL AS SaleCity,NULL AS SaleState,NULL AS SalePostalCode,NULL AS SaleCountry,NULL AS SaleCardNumberFirst6Char,");
		querySb.append(
				"st1.CardNumberLast4Char AS SaleCardNumberLast4Char,st1.CardType AS SaleCardType,CAST(NULL AS DATETIME) AS SaleExpiryDate,NULL AS SaleToken,st1.ChargeAmount AS SaleChargeAmount,");
		querySb.append(
				"st1.LegalEntityApp AS SaleLegalEntityApp,st1.AccountId AS SaleAccountId,rt.ApplicationTransactionID AS SaleApplicationTransactionID,rt.MerchantID AS SaleMerchantID,");
		querySb.append(
				"rt.Processor AS SaleProcessor,rt.Application AS SaleApplication,NULL AS SaleOrigin,rt.ProcessorTransactionID AS SaleProcessorTransactionID,");
		querySb.append(
				"rt.TransactionDateTime AS SaleTransactionDateTime,NULL AS SaleTestMode,rt.ApprovalCode AS SaleApprovalCode,NULL AS SaleTokenized,");
		querySb.append("rt.PaymentProcessorStatusCode AS SalePaymentProcessorStatusCode,");
		querySb.append("rt.PaymentProcessorStatusCodeDescription AS SalePaymentProcessorStatusCodeDescription,");
		querySb.append("rt.PaymentProcessorResponseCode AS SalePaymentProcessorResponseCode,");
		querySb.append("rt.PaymentProcessorResponseCodeDescription AS SalePaymentProcessorResponseCodeDescription,");
		querySb.append(
				"rt.InternalStatusCode AS SaleInternalStatusCode,rt.InternalStatusDescription AS SaleInternalStatusDescription,");
		querySb.append(
				"rt.InternalResponseCode AS SaleInternalResponseCode,rt.InternalResponseDescription AS SaleInternalResponseDescription,");
		querySb.append("rt.PaymentProcessorInternalStatusCodeID AS SalePaymentProcessorInternalStatusCodeID,");
		querySb.append("rt.PaymentProcessorInternalResponseCodeID AS SalePaymentProcessorInternalResponseCodeID,");
		querySb.append(
				"rt.DateCreated AS SaleDateCreated,NULL AS SalePaymentProcessorRuleID,NULL AS SaleRulePaymentProcessorID,NULL AS SaleRuleCardType,");
		querySb.append(
				"NULL AS SaleRuleMaximumMonthlyAmount,NULL AS SaleRuleNoMaximumMonthlyAmountFlag,NULL AS SaleRulePriority,NULL AS SaleAccountPeriod,");
		querySb.append(
				"NULL AS SaleDesk,NULL AS SaleInvoiceNumber,NULL AS SaleUserDefinedField1,NULL AS SaleUserDefinedField2,NULL AS SaleUserDefinedField3,");
		querySb.append(
				"rt.ReconciliationStatusID AS SaleReconciliationStatusID,rt.ReconciliationDate AS SaleReconciliationDate,NULL AS SaleBatchUploadID,");
		querySb.append("0 AS SaleIsVoided,0 AS SaleIsRefunded,");
		querySb.append(
				"ppr.MerchantID AS MID,ppl.ProcessorName AS Processor_Name,ppr.ReconciliationStatusID AS ReconciliationStatus_ID ");
		querySb.append("FROM PaymentProcessor_Remittance ppr ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
		querySb.append("LEFT JOIN Refund_Transaction rt ON (ppr.ProcessorTransactionID = rt.ProcessorTransactionID) ");
		querySb.append("LEFT JOIN Sale_Transaction st1 ON (rt.SaleTransactionId = st1.SaleTransactionId) ");
		return querySb.toString();
	}

	private String getSaleQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append(
				"SELECT NULL AS PaymentProcessorRemittanceID,NULL AS DateCreated,NULL AS ReconciliationStatusID,NULL AS ReconciliationDate,");
		querySb.append(
				"NULL AS PaymentMethod,NULL AS TransactionAmount,NULL AS TransactionType,NULL AS TransactionTime,NULL AS AccountID,");
		querySb.append(
				"NULL AS Application,NULL AS ProcessorTransactionID,NULL AS MerchantID,NULL AS TransactionSource,NULL AS FirstName,NULL AS LastName,");
		querySb.append(
				"NULL AS RemittanceCreationDate,NULL AS PaymentProcessorID,NULL AS ProcessorName,SALE.SaleTransactionID,SALE.FirstName,SALE.LastName,");
		querySb.append(
				"SALE.ProcessUser,SALE.TransactionType,SALE.Address1,SALE.Address2,SALE.City,SALE.State,SALE.PostalCode,SALE.Country,SALE.CardNumberFirst6Char,");
		querySb.append(
				"SALE.CardNumberLast4Char,SALE.CardType,SALE.ExpiryDate,SALE.Token,SALE.ChargeAmount,SALE.LegalEntityApp,SALE.AccountId,");
		querySb.append(
				"SALE.ApplicationTransactionID,SALE.MerchantID,SALE.Processor AS SaleProcessor,SALE.Application AS Application,SALE.Origin,SALE.ProcessorTransactionID,SALE.TransactionDateTime,");
		querySb.append(
				"SALE.TestMode,SALE.ApprovalCode,SALE.Tokenized,SALE.PaymentProcessorStatusCode,SALE.PaymentProcessorStatusCodeDescription,");
		querySb.append(
				"SALE.PaymentProcessorResponseCode,SALE.PaymentProcessorResponseCodeDescription,SALE.InternalStatusCode,SALE.InternalStatusDescription,");
		querySb.append(
				"SALE.InternalResponseCode,SALE.InternalResponseDescription,SALE.PaymentProcessorInternalStatusCodeID,SALE.PaymentProcessorInternalResponseCodeID,");
		querySb.append(
				"SALE.DateCreated,SALE.PaymentProcessorRuleID,SALE.RulePaymentProcessorID,SALE.RuleCardType,SALE.RuleMaximumMonthlyAmount,");
		querySb.append(
				"SALE.RuleNoMaximumMonthlyAmountFlag,SALE.RulePriority,SALE.AccountPeriod,SALE.Desk,SALE.InvoiceNumber,SALE.UserDefinedField1,");
		querySb.append(
				"SALE.UserDefinedField2,SALE.UserDefinedField3,SALE.ReconciliationStatusID,SALE.ReconciliationDate,SALE.BatchUploadID,");
		querySb.append("0 AS SaleIsVoided,0 AS SaleIsRefunded,");
		querySb.append(
				"SALE.MerchantID AS MID,SALE.Processor AS Processor_Name,SALE.ReconciliationStatusID AS ReconciliationStatus_ID ");
		querySb.append("FROM Sale_Transaction SALE ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (SALE.Processor = ppl.ProcessorName) ");
		return querySb.toString();
	}

	private String getRefundQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append(
				"SELECT NULL AS PaymentProcessorRemittanceID,NULL AS DateCreated,NULL AS ReconciliationStatusID,NULL AS ReconciliationDate,NULL AS PaymentMethod,");
		querySb.append(
				"NULL AS TransactionAmount,NULL AS TransactionType,NULL AS TransactionTime,NULL AS AccountID,NULL AS Application,NULL AS ProcessorTransactionID,");
		querySb.append(
				"NULL AS MerchantID,NULL AS TransactionSource,NULL AS FirstName,NULL AS LastName,NULL AS RemittanceCreationDate,NULL AS PaymentProcessorID,");
		querySb.append(
				"NULL AS ProcessorName,REFUND.SaleTransactionID,NULL AS RefundFirstName,NULL AS RefundLastName,NULL AS RefundProcessUser,'REFUND' AS RefundTransactionType,");
		querySb.append(
				"NULL AS RefundAddress1,NULL AS RefundAddress2,NULL AS RefundCity,NULL AS RefundState,NULL AS RefundPostalCode,NULL AS RefundCountry,");
		querySb.append(
				"NULL AS RefundCardNumberFirst6Char,st2.CardNumberLast4Char AS RefundCardNumberLast4Char,st2.CardType AS RefundCardType,NULL AS RefundExpiryDate,NULL AS RefundToken,");
		querySb.append(
				"st2.ChargeAmount AS RefundChargeAmount,st2.LegalEntityApp AS RefundLegalEntityApp,st2.AccountId AS RefundAccountId,REFUND.ApplicationTransactionID,REFUND.MerchantID,");
		querySb.append(
				"REFUND.Processor AS SaleProcessor,REFUND.Application AS Application,NULL AS RefundOrigin,REFUND.ProcessorTransactionID,REFUND.TransactionDateTime,NULL AS RefundTestMode,");
		querySb.append(
				"REFUND.ApprovalCode,NULL AS RefundTokenized,REFUND.PaymentProcessorStatusCode,REFUND.PaymentProcessorStatusCodeDescription,");
		querySb.append(
				"REFUND.PaymentProcessorResponseCode,REFUND.PaymentProcessorResponseCodeDescription,REFUND.InternalStatusCode,REFUND.InternalStatusDescription,");
		querySb.append(
				"REFUND.InternalResponseCode,REFUND.InternalResponseDescription,REFUND.PaymentProcessorInternalStatusCodeID,REFUND.PaymentProcessorInternalResponseCodeID,");
		querySb.append(
				"REFUND.DateCreated,NULL AS RefundPaymentProcessorRuleID,NULL AS RefundRulePaymentProcessorID,NULL AS RefundRuleCardType,");
		querySb.append(
				"NULL AS RefundRuleMaximumMonthlyAmount,NULL AS RefundRuleNoMaximumMonthlyAmountFlag,NULL AS RefundRulePriority,NULL AS RefundAccountPeriod,");
		querySb.append(
				"NULL AS RefundDesk,NULL AS RefundInvoiceNumber,NULL AS RefundUserDefinedField1,NULL AS RefundUserDefinedField2,NULL AS RefundUserDefinedField3,");
		querySb.append(
				"REFUND.ReconciliationStatusID,REFUND.ReconciliationDate,NULL AS RefundBatchUploadID,0 AS REFUNDIsVoided,0 AS REFUNDIsRefunded,");
		querySb.append(
				"REFUND.MerchantID AS MID,REFUND.Processor AS Processor_Name,REFUND.ReconciliationStatusID AS ReconciliationStatus_ID ");
		querySb.append("FROM Refund_Transaction REFUND ");
		querySb.append("JOIN Sale_Transaction st2 on (REFUND.SaleTransactionId = st2.SaleTransactionId) ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (REFUND.Processor = ppl.ProcessorName) ");
		return querySb.toString();
	}
	
	private String getNativeQueryForRemittanceSaleRefundDetail(String transactionId,
			TransactionTypeCode transactionType, String processorTransactionType) {
		StringBuilder querySb = new StringBuilder();
		switch (transactionType) {
		case REFUND:
			if ("BlueFin".equalsIgnoreCase(processorTransactionType)) {
				querySb.append(getPaymentProcessorRemittanceAndRefundQuery());
				querySb.append("WHERE rt.ApplicationTransactionID = '" + transactionId + "' ");
			} else {
				querySb.append(getPaymentProcessorRemittanceAndRefundQuery());
				querySb.append("WHERE rt.ProcessorTransactionID = '" + transactionId + "' ");
			}
			break;
		case VOID:
			// Type VOID should not be used for remittance.
			break;
		case SALE:
		case TOKENIZE:
		default:
			if ("BlueFin".equalsIgnoreCase(processorTransactionType)) {
				querySb.append(getPaymentProcessorRemittanceAndSaleQuery());
				querySb.append("WHERE st.ApplicationTransactionID = '" + transactionId + "' ");
			} else {
				querySb.append(getPaymentProcessorRemittanceAndSaleQuery());
				querySb.append("WHERE st.ProcessorTransactionID = '" + transactionId + "' ");
			}
		}
		return querySb.toString();
	}
	
}

class PaymentProcessorRemittanceExtractor implements ResultSetExtractor<List<RemittanceSale>> {

	@Override
	public List<RemittanceSale> extractData(ResultSet rs) throws SQLException, DataAccessException {

		ArrayList<RemittanceSale> list = new ArrayList<RemittanceSale>();

		while (rs.next()) {

			RemittanceSale remittanceSale = new RemittanceSale();

			PaymentProcessorRemittance ppr = new PaymentProcessorRemittance();
			ppr.setPaymentProcessorRemittanceId(rs.getLong("PaymentProcessorRemittanceID"));
			Timestamp ts = null;
			if (rs.getString("DateCreated") != null){
				ts = Timestamp.valueOf(rs.getString("DateCreated"));
				ppr.setDateCreated(new DateTime(ts));
			}
			// Mitul overrides this with ReconciliationStatus_ID
			ppr.setReconciliationStatusId(rs.getLong("ReconciliationStatusID"));
			ppr.setReconciliationDate(new DateTime(rs.getTimestamp("ReconciliationDate")));
			ppr.setPaymentMethod(rs.getString("PaymentMethod"));
			ppr.setTransactionAmount(rs.getBigDecimal("TransactionAmount"));
			ppr.setTransactionType(rs.getString("TransactionType"));
			ppr.setTransactionTime(new DateTime(rs.getTimestamp("TransactionTime")));
			ppr.setAccountId(rs.getString("AccountID"));
			ppr.setApplication(rs.getString("Application"));
			ppr.setProcessorTransactionId(rs.getString("ProcessorTransactionID"));
			// Mitul overrides this with MID
			ppr.setMerchantId(rs.getString("MerchantID"));
			ppr.setTransactionSource(rs.getString("TransactionSource"));
			ppr.setFirstName(rs.getString("FirstName"));
			ppr.setLastName(rs.getString("LastName"));
			if (rs.getString("RemittanceCreationDate") != null){
				ts = Timestamp.valueOf(rs.getString("RemittanceCreationDate"));
				ppr.setRemittanceCreationDate(new DateTime(ts));
			}
			ppr.setPaymentProcessorId(rs.getLong("PaymentProcessorID"));
			// Final value (ORDER BY)
			ppr.setMerchantId(rs.getString("MID"));
			// Final value (ORDER BY)
			ppr.setReconciliationStatusId(rs.getLong("ReconciliationStatus_ID"));
			remittanceSale.setPaymentProcessorRemittance(ppr);

			SaleTransaction st = new SaleTransaction();
			// Mitul overrides this with Processor_Name
			st.setProcessor(rs.getString("ProcessorName"));
			st.setSaleTransactionId(rs.getLong("SaleTransactionID"));
			st.setFirstName(rs.getString("SaleFirstName"));
			st.setLastName(rs.getString("SaleLastName"));
			st.setProcessUser(rs.getString("SaleProcessUser"));
			st.setTransactionType(rs.getString("SaleTransactionType"));
			st.setAddress1(rs.getString("SaleAddress1"));
			st.setAddress2(rs.getString("SaleAddress2"));
			st.setCity(rs.getString("SaleCity"));
			st.setState(rs.getString("SaleState"));
			st.setPostalCode(rs.getString("SalePostalCode"));
			st.setCountry(rs.getString("SaleCountry"));
			st.setCardNumberFirst6Char(rs.getString("SaleCardNumberFirst6Char"));
			st.setCardNumberLast4Char(rs.getString("SaleCardNumberLast4Char"));
			st.setCardType(rs.getString("SaleCardType"));
			st.setExpiryDate(rs.getTimestamp("SaleExpiryDate"));
			st.setToken(rs.getString("SaleToken"));
			st.setChargeAmount(rs.getBigDecimal("SaleChargeAmount"));
			st.setLegalEntityApp(rs.getString("SaleLegalEntityApp"));
			st.setAccountId(rs.getString("SaleAccountId"));
			st.setApplicationTransactionId(rs.getString("SaleApplicationTransactionID"));
			st.setMerchantId(rs.getString("SaleMerchantID"));
			st.setProcessor(rs.getString("SaleProcessor"));
			st.setApplication(rs.getString("SaleApplication"));
			st.setOrigin(rs.getString("SaleOrigin"));
			st.setProcessorTransactionId(rs.getString("SaleProcessorTransactionID"));
			st.setTransactionDateTime(new DateTime(rs.getTimestamp("SaleTransactionDateTime")));
			st.setTestMode(rs.getShort("SaleTestMode"));
			st.setApprovalCode(rs.getString("SaleApprovalCode"));
			st.setTokenized(rs.getShort("SaleTokenized"));
			st.setPaymentProcessorStatusCode(rs.getString("SalePaymentProcessorStatusCode"));
			st.setPaymentProcessorStatusCodeDescription(rs.getString("SalePaymentProcessorStatusCodeDescription"));
			st.setPaymentProcessorResponseCode(rs.getString("SalePaymentProcessorResponseCode"));
			st.setPaymentProcessorResponseCodeDescription(rs.getString("SalePaymentProcessorResponseCodeDescription"));
			st.setInternalStatusCode(rs.getString("SaleInternalStatusCode"));
			st.setInternalStatusDescription(rs.getString("SaleInternalStatusDescription"));
			st.setInternalResponseCode(rs.getString("SaleInternalResponseCode"));
			st.setInternalResponseDescription(rs.getString("SaleInternalResponseDescription"));
			st.setPaymentProcessorInternalStatusCodeId(rs.getLong("SalePaymentProcessorInternalStatusCodeID"));
			st.setPaymentProcessorInternalResponseCodeId(rs.getLong("SalePaymentProcessorInternalResponseCodeID"));
			st.setDateCreated(new DateTime(rs.getTimestamp("SaleDateCreated")));
			st.setPaymentProcessorRuleId(rs.getLong("SalePaymentProcessorRuleID"));
			st.setRulePaymentProcessorId(rs.getLong("SaleRulePaymentProcessorID"));
			st.setRuleCardType(rs.getString("SaleRuleCardType"));
			st.setRuleMaximumMonthlyAmount(rs.getBigDecimal("SaleRuleMaximumMonthlyAmount"));
			st.setRuleNoMaximumMonthlyAmountFlag(rs.getShort("SaleRuleNoMaximumMonthlyAmountFlag"));
			st.setRulePriority(rs.getShort("SaleRulePriority"));
			st.setAccountPeriod(rs.getString("SaleAccountPeriod"));
			st.setDesk(rs.getString("SaleDesk"));
			st.setInvoiceNumber(rs.getString("SaleInvoiceNumber"));
			st.setUserDefinedField1(rs.getString("SaleUserDefinedField1"));
			st.setUserDefinedField2(rs.getString("SaleUserDefinedField2"));
			st.setUserDefinedField3(rs.getString("SaleUserDefinedField3"));
			st.setReconciliationStatusId(rs.getLong("SaleReconciliationStatusID"));
			st.setReconciliationDate(new DateTime(rs.getTimestamp("SaleReconciliationDate")));
			st.setBatchUploadId(rs.getLong("SaleBatchUploadID"));
			st.setIsVoided(rs.getInt("SaleIsVoided"));
			st.setIsRefunded(rs.getInt("SaleIsRefunded"));
			// Final value (ORDER BY)
			st.setProcessor(rs.getString("Processor_Name"));
			remittanceSale.setSaleTransaction(st);

			list.add(remittanceSale);
		}

		return list;
	}
}
