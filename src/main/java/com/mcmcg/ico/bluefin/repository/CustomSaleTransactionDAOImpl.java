package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.bindb.service.TransationBinDBDetailsService;
import com.mcmcg.ico.bluefin.mapper.RemittanceSaleRefundRowMapper;
import com.mcmcg.ico.bluefin.model.*;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.TransactionPageImpl;
import com.mcmcg.ico.bluefin.service.util.QueryUtil;
import com.mcmcg.ico.bluefin.util.DateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class CustomSaleTransactionDAOImpl implements CustomSaleTransactionDAO {

    private static final Logger logger = LoggerFactory.getLogger(CustomSaleTransactionDAOImpl.class);

	private static final String EQUALS = " = ";
	private static final String LOE = " <= ";
	private static final String GOE = " >= ";
	private static final String WHERE_COND = "WHERE";

	@Qualifier(BluefinWebPortalConstants.BLUEFIN_WEB_PORTAL_JDBC_TEMPLATE)
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private HashMap<String, String> predicatesHashMapping = new HashMap<>();

	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;

	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;

	@Autowired
	private PropertyDAO propertyDAO;

	@Autowired
	private TransationBinDBDetailsService transationBinDBDetailsService;
	private Set<String> refundOrVoidTypeAttributesFilterNames = new HashSet<>();

	private String clientTimeZone;

	public CustomSaleTransactionDAOImpl(){
		loadSaleTransactionMappings();
		populateRefundOrVoidTypeAttributesFilterNames();
	}

	/**
	 * Loads the predicates mapping the elements in the sale transaction entity
	 */
	private void loadSaleTransactionMappings() {
		logger.info("Loading Predicates");
		predicatesHashMapping.put("saleTransactionId", ":prefix.SaleTransactionID = :saleTransactionIdParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.TRANSACTION_ID,
				"(:prefix.ApplicationTransactionID in (:transactionIdParam1) OR :prefix.ProcessorTransactionID in (:transactionIdParam1))");
		predicatesHashMapping.put("merchantId", ":prefix.MerchantID = :merchantIdParam1");
		predicatesHashMapping.put("transactionType", ":prefix.TransactionType = :transactionTypeParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.PROCESSORNAME, ":prefix.Processor = :processorNameParam1");
		predicatesHashMapping.put("internalStatusCode", ":prefix.InternalStatusCode = :internalStatusCodeParam1");
		predicatesHashMapping.put("internalStatusDescription",
				":prefix.InternalStatusDescription = :internalStatusDescriptionParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.TRANSACTIONDATETIME,
				":prefix.TransactionDateTime :atributeOperator :transactionDateTimeParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.AMOUNT, ":prefix.ChargeAmount :atributeOperator :amountParam1");
		predicatesHashMapping.put("firstName", ":prefix.FirstName LIKE :firstNameParam1");
		predicatesHashMapping.put("lastName", ":prefix.LastName LIKE :lastNameParam1");
		predicatesHashMapping.put("cardType", ":prefix.CardType = :cardTypeParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.LEGALENTITY, ":prefix.LegalEntityApp IN (:legalEntityParam1)");
		predicatesHashMapping.put(BluefinWebPortalConstants.ACCOUNT_NUM, ":prefix.AccountId in (:accountNumberParam1)");
		predicatesHashMapping.put("application", ":prefix.Application = :applicationParam1");
		predicatesHashMapping.put("processUser", ":prefix.ProcessUser = :processUserParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.BATCHUPLOADID, ":prefix.BatchUploadID = :batchUploadIdParam1"); // This is ONLY for sale
		predicatesHashMapping.put("pUser", ":prefix.pUser = :pUserParam1"); // This is ONLY for void and refund
		predicatesHashMapping.put("accountPeriod", ":prefix.AccountPeriod = :accountPeriodParam1");
		predicatesHashMapping.put("desk", ":prefix.Desk = :deskParam1");
		predicatesHashMapping.put("invoiceNumber", ":prefix.InvoiceNumber = :invoiceNumberParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.PAYMENTFREQUENCY, "lower(:prefix.Origin) IN (:paymentFrequencyParam1)");
		// Payment Processor Remittance
		predicatesHashMapping.put(BluefinWebPortalConstants.PAYMENTPROCESSORID, ":prefix.PaymentProcessorID = :paymentProcessorIdParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.PROCESSORNAME, ":prefix.Processor = :processorNameParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.RECONCILIATIONSTATUSID,
				":prefix.ReconciliationStatusID = :reconciliationStatusIdParam1");
		predicatesHashMapping.put(BluefinWebPortalConstants.REMITTANCECREATIONDATEVAL,
				":prefix.RemittanceCreationDate :atributeOperator :remittanceCreationDateParam1");
		predicatesHashMapping.put("processorTransactionId",
				":prefix.ProcessorTransactionID = :processorTransactionIdParam1");
		predicatesHashMapping.put("last4Card",
				":prefix.CardNumberLast4Char = :last4CardParam1");

		logger.info("After populate={}", predicatesHashMapping);
	}

	@Override
	public List<SaleTransaction> findTransactionsReport(String search,Map<String, List<String>> multipleValuesMap) throws ParseException {
		logger.info("Executing findTransactionsReport , Search Value {}",search);
		HashMap<String, Object> dynamicParametersMap = new HashMap<> ();
		String query = getQueryByCriteria(search, multipleValuesMap, dynamicParametersMap);
		logger.info("Dynamic Query {}", query);

		Map<String, CustomQuery> queriesMap = createQueries(query, null,dynamicParametersMap);
		CustomQuery result = queriesMap.get(BluefinWebPortalConstants.RESULT);
		String finalQueryToExecute = result.getFinalQueryToExecute();
		int transactionsReportMaxSize=getIntValue(propertyDAO.getPropertyValue("TRANSACTIONS_REPORT_MAX_SIZE"));
		if (transactionsReportMaxSize > 0) {
			finalQueryToExecute = finalQueryToExecute + BluefinWebPortalConstants.LIMIT + transactionsReportMaxSize;
		}
		logger.info("Query to execute={}",finalQueryToExecute);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		List<SaleTransaction> tr = namedJDBCTemplate.query(finalQueryToExecute,result.getParametersMap(),new SaleTransactionRowMapper());
		try {
			logger.info("Total number of rows={}", tr.size());
		}catch(Exception ex) {
			logger.error("Total number of rows= 0 = 0 Error:{} ",ex.getMessage());
			tr = new ArrayList<>();
		}
		transationBinDBDetailsService.setBinDBDetailsForTransactions(tr);
		return tr;
	}

	private int getIntValue(String valueToConvert){
		try {
			return Integer.parseInt(valueToConvert);
		} catch (NumberFormatException nfe) {
			if (logger.isDebugEnabled()) {
				logger.info("Failed to parse value {}",valueToConvert);
			}
		}
		return -1;
	}

	@Override
	public List<RemittanceSale> findRemittanceSaleRefundTransactionsReport(String search,boolean negate)
			throws ParseException {
		logger.info("Search Value {}",search);
		Map<String,String> queryParameters = new HashMap<>();
		String query = getNativeQueryForRemittanceSaleRefund(search,negate, queryParameters);
		CustomQuery queryObj = new CustomQuery(query);
		query = queryObj.getFinalQueryToExecute();

		int transactionsReportMaxSize = getIntValue(propertyDAO.getPropertyValue("TRANSACTIONS_REPORT_MAX_SIZE"));
		if (transactionsReportMaxSize > 0) {
			query = query + BluefinWebPortalConstants.LIMIT + transactionsReportMaxSize;
		}
		logger.info("RRR***-Result Data Query to execute ={}",query);
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		List<RemittanceSale> tr = namedJDBCTemplate.query(query,queryParameters,new CustomSalePaymentProcessorRemittanceExtractor());
		logger.info("Total number of rows={}", tr != null ? tr.size() : 0);
		return tr;
	}

	@Override
	public PaymentProcessorRemittance findRemittanceSaleRefundTransactionsDetail(String transactionId,
																				 TransactionTypeCode transactionType, String processorTransactionType) throws ParseException {
		logger.debug("Executing findRemittanceSaleRefundTransactionsDetail, Transaction_Id {} , Transaction Type {} , Processor Transaction Type {}", transactionId, transactionType, processorTransactionType);
		String query = getNativeQueryForRemittanceSaleRefundDetail(transactionType, processorTransactionType);
		PaymentProcessorRemittance ppr = null;
		if (query != null && query.length() > 0) {
			logger.debug("Detail Page Query: {}", query);
			ppr = fetchPaymentProcessorRemittanceCustomMappingResultSingle(query, transactionId);
		}
		return ppr;
	}

	@Override
	public Page<SaleTransaction> findTransaction(String search,Map<String, List<String>> multipleValuesMap, PageRequest page, String timeZone) throws ParseException {

		clientTimeZone = timeZone;

		logger.info("Fetching Transactions, Search  Value {} , page{} ",search,page);
		HashMap<String, Object> dynamicParametersMap = new HashMap<> ();
		String query = getQueryByCriteria(search,multipleValuesMap, dynamicParametersMap);
		logger.info(" Query={}", query);
		Map<String, CustomQuery> queriesMap = createQueries(query, page,dynamicParametersMap);
		CustomQuery result = queriesMap.get(BluefinWebPortalConstants.RESULT);
		CustomQuery queryTotal = queriesMap.get("queryTotal");
		int pageNumber = page != null ? page.getPageNumber() : 0;
		int pageSize = page != null ? page.getPageSize() : 0;
		if ( result != null ) {
			result.setPagination(true);
			result.setPageSize(pageSize);
			result.setPageNumber(pageNumber);
		}
		String queryTotalFinalQueryToExecute = queryTotal.getFinalQueryToExecute();
		logger.info("Count Query to execute ={}",queryTotalFinalQueryToExecute);
		// Set the paging for the created select
		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		Integer countResult = namedJDBCTemplate.query(queryTotalFinalQueryToExecute, queryTotal.getParametersMap(), rs->{
				Integer finalCount = null;
				while (rs.next()) {
					finalCount = rs.getInt(1);
					logger.info("finalCount= {}", finalCount);
					if(finalCount >= 0) {
						break;
					}
				}
				return finalCount;
		});
		logger.info("QueryTotal_Count Result={}", countResult);
		Page<SaleTransaction> list;
		if (result != null) {
			String resultFinalQueryToExecute = result.getFinalQueryToExecute();
			logger.info("Result Data Query to execute: ={}",resultFinalQueryToExecute);
			logger.info("Query Parameter Map-placeholder={}",result.getParametersMap());
			List<SaleTransaction> tr = namedJDBCTemplate.query(resultFinalQueryToExecute,result.getParametersMap(),new SaleTransactionRowMapper());
			try {
				logger.info("Count Rows Result {}, Data Query Result {}",countResult,tr.size());
			}catch(Exception ex) {
				logger.error("Count Rows Result 0, Error: {}",ex.getMessage());
				tr = new ArrayList<>();
			}
			list = new TransactionPageImpl<SaleTransaction>(tr,page,countResult,transationBinDBDetailsService.fetchBinDBDetailsForTransactions(tr));
		} else {
			list = new TransactionPageImpl<SaleTransaction>( new ArrayList<>(),page,countResult,null);
		}
		return list;
	}



	@Override
	public Page<PaymentProcessorRemittance> findRemittanceSaleRefundTransactions(String search,PageRequest page,boolean negate) throws ParseException  {
		logger.info("Search  Value {} , Page {}, negate {}",search,page,negate);
		Map<String,String> queryParameters = new HashMap<>();
		// Creates the query for the total and for the retrieved data
		String query = getNativeQueryForRemittanceSaleRefund(search,negate, queryParameters);
		logger.info("Query Prepared={}",query);
		CustomQuery queryObj = new CustomQuery(query);
		if ( page != null ) {
			queryObj.setPagination(true);
			queryObj.setPageSize(page.getPageSize());
			queryObj.setPageNumber(page.getPageNumber());
		} else {
			logger.info("Page object or query object found null");
		}
		query = queryObj.getFinalQueryToExecute();
		queryObj.setPagination(false);
		String queryForCount = queryObj.getFinalQueryToExecute();
		int astrikIndex = queryForCount.indexOf('*');
		if (astrikIndex != -1) {
			String beforeAsktrik = queryForCount.substring(0,astrikIndex);
			String afterAsktrik = queryForCount.substring(astrikIndex+1);
			queryForCount = beforeAsktrik + " COUNT(*) " + afterAsktrik;
		}

		logger.info("Result Data Query to execute={}",query);
		logger.info("Count Query to execute = {}",queryForCount);

		NamedParameterJdbcTemplate namedJDBCTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		// Brings the data and transform it into a Page value list
		List<PaymentProcessorRemittance> tr = fetchPaymentProcessorRemittanceCustomMappingResult(query,queryParameters,namedJDBCTemplate);
		try {

			logger.info("Number of records fetched ={} successfully", tr.size());
		}catch(Exception ex) {
			logger.error("Number of records fetched = 0 Error:{} ",ex.getMessage());
			tr = new ArrayList<>();
		}

		int countResult = namedJDBCTemplate.queryForObject(queryForCount,queryParameters,Integer.class);
		logger.info("Count Rows Result {}, Data Query Result {}",countResult,tr.size());
		return new PageImpl<PaymentProcessorRemittance>(tr, page, countResult);
	}

	private String getQueryByCriteria(String search, Map<String, List<String>> multipleValuesMap, HashMap<String, Object> dynamicParametersMap) {
		StringBuilder querySb = new StringBuilder();
		querySb.append(" SELECT * FROM (");

        switch (getTransactionType(search).toLowerCase()) {
            case "void":
                querySb.append(getSelectForVoidTransaction(search, multipleValuesMap, dynamicParametersMap));
                break;
            case "refund":
                querySb.append(getSelectForRefundTransaction(search, multipleValuesMap, dynamicParametersMap));
                break;
            case "all":
                prepareQueryForAllType(querySb, multipleValuesMap, search, dynamicParametersMap);
                break;
            case "sale":
            case "tokenize":
            default:
                querySb.append(getSelectForSaleTransaction(search, multipleValuesMap, dynamicParametersMap));
                break;
        }
        querySb.append(" ) RESULTINFO ");

		return querySb.toString();
	}

	private void prepareQueryForAllType(StringBuilder querySb, Map<String, List<String>> multipleValuesMap, String search,HashMap<String, Object> dynamicParametersMap){
		querySb.append(getSelectForSaleTransaction(search, multipleValuesMap, dynamicParametersMap));
		querySb.append(BluefinWebPortalConstants.UNION);
		querySb.append(getSelectForVoidTransaction(search, multipleValuesMap, dynamicParametersMap));
		querySb.append(BluefinWebPortalConstants.UNION);
		querySb.append(getSelectForRefundTransaction(search, multipleValuesMap, dynamicParametersMap));
	}

	/**
	 * Creates the select for table SALE_TRANSACTION
	 *
	 * @param search
	 * @return String with the select of the sale transaction table
	 */
	private String getSelectForSaleTransaction(String search, Map<String, List<String>> multipleValuesMap, HashMap<String, Object> dynamicParametersMap) {
		StringBuilder querySb = new StringBuilder();

		// create select from transaction type
		querySb.append(" SELECT MAINSALE.SaleTransactionID,MAINSALE.TransactionType,MAINSALE.LegalEntityApp,MAINSALE.AccountId,MAINSALE.ApplicationTransactionID,MAINSALE.ProcessorTransactionID,")
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
				.append("FROM Sale_Transaction MAINSALE ")
                .append(createWhereStatement(search, multipleValuesMap, BluefinWebPortalConstants.MAINSALE,dynamicParametersMap));


		return querySb.toString();
	}

	private boolean shouldContinue(String attribute,String prefix){
		return validateAndSkipFilter(attribute,prefix) || isRemitanceCreattionDateFilterAllow(attribute,prefix);
	}

	private boolean validateAndSkipFilter(String attribute,String prefix){
		return !BluefinWebPortalConstants.MAINSALE.equalsIgnoreCase(prefix) && skipFilter(attribute, prefix);
	}

	private boolean isRemitanceCreattionDateFilterAllow(String attribute,String prefix){
		return  ( BluefinWebPortalConstants.MAINSALE.equals(prefix) || BluefinWebPortalConstants.SALEINNERVOID.equals(prefix) || BluefinWebPortalConstants.SALEINNERREFUND.equals(prefix) ) && (BluefinWebPortalConstants.REMITTANCECREATIONDATEVAL.equalsIgnoreCase(attribute)) ;
	}

	private boolean isProcessUser(String attribute,String prefix){
		return "processUser".equalsIgnoreCase(attribute)
				&& (BluefinWebPortalConstants.REFUND.equalsIgnoreCase(prefix) || "VOID".equalsIgnoreCase(prefix));
	}

	private boolean isAttributeInBetweenTransactionDateTimeOrAmountOrRemittanceCreationDate(String attribute){
		return BluefinWebPortalConstants.TRANSACTIONDATETIME.equalsIgnoreCase(attribute) || BluefinWebPortalConstants.AMOUNT.equalsIgnoreCase(attribute)
		|| BluefinWebPortalConstants.REMITTANCECREATIONDATEVAL.equalsIgnoreCase(attribute);
	}

	private boolean isPrefixAsSale(String prefix){
		boolean type1 = BluefinWebPortalConstants.MAINSALE.equals(prefix) || BluefinWebPortalConstants.REFUND.equals(prefix) || "VOID".equals(prefix);
		boolean type2 = type1 || BluefinWebPortalConstants.SALEINNERVOID.equals(prefix) || BluefinWebPortalConstants.SALEINNERREFUND.equals(prefix);
		return type1 || type2;
	}

	private boolean isPrefixAndAttributeAsProcessor(String attribute,String prefix){
		return "ppr".equals(prefix) && BluefinWebPortalConstants.PROCESSORNAME.equalsIgnoreCase(attribute);
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
	private String createWhereStatement(String search, Map<String, List<String>> multipleValuesMap, String prefix,HashMap<String, Object> dynamicParametersMap) {
		logger.info("Creating where statement");
		StringJoiner statement = new StringJoiner(" AND ");

        if (StringUtils.isNotEmpty(search)) {
            Pattern pattern = Pattern.compile(QueryUtil.SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + QueryUtil.SEARCH_DELIMITER_CHAR);

            while (matcher.find()) {
                final String attribute = matcher.group(1);
                final String operator = matcher.group(2);
                String value = matcher.group(3);
                String attributeParam = attribute + BluefinWebPortalConstants.PARAM1;
                String predicate = getPropertyPredicate(attribute);

				if ("transactionDateTime".equalsIgnoreCase(attribute)) {
					value = DateTimeUtil.datetimeToUTC(value, clientTimeZone, "yyyy-MM-dd HH:mm:ss");
					logger.info("Value of value: {}", value);
				}

				if (shouldContinue(attribute,prefix)) {
					continue;
				}
				WhereCalValues whereCalValues ;
                whereCalValues = new WhereCalValues(attribute,prefix,value,multipleValuesMap,attributeParam,operator,predicate);
                calculateValues(whereCalValues,dynamicParametersMap);

                statement.add(whereCalValues.getPredicate().replace(":prefix", whereCalValues.getPrefix()));
                dynamicParametersMap.put(whereCalValues.getAttributeParam(), getParametersMapValue(whereCalValues, attribute));
            }
        }
        return prepareStatementWithWhere(statement);
    }

	private Object getParametersMapValue(WhereCalValues whereCalValues, String attribute) {
		logger.info("Getting parameter value");
		Object value;

        if ((BluefinWebPortalConstants.ACCOUNT_NUM.equals(attribute) || BluefinWebPortalConstants.TRANSACTION_ID.equals(attribute))
                && !whereCalValues.getMultipleValuesMap().isEmpty()) {
            value = whereCalValues.getMultipleValuesMap().get(attribute);
        } else {
            value = whereCalValues.getValue();
        }

        return value;
    }

	private class WhereCalValues {
		String attribute;
		String prefix;
		String value;
		Map<String, List<String>> multipleValuesMap;
		String attributeParam;
		String operator;
		String predicate;
		private WhereCalValues(String attribute,String prefix,String value,Map<String, List<String>> multipleValuesMap,String attributeParam,String operator,String predicate){
			// Default Constructor
			this.attribute = attribute;
			this.prefix = prefix;
			this.value = value;
			this.multipleValuesMap = multipleValuesMap;
			this.attributeParam = attributeParam;
			this.operator = operator;
			this.predicate = predicate;
		}

		public String getAttribute() {
			return attribute;
		}

		public String getPrefix() {
			return prefix;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public Map<String, List<String>> getMultipleValuesMap() {
			return multipleValuesMap;
		}
		public String getAttributeParam() {
			return attributeParam;
		}
		public void setAttributeParam(String attributeParam) {
			this.attributeParam = attributeParam;
		}
		public String getOperator() {
			return operator;
		}
		public String getPredicate() {
			return predicate;
		}
		public void setPredicate(String predicate) {
			this.predicate = predicate;
		}



	}
	private void calculateValues(WhereCalValues whereCalValues,HashMap<String, Object> dynamicParametersMap){
		// Special scenarios, be careful when you change this
		if (isProcessUser(whereCalValues.getAttribute(),whereCalValues.getPrefix())) {
			// Special case for pUser in VOID and REFUND tables
			whereCalValues.setPredicate(getPropertyPredicate("pUser"));
			whereCalValues.setAttributeParam("pUserParam1");
		} else if (isAttributeInBetweenTransactionDateTimeOrAmountOrRemittanceCreationDate(whereCalValues.getAttribute())) {
			// Specific cases for transactionDateTime, amount
			whereCalValues.setPredicate(whereCalValues.getPredicate().replace(":atributeOperator", getOperation(whereCalValues.getOperator())));
			if (dynamicParametersMap.containsKey(whereCalValues.getAttribute() + BluefinWebPortalConstants.PARAM1)) {
				whereCalValues.setAttributeParam(whereCalValues.getAttribute() + "Param2");
				whereCalValues.setPredicate( whereCalValues.getPredicate().replace(whereCalValues.getAttribute() + BluefinWebPortalConstants.PARAM1, whereCalValues.getAttributeParam()));
			}
		} else if (BluefinWebPortalConstants.PAYMENTPROCESSORID.equalsIgnoreCase(whereCalValues.getAttribute()) && isPrefixAsSale(whereCalValues.getPrefix())) {
				// Processor name, not ID, is used in sale, refund, and
				// void tables.
			whereCalValues.setAttributeParam(whereCalValues.getAttributeParam().replace(BluefinWebPortalConstants.PAYMENTPROCESSORID, BluefinWebPortalConstants.PROCESSORNAME) );
			whereCalValues.setValue(getPaymentProcessorName(whereCalValues.getValue()));
			whereCalValues.setPredicate(whereCalValues.getPredicate().replace(BluefinWebPortalConstants.PAYMENTPROCESSORIDVAL, "Processor"));
			whereCalValues.setPredicate(whereCalValues.getPredicate().replace(whereCalValues.getAttribute(), BluefinWebPortalConstants.PROCESSORNAME));
		} else if (BluefinWebPortalConstants.PAYMENTFREQUENCY.equalsIgnoreCase(whereCalValues.getAttribute())) {
			// Specific case for paymentFrequency, when paymentFrequency
			// is NOT 'Recurring' then we need to search by all the
			// values except 'Recurring'
			whereCalValues.setValue(getOriginFromPaymentFrequency(whereCalValues.getValue().toLowerCase()).toString().toLowerCase());
		} else if (isPrefixAndAttributeAsProcessor(whereCalValues.getAttribute(),whereCalValues.getPrefix())) {
			whereCalValues.setValue(getPaymentProcessorId(whereCalValues.getValue()));
		}
	}
	private String getPaymentProcessorName(String value){
		PaymentProcessor paymentProcessor = paymentProcessorDAO.findByPaymentProcessorId(Long.parseLong(value));
		return paymentProcessor != null ? paymentProcessor.getProcessorName() : null;
	}

	private String getPaymentProcessorId(String value){
		PaymentProcessor paymentProcessor = paymentProcessorDAO.getPaymentProcessorByProcessorName(value);
		Long paymentProcessorId = paymentProcessor != null ? paymentProcessor.getPaymentProcessorId() : null;
		return String.valueOf(paymentProcessorId);
	}

	private String prepareStatementWithWhere(StringJoiner statement){
		return statement.length() == 0 ? "" : " WHERE " + statement.toString();
	}

	private List<String> getOriginFromPaymentFrequency(String paymentFrequency) {
		logger.info("Fetching Origins PaymentFrequency{} ",paymentFrequency);
		String query = "SELECT Origin FROM OriginPaymentFrequency_Lookup where PaymentFrequency = lower('"	+ paymentFrequency + "')";
		return jdbcTemplate.queryForList(query,String.class);
	}

	private class CustomQuery {

		private String queryAsString;
		private Map<String,Object> parametersMap = new HashMap<>();
		private Sort sort;
		private int pageNumber;
		private int pageSize;
		private boolean pagination;

		public CustomQuery(String queryAsStringVal){
			this.queryAsString = queryAsStringVal;
		}

		public void setPageNumber(int pageNumber) {
			this.pageNumber = pageNumber;
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

		public Map<String,Object> getParametersMap(){
			return this.parametersMap;
		}
		public void setParameter(String paramName,Object paramVal){
			parametersMap.put(paramName, paramVal);
		}

		public String getFinalQueryToExecute(){
			String query = this.queryAsString + ( this.sort != null ? addSort(this.sort) : "" );
			if ( isPagination() ) {
				if ( pageSize < 1 ) {
					// in case request parameter contains page size 0 or negative then use default value = 15
					pageSize = 15;
				}
				if ( pageNumber < 0 ) {
					pageNumber = 0;
				}
				query = query + BluefinWebPortalConstants.LIMIT + ( pageSize * pageNumber ) + "," + pageSize;
			}
			return query;
		}

		public void setSort(Sort sort) {
			this.sort = sort;
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
			while (list.hasNext()) {
				Order order = list.next();
				String predicate = getPropertyPredicate(order.getProperty());
				result.append(predicate.substring(predicate.indexOf(":prefix.") + 8, predicate.indexOf(' ')));
				result.append(" ");
				result.append(order.getDirection().toString());
				if (list.hasNext()) {
					result.append(", ");
				} else {
					result.append(" ");
				}
			}
			String query = result.toString();
			logger.info("result ={} ",query);
			return query;
		}
	}

	public Map<String, CustomQuery> createQueries(String query, PageRequest page,Map<String, Object> dynamicParametersMap) throws ParseException {
		CustomQuery queryTotalCustomQuery = new CustomQuery("SELECT COUNT(finalCount.ApplicationTransactionID) FROM (" + query + ") finalCount");
		CustomQuery resultCustomQuery = new CustomQuery(query);
		resultCustomQuery.setSort(page != null ? page.getSort() : null);
		logger.info("Dynamic Parameters {}", dynamicParametersMap);
		// Sets all parameters to the Query result
		for (Map.Entry<String, Object> entry : dynamicParametersMap.entrySet()) {
			if (entry.getKey().contains("amountParam")) {
				resultCustomQuery.setParameter(entry.getKey(), new BigDecimal((String)entry.getValue()));
				queryTotalCustomQuery.setParameter(entry.getKey(), new BigDecimal((String)entry.getValue()));
			} else if (entry.getKey().contains("transactionDateTimeParam")
					|| (entry.getKey().contains(BluefinWebPortalConstants.REMITTANCECREATIONDATEVAL))) {
				boolean validFormat = validFormatDate((String)entry.getValue());
				if (!validFormat) {
					throw new CustomNotFoundException(
							"Unable to process find transaction, due an error with date formatting");
				}
				// Special case for the dates
				resultCustomQuery.setParameter(entry.getKey(), entry.getValue());
				queryTotalCustomQuery.setParameter(entry.getKey(), entry.getValue());
			} else if (entry.getKey().contains("legalEntityParam")
					|| entry.getKey().contains("paymentFrequencyParam")) {
				// Special case for legal entity
				String value = ((String)entry.getValue()).replace("[", "").replace("]", "").replace(" ", "");
				resultCustomQuery.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
				queryTotalCustomQuery.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
			} else {
				resultCustomQuery.setParameter(entry.getKey(), entry.getValue());
				queryTotalCustomQuery.setParameter(entry.getKey(), entry.getValue());
			}
		}
		dynamicParametersMap.clear();
		Map<String, CustomQuery> queriesMap = new HashMap<>();

		queriesMap.put(BluefinWebPortalConstants.RESULT, resultCustomQuery);
		queriesMap.put("queryTotal", queryTotalCustomQuery);
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
		if (">".equalsIgnoreCase(operator)) {
			return GOE;
		}

		if ("<".equalsIgnoreCase(operator)) {
			return LOE;
		}

		return EQUALS;
	}

	public boolean skipFilter(String attribute, String prefix) {
		// For payment processor remittance, processorName is a filter,
		// so this should not be skipped.
		if (validateProcessorName(attribute, prefix)) {
			return false;
		}
		// For payment processor remittance, legalEntity and batchUploadId are
		// not a filters.
		if (validateLegalEntityOrBatchUploadOrTransactionType(attribute, prefix)) {
			return true;
		}
		return validateOtherCases(attribute, prefix);
	}

	private boolean validateOtherCases(String attribute, String prefix){
		if (BluefinWebPortalConstants.REFUND.equals(prefix) || "VOID".equals(prefix)) {
			if (refundOrVoidTypeAttributesFilterNames.contains(StringUtils.upperCase(attribute))) {
				return true;
			}
		} else if (validateOtherAttributes(attribute)) {
			// This are special cases where we don't need to apply this filters
			// for the inner sale tables, because we will never get the sale
			// transaction
			return true;
		}
		return false;
	}

	private boolean validateOtherAttributes(String attribute){
		return "transactionId".equalsIgnoreCase(attribute) || "internalStatusCode".equalsIgnoreCase(attribute)
		|| BluefinWebPortalConstants.TRANSACTIONDATETIME.equalsIgnoreCase(attribute) || BluefinWebPortalConstants.PROCESSORNAME.equalsIgnoreCase(attribute);
	}

	private boolean validateLegalEntityOrBatchUploadOrTransactionType(String attribute, String prefix){
		if (validateLegalEntityName(attribute, prefix)) {
			return true;
		}
		if (validateBatchUpload(attribute, prefix)) {
			return true;
		}
		return validateTransactionType(attribute);
		/**if (validateTransactionType(attribute)) {
			return true;
		}
		return false;*/
	}

	private boolean validateTransactionType(String attribute){
		return "transactionType".equalsIgnoreCase(attribute);
	}

	private boolean validateProcessorName(String attribute, String prefix){
		return ("st".equals(prefix) && BluefinWebPortalConstants.PROCESSORNAME.equalsIgnoreCase(attribute))
				|| ("ppr".equals(prefix) && BluefinWebPortalConstants.PROCESSORNAME.equalsIgnoreCase(attribute));
	}

	private boolean validateLegalEntityName(String attribute, String prefix){
		return "ppr".equals(prefix) && BluefinWebPortalConstants.LEGALENTITY.equalsIgnoreCase(attribute);
	}

	private boolean validateBatchUpload(String attribute, String prefix){
		return "ppr".equals(prefix) && BluefinWebPortalConstants.BATCHUPLOADID.equalsIgnoreCase(attribute);
	}

	private void populateRefundOrVoidTypeAttributesFilterNames(){
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.ACCOUNT_NUM));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.AMOUNT));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("cardType"));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.LEGALENTITY));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("firstName"));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("lastName"));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("accountPeriod"));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("desk"));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("invoiceNumber"));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.PAYMENTFREQUENCY));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.RECONCILIATIONSTATUSID));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.REMITTANCECREATIONDATEVAL));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase(BluefinWebPortalConstants.BATCHUPLOADID));
		refundOrVoidTypeAttributesFilterNames.add(StringUtils.upperCase("last4Card"));
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
			logger.error("Property not found, unable to parse {}", property);
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
	private String getSelectForRefundTransaction(String search, Map<String, List<String>> multipleValuesMap, HashMap<String, Object> dynamicParametersMap ) {
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

                .append(createWhereStatement(search, multipleValuesMap, BluefinWebPortalConstants.SALEINNERREFUND, dynamicParametersMap))
                .append(" ) REFUNDSALE ON (REFUND.saleTransactionID = REFUNDSALE.saleTransactionID) ")
                .append(createWhereStatement(search, multipleValuesMap, BluefinWebPortalConstants.REFUND, dynamicParametersMap));

		return querySb.toString();
	}

	/**
	 * Creates the select for table VOID_TRANSACTION
	 *
	 * @param search
	 * @return String with the select of the void transaction table
	 */
	private String getSelectForVoidTransaction(String search, Map<String, List<String>> multipleValuesMap, HashMap<String, Object> dynamicParametersMap) {
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
                .append(createWhereStatement(search, multipleValuesMap, BluefinWebPortalConstants.SALEINNERVOID, dynamicParametersMap))
                .append(" ) VOIDSALE ON (VOID.saleTransactionID = VOIDSALE.saleTransactionID) ")
                .append(createWhereStatement(search, multipleValuesMap, "VOID", dynamicParametersMap));
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
		final String transactionType = "(transactionType)(:|<|>)([\\w]+)";

        String transactionTypeAll = "ALL";
        Pattern pattern = Pattern.compile(transactionType);
        Matcher matcher = pattern.matcher(search + QueryUtil.SEARCH_DELIMITER_CHAR);
        while (matcher.find()) {
            transactionTypeAll = matcher.group(3);
        }

        return transactionTypeAll;
    }

	private List<PaymentProcessorRemittance> fetchPaymentProcessorRemittanceCustomMappingResult(String query, Map<String,String> queryParameters, NamedParameterJdbcTemplate namedJDBCTemplate){
		return namedJDBCTemplate.query(query,queryParameters,new RemittanceSaleRefundRowMapper());
	}

	class PaymentProcessorRemittanceRowMapper implements RowMapper<PaymentProcessorRemittance> {

		@Override
		public PaymentProcessorRemittance mapRow(ResultSet rs, int rowNum) throws SQLException {
			return prepareRecordPaymentProcessorRemittance(rs,true);
		}

		private PaymentProcessorRemittance prepareRecordPaymentProcessorRemittance(ResultSet rs,boolean readAll) throws SQLException {
			PaymentProcessorRemittance record = new PaymentProcessorRemittance();
			record.setPaymentProcessorRemittanceId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORREMITTANCEID));
			record.setCreatedDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.DATECREATED)));
			record.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL));
			record.setReconciliationDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.RECONCILIATIONDATE)));
			record.setPaymentMethod(rs.getString(BluefinWebPortalConstants.PAYMENTMETHOD));
			record.setTransactionAmount(rs.getBigDecimal(BluefinWebPortalConstants.TRANSACTIONAMOUNT));
			record.setTransactionType(rs.getString(BluefinWebPortalConstants.TRANSACTIONTYPE));
			record.setTransactionTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.TRANSACTIONTIME)));
			record.setAccountId(rs.getString(BluefinWebPortalConstants.ACCOUNTIDVAL));
			record.setApplication(rs.getString(BluefinWebPortalConstants.APPLICATION));
			record.setProcessorTransactionId(rs.getString(BluefinWebPortalConstants.PROCESSORTRANSACTIONID));
			record.setMerchantId(rs.getString(BluefinWebPortalConstants.MERCHANTID));
			record.setTransactionSource(rs.getString(BluefinWebPortalConstants.TRANSACTIONSOURCE));
			record.setFirstName(rs.getString(BluefinWebPortalConstants.FIRSTNAME));
			record.setLastName(rs.getString(BluefinWebPortalConstants.LASTNAME));
			record.setRemittanceCreationDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.REMITTANCECREATIONDATE)));
			record.setPaymentProcessorId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORIDVAL));
			record.setProcessorName(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL));
			record.setSaleTransactionId(rs.getLong(BluefinWebPortalConstants.SALETRANSACTIONID));
			record.setSaleFirstName(rs.getString(BluefinWebPortalConstants.SALEFIRSTNAME));
			record.setSaleLastName(rs.getString(BluefinWebPortalConstants.SALELASTNAME));
			record.setSaleProcessUser(rs.getString(BluefinWebPortalConstants.SALEPROCESSUSER));
			record.setSaleTransactionType(rs.getString(BluefinWebPortalConstants.SALETRANSACTIONTYPE));
			record.setSaleAddress1(rs.getString(BluefinWebPortalConstants.SALEADDRESS1));
			record.setSaleAddress2(rs.getString(BluefinWebPortalConstants.SALEADDRESS2));
			record.setSaleCity(rs.getString(BluefinWebPortalConstants.SALECITY));
			record.setSaleState(rs.getString(BluefinWebPortalConstants.SALESTATE));
			record.setSalePostalCode(rs.getString(BluefinWebPortalConstants.SALEPOSTALCODE));
			record.setSaleCountry(rs.getString(BluefinWebPortalConstants.SALECOUNTRY));
			record.setSaleCardNumberFirst6Char(rs.getString(BluefinWebPortalConstants.SALECARDNUMBERFIRST6CHAR));
			record.setSaleCardNumberLast4Char(rs.getString(BluefinWebPortalConstants.SALECARDNUMBERLAST4CHAR));
			record.setSaleCardType(rs.getString(BluefinWebPortalConstants.SALECARDTYPE));
			record.setSaleExpiryDate(rs.getDate(BluefinWebPortalConstants.SALEEXPIRYDATE));
			record.setSaleToken(rs.getString(BluefinWebPortalConstants.SALETOKEN));
			record.setSaleChargeAmount(rs.getBigDecimal(BluefinWebPortalConstants.SALECHARGEAMOUNT));
			record.setSaleLegalEntityApp(rs.getString(BluefinWebPortalConstants.SALELEGALENTITYAPP));
			record.setSaleAccountId(rs.getString(BluefinWebPortalConstants.SALEACCOUNTID));
			if (readAll) {
				record.setSaleAccountNumber(rs.getString(BluefinWebPortalConstants.SALEACCOUNTID));
				record.setSaleAmount(rs.getBigDecimal(BluefinWebPortalConstants.SALECHARGEAMOUNT));
			}
			record.setSaleApplicationTransactionId(rs.getString(BluefinWebPortalConstants.SALEAPPLICATIONTRANSACTIONID));
			record.setSaleMerchantId(rs.getString(BluefinWebPortalConstants.SALEMERCHANTID));
			record.setSaleProcessor(rs.getString(BluefinWebPortalConstants.SALEPROCESSOR));
			record.setSaleProcessorName(record.getSaleProcessor());
			record.setSaleApplication(rs.getString(BluefinWebPortalConstants.SALEAPPLICATION));
			record.setSaleOrigin(rs.getString(BluefinWebPortalConstants.SALEORIGIN));
			record.setSaleProcessorTransactionId(rs.getString(BluefinWebPortalConstants.SALEPROCESSORTRANSACTIONID));
			record.setSaleTransactionDateTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.SALETRANSACTIONDATETIME)));
			record.setSaleTestMode(rs.getShort(BluefinWebPortalConstants.SALETESTMODE));
			record.setSaleApprovalCode(rs.getString(BluefinWebPortalConstants.SALEAPPROVALCODE));
			record.setSaleTokenized(rs.getShort(BluefinWebPortalConstants.SALETOKENIZED));
			record.setSalePaymentProcessorStatusCode(rs.getString(BluefinWebPortalConstants.SALEPAYMENTPROCESSORSTATUSCODE));
			record.setSalePaymentProcessorStatusCodeDescription(rs.getString(BluefinWebPortalConstants.SALEPAYMENTPROCESSORSTATUSCODEDESCRIPTION));
			record.setSalePaymentProcessorResponseCode(rs.getString(BluefinWebPortalConstants.SALEPAYMENTPROCESSORRESPONSECODE));
			record.setSalePaymentProcessorResponseCodeDescription(rs.getString(BluefinWebPortalConstants.SALEPAYMENTPROCESSORRESPONSECODEDESCRIPTION));
			record.setSaleInternalStatusCode(rs.getString(BluefinWebPortalConstants.SALEINTERNALSTATUSCODE));
			record.setSaleInternalStatusDescription(rs.getString(BluefinWebPortalConstants.SALEINTERNALSTATUSCODEDESCRIPTION));
			record.setSaleInternalResponseCode(rs.getString(BluefinWebPortalConstants.SALEINTERNALRESPONSECODE));
			record.setSaleInternalResponseDescription(rs.getString(BluefinWebPortalConstants.SALEINTERNALRESPONSECODEDESCRIPTION));
			record.setSalePaymentProcessorInternalStatusCodeId(rs.getLong(BluefinWebPortalConstants.SALEPAYMENTPROCESSORINTERNALSTATUSCODEID));
			record.setSalePaymentProcessorInternalResponseCodeId(rs.getLong(BluefinWebPortalConstants.SALEPAYMENTPROCESSORINTERNALRESPONSECODEID));
			record.setSaleCreatedDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.SALEDATECREATED)));
			record.setSalePaymentProcessorRuleId(rs.getLong(BluefinWebPortalConstants.SALEPAYMENTPROCESSORRULEID));
			record.setSaleRulePaymentProcessorId(rs.getLong(BluefinWebPortalConstants.SALERULEPAYMENTPROCESSORID));
			record.setSaleRuleCardType(rs.getString(BluefinWebPortalConstants.SALERULECARDTYPE));
			record.setSaleRuleMaximumMonthlyAmount(rs.getBigDecimal(BluefinWebPortalConstants.SALERULEMAXIMUMMONTHLYAMOUNT));
			record.setSaleRuleNoMaximumMonthlyAmountFlag(rs.getShort(BluefinWebPortalConstants.SALERULEMAXIMUMMONTHLYAMOUNTFLAG));
			record.setSaleRulePriority(rs.getShort(BluefinWebPortalConstants.SALERULEPRIORITY));
			record.setSaleAccountPeriod(rs.getString(BluefinWebPortalConstants.SALEACCOUNTPERIOD));
			record.setSaleDesk(rs.getString(BluefinWebPortalConstants.SALEDESK));
			record.setSaleInvoiceNumber(rs.getString(BluefinWebPortalConstants.SALEINVOICENUMBER));
			record.setSaleUserDefinedField1(rs.getString(BluefinWebPortalConstants.SALEUSERDEFINEDFIELD1));
			record.setSaleUserDefinedField2(rs.getString(BluefinWebPortalConstants.SALEUSERDEFINEDFIELD2));
			record.setSaleUserDefinedField3(rs.getString(BluefinWebPortalConstants.SALEUSERDEFINEDFIELD3));
			record.setSaleReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.SALERECONCILIATIONSTATUSID));
			record.setSaleReconciliationDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.SALERECONCILIATIONDATE)));
			record.setSaleBatchUploadId(rs.getLong(BluefinWebPortalConstants.SALEBATCHUPLOADID));
			record.setSaleIsVoided(rs.getInt(BluefinWebPortalConstants.SALEISVOIDED));
			record.setSaleIsRefunded(rs.getInt(BluefinWebPortalConstants.SALEISREFUNDED));
			record.setMerchantId(rs.getString("MID"));// MID
			record.setRecondProcessorName(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL1));
			record.setReconReconciliationStatusId(rs.getString(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL1));
			return record;
		}
	}

	class SaleTransactionRowMapper implements RowMapper<SaleTransaction> {

		@Override
		public SaleTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			SaleTransaction record = new SaleTransaction();
			record.setSaleTransactionId(rs.getLong(BluefinWebPortalConstants.SALETRANSACTIONID));
			record.setTransactionType(rs.getString(BluefinWebPortalConstants.TRANSACTIONTYPE));
			record.setLegalEntityApp(rs.getString("LegalEntityApp"));
			record.setAccountId(rs.getString("AccountId"));
			record.setApplicationTransactionId(rs.getString("ApplicationTransactionID"));
			record.setProcessorTransactionId(rs.getString(BluefinWebPortalConstants.PROCESSORTRANSACTIONID));
			record.setMerchantId(rs.getString(BluefinWebPortalConstants.MERCHANTID));
			Timestamp ts;
			if (rs.getString("TransactionDateTime") != null) {
				ts = Timestamp.valueOf(rs.getString("TransactionDateTime"));
				record.setTransactionDateTime(new DateTime(ts));
			}
			record.setCardNumberFirst6Char(rs.getString("CardNumberFirst6Char"));
			record.setCardNumberLast4Char(BluefinWebPortalConstants.CARDMASK+rs.getString("CardNumberLast4Char"));

			record.setCardType(rs.getString("CardType"));
			record.setChargeAmount(rs.getBigDecimal("ChargeAmount"));
			try {
				record.setExpiryDate(rs.getDate("ExpiryDate"));
			} catch (SQLException sqlExp) {
				logger.error("Invalid value found for expiry date , App Transaction Id= {} , Exp Message={}",record.getApplicationTransactionId(),sqlExp.getMessage(),sqlExp);
			}
			record.setFirstName(rs.getString(BluefinWebPortalConstants.FIRSTNAME));
			record.setLastName(rs.getString(BluefinWebPortalConstants.LASTNAME));
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
			record.setApplication(rs.getString(BluefinWebPortalConstants.APPLICATION));
			record.setOrigin(rs.getString("Origin"));
			record.setAccountPeriod(rs.getString("AccountPeriod"));
			record.setDesk(rs.getString("Desk"));
			record.setInvoiceNumber(rs.getString("InvoiceNumber"));
			record.setUserDefinedField1(rs.getString("UserDefinedField1"));
			record.setUserDefinedField2(rs.getString("UserDefinedField2"));
			record.setUserDefinedField3(rs.getString("UserDefinedField3"));
			record.setDateCreated(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.DATECREATED)));
			record.setIsVoided(rs.getInt("IsVoided"));
			record.setIsRefunded(rs.getInt("IsRefunded"));
			record.setPaymentProcessorInternalStatusCodeId(rs.getLong("PaymentProcessorInternalStatusCodeID"));
			record.setPaymentProcessorInternalResponseCodeId(rs.getLong("PaymentProcessorInternalResponseCodeID"));
			record.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL));
			record.setReconciliationDate(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.RECONCILIATIONDATE)));
			record.setBatchUploadId(rs.getLong("BatchUploadID"));
			return record;
		}
	}

	private PaymentProcessorRemittance fetchPaymentProcessorRemittanceCustomMappingResultSingle(String query, String transactionId) {
		return jdbcTemplate.queryForObject(query, new Object[]{transactionId}, new PaymentProcessorRemittanceRowMapper());
	}

	private String getSearchValue(String search){
		int anyOtherParamsIndex = search.indexOf('&');
		String searchValue;
		if (anyOtherParamsIndex != -1 && anyOtherParamsIndex < search.length()) {
			searchValue = search.substring(0, anyOtherParamsIndex);
		} else {
			searchValue = search;
		}
		return searchValue;
	}
	/**
	 * This method is used to split the data that comes in the search parameter
	 *
	 * @param searchValue, this values comes with the filter data selected in the UI
	 * @return Map<String, String> with those values splitted
	 * */
	private Map<String, String> evaluateSearchParam(String searchValue) {
		String[] searchArray = searchValue.split("\\$\\$");
		logger.info("Search Array Values= {} and size of searchArray {}", Arrays.asList(searchArray), searchArray.length);
		Map<String, String> paramValues = new HashMap<>();

		for (String parameter : searchArray) {
			// split: second parameter set to 2, because when we split the date time we
			// don't want that the time be splitted
			String[] parameterArray = parameter.split(":|<|>", 2);
			if (parameter.contains(">")) {
				parameterArray[0] = BluefinWebPortalConstants.REMITTANCECREATIONDATEBEGIN;
			} else if (parameter.contains("<")) {
				parameterArray[0] = BluefinWebPortalConstants.REMITTANCECREATIONDATEEND;
			} else if (BluefinWebPortalConstants.MERCHANTIDPARAM.equals(parameterArray[0])) {
				parameterArray[1] = parameterArray[1].replaceAll("\\[|\\]", "");
			}

			paramValues.put(parameterArray[0], parameterArray[1]);
		}

		return paramValues;
	}

	private String[] getMerchantIdArray(String values){
		return values != null ? values.split(",") : null;
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
	private String getNativeQueryForRemittanceSaleRefund(String search,boolean negate, Map<String,String> queryParameters) {
		String searchValue = getSearchValue(search);

		Map<String, String> evaluatedValues = evaluateSearchParam(searchValue);
		boolean dataFiltered = !CollectionUtils.isEmpty(evaluatedValues);
		String remittanceCreationDateBegin = evaluatedValues.get(BluefinWebPortalConstants.REMITTANCECREATIONDATEBEGIN);
		String remittanceCreationDateEnd = evaluatedValues.get(BluefinWebPortalConstants.REMITTANCECREATIONDATEEND);

		String testOrProd = propertyDAO.getPropertyValue("TEST_OR_PROD");

		// Get reconciliationStatudId for "Missing from Remit"
		ReconciliationStatus reconciliationStatus = reconciliationStatusDAO.findByReconciliationStatus("Missing from Remit");
		String statusId = reconciliationStatus != null ? String.valueOf(reconciliationStatus.getReconciliationStatusId()) : "";

		StringBuilder querySbPart1 = appendQueryForPPR();
		StringBuilder querySbPart2 = appendSaleWhereCondQuery();

		queryParameters.put("remittanceCreationDateBegin", remittanceCreationDateBegin);
		queryParameters.put("remittanceCreationDateEnd", remittanceCreationDateEnd);
		queryParameters.put("testOrProd", testOrProd);
		queryParameters.put("statusId", statusId);

        StringBuilder querySbPart3 = appendWhereConditionsToMainSelect(dataFiltered, evaluatedValues, queryParameters);

		StringBuilder querySb = appendSeparateQueriesIntoConciseQuery(querySbPart1,querySbPart2,querySbPart3,dataFiltered);

		/**
		 *  Currently this is only used if the user selects 'Not Reconcilied' on
		 *  the UI.
		 *  Change to: WHERE ReconciliationID != 'Reconciled'
		 */
		return finalQuery(querySb,negate);
	}

	private StringBuilder appendQueryForPPR(){
		StringBuilder querySbPart1 = new StringBuilder();
		querySbPart1.append(getappendPPRAndSaleQuery());
		querySbPart1.append("WHERE ppr.RemittanceCreationDate >= :remittanceCreationDateBegin ");
		querySbPart1.append("AND ppr.RemittanceCreationDate <= :remittanceCreationDateEnd ");
		querySbPart1.append("AND (ppr.TransactionType in ('SALE', 'settle')) ");
		querySbPart1.append("AND (st.TestMode = :testOrProd OR st.TestMode IS NULL) ");
		querySbPart1.append(BluefinWebPortalConstants.UNION);
		querySbPart1.append(getappendPPRAndRefundQuery());
		querySbPart1.append("WHERE ppr.RemittanceCreationDate >= :remittanceCreationDateBegin ");
		querySbPart1.append("AND ppr.RemittanceCreationDate <= :remittanceCreationDateEnd ");
		querySbPart1.append("AND (ppr.TransactionType in ('REFUND')) ");
		querySbPart1.append("AND (st1.TestMode = :testOrProd OR st1.TestMode IS NULL) ");
		logger.info("Query (part 1): {} " , querySbPart1);
		return querySbPart1;
	}


	//SELECT #1 + FROM
	private String getappendPPRAndSaleQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append("SELECT ppr.ReconciliationStatusID, ppr.TransactionAmount , ppr.TransactionType, ppr.TransactionTime, ppr.AccountID,");
		querySb.append("ppr.Application AS Application, ppr.ProcessorTransactionID, ppr.MerchantID, ppr.PaymentProcessorID,");
		querySb.append("ppl.ProcessorName AS ProcessorName, st.SaleTransactionID AS SaleTransactionID, st.TransactionType AS SaleTransactionType, st.CardNumberLast4Char AS SaleCardNumberLast4Char,");
		querySb.append("st.CardType AS SaleCardType , st.AccountId AS SaleAccountId ,st.ChargeAmount AS SaleChargeAmount ,  st.ApplicationTransactionID AS SaleApplicationTransactionID,");
		querySb.append("st.MerchantID AS SaleMerchantID, st.Processor AS SaleProcessor , st.Application AS SaleApplication , st.ProcessorTransactionID AS SaleProcessorTransactionID, ");
		querySb.append("st.TransactionDateTime AS SaleTransactionDateTime, st.ReconciliationStatusID AS SaleReconciliationStatusID , ppr.MerchantID AS MID, ppl.ProcessorName AS Processor_Name, ppr.ReconciliationStatusID AS ReconciliationStatus_ID ");

		querySb.append("FROM PaymentProcessor_Remittance ppr ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
		querySb.append("JOIN (SELECT ppr2.PaymentProcessorRemittanceID AS PaymentProcessorRemittanceID2 , st2.* FROM PaymentProcessor_Remittance ppr2 ");
		querySb.append("LEFT JOIN Sale_Transaction st2 ON ppr2.ProcessorTransactionID = st2.ProcessorTransactionID ");
		querySb.append("WHERE ppr2.RemittanceCreationDate >= :remittanceCreationDateBegin ");
		querySb.append("AND ppr2.RemittanceCreationDate <= :remittanceCreationDateEnd ");
		querySb.append("AND (ppr2.TransactionType in ('SALE', 'settle')) ");
		querySb.append("AND (st2.TestMode = :testOrProd OR st2.TestMode IS NULL) ");
		querySb.append(") as st ON ppr.PaymentProcessorRemittanceID = st.PaymentProcessorRemittanceID2 ");
		return querySb.toString();
	}


	//SELECT #2 + FROM
	private String getappendPPRAndRefundQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append("SELECT ppr.ReconciliationStatusID, ppr.TransactionAmount, ppr.TransactionType, ppr.TransactionTime, ppr.AccountID, ");
		querySb.append(" ppr.Application AS Application, ppr.ProcessorTransactionID, ppr.MerchantID, ppr.PaymentProcessorID, ");
		querySb.append(" ppl.ProcessorName AS ProcessorName , rt.SaleTransactionID AS SaleTransactionID ,'REFUND' AS SaleTransactionType, st1.CardNumberLast4Char AS SaleCardNumberLast4Char, ");
		querySb.append(" st1.CardType AS SaleCardType, st1.AccountId AS SaleAccountId, st1.ChargeAmount AS SaleChargeAmount, rt.ApplicationTransactionID AS SaleApplicationTransactionID,");
		querySb.append(" rt.MerchantID AS SaleMerchantID, rt.Processor AS SaleProcessor, rt.Application AS SaleApplication, rt.ProcessorTransactionID AS SaleProcessorTransactionID, ");
		querySb.append(" rt.TransactionDateTime AS SaleTransactionDateTime , rt.ReconciliationStatusID AS SaleReconciliationStatusID, ");
		querySb.append(" ppr.MerchantID AS MID, ppl.ProcessorName AS Processor_Name, ppr.ReconciliationStatusID AS ReconciliationStatus_ID ");

		querySb.append("FROM PaymentProcessor_Remittance ppr ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID)");
		querySb.append("INNER JOIN (SELECT ppr2.PaymentProcessorRemittanceID AS PaymentProcessorRemittanceID2 , rt2.* ");
		querySb.append("FROM PaymentProcessor_Remittance ppr2 ");
		querySb.append("LEFT JOIN Refund_Transaction rt2 ON ppr2.ProcessorTransactionID = rt2.ProcessorTransactionID ");
		querySb.append("WHERE ppr2.RemittanceCreationDate >= :remittanceCreationDateBegin ");
		querySb.append("AND ppr2.RemittanceCreationDate <= :remittanceCreationDateEnd ");
		querySb.append(" AND (ppr2.TransactionType in ('REFUND'))");
		querySb.append(") as rt ON ppr.PaymentProcessorRemittanceID = rt.PaymentProcessorRemittanceID2 ");
		querySb.append(" LEFT JOIN Sale_Transaction st1 ON (rt.SaleTransactionId = st1.SaleTransactionId) ");
		return querySb.toString();
		}


	private StringBuilder appendSaleWhereCondQuery(){
		StringBuilder querySbPart2 = new StringBuilder();
		querySbPart2.append(BluefinWebPortalConstants.UNION);
		querySbPart2.append(getSaleQuery());
		querySbPart2.append("WHERE SALE.TransactionDateTime >= DATE_ADD(CAST( :remittanceCreationDateBegin "
				+ " AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -2 DAY) ");
		querySbPart2.append("AND SALE.TransactionDateTime <= DATE_ADD(CAST( :remittanceCreationDateBegin "
				+ " AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -1 DAY) ");
		querySbPart2.append("AND SALE.InternalStatusCode = '1' ");
		querySbPart2.append("AND (SALE.TransactionType = 'SALE') ");
		querySbPart2.append("AND SALE.ReconciliationStatusID = :statusId ");
		querySbPart2.append(BluefinWebPortalConstants.UNION);
		querySbPart2.append(getRefundQuery());
		querySbPart2.append("WHERE REFUND.TransactionDateTime >= DATE_ADD(CAST( :remittanceCreationDateBegin "
				+ " AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -2 DAY) ");
		querySbPart2.append("AND REFUND.TransactionDateTime <= DATE_ADD(CAST( :remittanceCreationDateBegin "
				+ " AS DATETIME) + CAST(ppl.RemitTransactionCloseTime AS TIME),INTERVAL -1 DAY) ");
		querySbPart2.append("AND REFUND.InternalStatusCode = '1' ");
		querySbPart2.append("AND REFUND.ReconciliationStatusID = :statusId ");
		logger.info("query (part 2): {}" , querySbPart2);
		return querySbPart2;
	}
	/**
	 * This method appends where conditions to main select if there is any filter applied
	 *
	 * @param dataFiltered, if some filter data was sent
	 * @param evaluatedValues, the filters that were selected in the UI
	 * @param queryParameters, to save the parameters for the query
	 * @return StringBuilder with the where clauses to append in the main select
	 */
	private StringBuilder appendWhereConditionsToMainSelect(boolean dataFiltered, Map<String, String> evaluatedValues,Map<String, String> queryParameters) {
		StringBuilder sb = new StringBuilder();
		StringJoiner sj = new StringJoiner("AND", WHERE_COND, "");
		sj.setEmptyValue("");// "WHERE" is append as default value, if we do not have extra filters we do not append anything

        if (dataFiltered) {
            sb.append(" ReconDate ");

            Iterator<Map.Entry<String, String>> itr = evaluatedValues.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry<String, String> entry = itr.next();

                switch (entry.getKey()) {
                    case BluefinWebPortalConstants.PROCESSORNAME:
                        String processorName = evaluatedValues.get(BluefinWebPortalConstants.PROCESSORNAME);
                        sj.add(" ReconDate.Processor_Name = :RDProcessorName ");
                        queryParameters.put("RDProcessorName", processorName);
                        break;

                    case BluefinWebPortalConstants.MERCHANTIDPARAM:
                        StringBuilder midSB = new StringBuilder();
                        String[] merchantIdArray = getMerchantIdArray(evaluatedValues.get(BluefinWebPortalConstants.MERCHANTIDPARAM));
                        midSB.append(" (ReconDate.MID IN (");
                        for (int i = 0; i < merchantIdArray.length; i++) {
                            midSB.append(" :RDMerchantId" + i);
                            queryParameters.put("RDMerchantId" + i, merchantIdArray[i]);
                            if (i != (merchantIdArray.length - 1)) {
                                midSB.append(", ");
                            }
                        }
                        midSB.append(")) ");
                        sj.add(midSB);
                        break;

				case BluefinWebPortalConstants.RECONCILIATIONSTATUSID:
					String reconciliationStatusId = evaluatedValues.get(BluefinWebPortalConstants.RECONCILIATIONSTATUSID);
					sj.add(" ReconDate.ReconciliationStatus_ID = :RDReconciliationStatusId ");
					queryParameters.put("RDReconciliationStatusId", reconciliationStatusId);
					break;

				default:// default is empty since cases required are considered
					break;
				}
			}

			sb.append(sj);
		}

		sb.append(" ORDER BY Processor_Name ASC, MID ASC, ReconciliationStatus_ID ASC");

		return sb;
	}
	/**
	 * This method appends the different built parts of the query into one more concise one
	 *
	 * @param querySbPart1, part 1 of the query
	 * @param querySbPart2, part 2 of the query
	 * @param querySbPart3, part 3 of the query
	 * @param dataFiltered, if some filter data was sent
	 * @return StringBuilder, a united/more concise query
	 */
	private StringBuilder appendSeparateQueriesIntoConciseQuery(StringBuilder querySbPart1, StringBuilder querySbPart2, StringBuilder querySbPart3, boolean dataFiltered) {
		StringBuilder sb = new StringBuilder();

        sb.append(querySbPart1);
        sb.append(querySbPart2);

        if (dataFiltered) {
            StringJoiner sj = new StringJoiner(sb);
            sj.add("SELECT * FROM ("); // append the select at the beginning
            sj.add(")");               // append the close parenthesis at the end
            sb = new StringBuilder(sj.toString());
        }

        sb.append(querySbPart3);

        return sb;
    }

	private String finalQuery(StringBuilder querySb,boolean negate){
		if (negate) {
			return querySb.toString().replace("ReconciliationStatus_ID = 1", "ReconciliationStatus_ID != 1");
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
		querySb.append(BluefinWebPortalConstants.SALEVOIDREFUNDCONST);
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
                "st1.CardNumberLast4Char AS SaleCardNumberLast4Char,st1.CardType AS SaleCardType,st1.ExpiryDate AS SaleExpiryDate,NULL AS SaleToken,st1.ChargeAmount AS SaleChargeAmount,");
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
        querySb.append(BluefinWebPortalConstants.SALEVOIDREFUNDCONST);
        querySb.append(
                "ppr.MerchantID AS MID,ppl.ProcessorName AS Processor_Name,ppr.ReconciliationStatusID AS ReconciliationStatus_ID ");
        querySb.append("FROM PaymentProcessor_Remittance ppr ");
        querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
        querySb.append("LEFT JOIN Refund_Transaction rt ON (ppr.ProcessorTransactionID = rt.ProcessorTransactionID) ");
        querySb.append("LEFT JOIN Sale_Transaction st1 ON (rt.SaleTransactionId = st1.SaleTransactionId) ");
        return querySb.toString();
    }

	//Select #3 + FROM
	private String getSaleQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append(" SELECT NULL AS ReconciliationStatusID, NULL AS TransactionAmount, NULL AS TransactionType, NULL AS TransactionTime,");
		querySb.append(" NULL AS AccountID, NULL AS Application, NULL AS ProcessorTransactionID, NULL AS MerchantID, ");
		querySb.append(" NULL AS PaymentProcessorID, NULL AS ProcessorName, SALE.SaleTransactionID, ");
		querySb.append(" SALE.TransactionType, SALE.CardNumberLast4Char, SALE.CardType, SALE.AccountId, ");
		querySb.append(" SALE.ChargeAmount, SALE.ApplicationTransactionID, SALE.MerchantID,");
		querySb.append(" SALE.Processor   AS SaleProcessor, SALE.Application AS Application, SALE.ProcessorTransactionID, SALE.TransactionDateTime, SALE.ReconciliationStatusID,");
		querySb.append(" SALE.MerchantID AS MID , SALE.Processor AS Processor_Name, SALE.ReconciliationStatusID AS ReconciliationStatus_ID ");

		querySb.append("FROM Sale_Transaction SALE ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (SALE.Processor = ppl.ProcessorName) ");
		return querySb.toString();
	}

	//Select #4 + FROM
	private String getRefundQuery() {
		StringBuilder querySb = new StringBuilder();
		querySb.append(" SELECT NULL AS ReconciliationStatusID, NULL AS TransactionAmount, NULL AS TransactionType, NULL AS TransactionTime, ");
		querySb.append(" NULL AS AccountID, NULL AS Application, NULL AS ProcessorTransactionID, NULL AS MerchantID,");
		querySb.append(" NULL AS PaymentProcessorID, NULL AS ProcessorName, REFUND.SaleTransactionID,");
		querySb.append("'REFUND' AS RefundTransactionType, st2.CardNumberLast4Char AS RefundCardNumberLast4Char, st2.CardType AS RefundCardType, st2.AccountId AS RefundAccountId, ");
		querySb.append("st2.ChargeAmount AS RefundChargeAmount , REFUND.ApplicationTransactionID, REFUND.MerchantID, REFUND.Processor AS SaleProcessor, ");
		querySb.append(" REFUND.Application AS Application , REFUND.ProcessorTransactionID, REFUND.TransactionDateTime, REFUND.ReconciliationStatusID,");
		querySb.append(" REFUND.MerchantID AS MID, REFUND.Processor AS Processor_Name, REFUND.ReconciliationStatusID AS ReconciliationStatus_ID ");

		querySb.append("FROM Refund_Transaction REFUND ");
		querySb.append("JOIN Sale_Transaction st2 on (REFUND.SaleTransactionId = st2.SaleTransactionId) ");
		querySb.append("JOIN PaymentProcessor_Lookup ppl ON (REFUND.Processor = ppl.ProcessorName) ");
		return querySb.toString();
	}

	private String getNativeQueryForRemittanceSaleRefundDetail(TransactionTypeCode transactionType, String processorTransactionType) {
		StringBuilder querySb = new StringBuilder();
		switch (transactionType) {
			case REFUND:
				prepareForRefund(querySb, processorTransactionType);
				break;
			case VOID:
				// Type VOID should not be used for remittance.
				break;
			case SALE:
			case TOKENIZE:
			case SETTLE:
			default:
				prepareForDefault(querySb, processorTransactionType);
		}
		return querySb.toString();
	}

	private void prepareForRefund(StringBuilder querySb, String processorTransactionType) {
		querySb.append(getPaymentProcessorRemittanceAndRefundQuery());

		if ("BlueFin".equalsIgnoreCase(processorTransactionType)) {
			querySb.append("WHERE rt.ApplicationTransactionID = ?");
		} else {
			querySb.append("WHERE rt.ProcessorTransactionID = ?");
		}
	}

	private void prepareForDefault(StringBuilder querySb, String processorTransactionType) {
		querySb.append(getPaymentProcessorRemittanceAndSaleQuery());

		if ("BlueFin".equalsIgnoreCase(processorTransactionType)) {
			querySb.append("WHERE st.ApplicationTransactionID = ?");
		} else {
			querySb.append("WHERE st.ProcessorTransactionID = ?");
		}
	}
}

class CustomSalePaymentProcessorRemittanceExtractor implements ResultSetExtractor<List<RemittanceSale>> {

    @Override
    public List<RemittanceSale> extractData(ResultSet rs) throws SQLException {

        ArrayList<RemittanceSale> list = new ArrayList<>();

        while (rs.next()) {

            RemittanceSale remittanceSale = new RemittanceSale();

			PaymentProcessorRemittance ppr = new PaymentProcessorRemittance();

			// Mitul overrides this with ReconciliationStatus_ID
			ppr.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL));
			ppr.setTransactionAmount(rs.getBigDecimal(BluefinWebPortalConstants.TRANSACTIONAMOUNT));
			ppr.setTransactionType(rs.getString(BluefinWebPortalConstants.TRANSACTIONTYPE));
			ppr.setTransactionTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.TRANSACTIONTIME)));
			ppr.setAccountId(rs.getString(BluefinWebPortalConstants.ACCOUNTIDVAL));
			ppr.setApplication(rs.getString(BluefinWebPortalConstants.APPLICATION));
			ppr.setProcessorTransactionId(rs.getString(BluefinWebPortalConstants.PROCESSORTRANSACTIONID));
			// Mitul overrides this with MID
			ppr.setMerchantId(rs.getString(BluefinWebPortalConstants.MERCHANTID));
			ppr.setPaymentProcessorId(rs.getLong(BluefinWebPortalConstants.PAYMENTPROCESSORIDVAL));
			// Final value (ORDER BY)
			ppr.setMerchantId(rs.getString("MID"));
			// Final value (ORDER BY)
			ppr.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.RECONCILIATIONSTATUSIDVAL1));
			remittanceSale.setPaymentProcessorRemittance(ppr);

            SaleTransaction st = new SaleTransaction();
            // Mitul overrides this with Processor_Name
            st.setProcessor(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL));
            st.setSaleTransactionId(rs.getLong(BluefinWebPortalConstants.SALETRANSACTIONID));
            st.setTransactionType(rs.getString(BluefinWebPortalConstants.SALETRANSACTIONTYPE));
            st.setCardNumberLast4Char(rs.getString(BluefinWebPortalConstants.SALECARDNUMBERLAST4CHAR));
            st.setCardType(rs.getString(BluefinWebPortalConstants.SALECARDTYPE));
            st.setChargeAmount(rs.getBigDecimal(BluefinWebPortalConstants.SALECHARGEAMOUNT));
            st.setAccountId(rs.getString(BluefinWebPortalConstants.SALEACCOUNTID));
            st.setApplicationTransactionId(rs.getString(BluefinWebPortalConstants.SALEAPPLICATIONTRANSACTIONID));
            st.setMerchantId(rs.getString(BluefinWebPortalConstants.SALEMERCHANTID));
            st.setProcessor(rs.getString(BluefinWebPortalConstants.SALEPROCESSOR));
            st.setApplication(rs.getString(BluefinWebPortalConstants.SALEAPPLICATION));
            st.setProcessorTransactionId(rs.getString(BluefinWebPortalConstants.SALEPROCESSORTRANSACTIONID));
            st.setTransactionDateTime(new DateTime(rs.getTimestamp(BluefinWebPortalConstants.SALETRANSACTIONDATETIME)));
            st.setReconciliationStatusId(rs.getLong(BluefinWebPortalConstants.SALERECONCILIATIONSTATUSID));
            // Final value (ORDER BY)
            st.setProcessor(rs.getString(BluefinWebPortalConstants.PROCESSORNAMEVAL1));
            remittanceSale.setSaleTransaction(st);

            list.add(remittanceSale);
        }

		return list;
	}

}
