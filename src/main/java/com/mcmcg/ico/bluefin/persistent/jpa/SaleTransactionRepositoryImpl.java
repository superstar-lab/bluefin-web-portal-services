package com.mcmcg.ico.bluefin.persistent.jpa;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.mcmcg.ico.bluefin.model.PaymentFrequency;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

class SaleTransactionRepositoryImpl implements TransactionRepositoryCustom {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaleTransactionRepositoryImpl.class);

    private static final String EQUALS = " = ";
    private static final String LOE = " <= ";
    private static final String GOE = " >= ";

    @PersistenceContext
    private EntityManager em;

    private HashMap<String, String> dynamicParametersMap = new HashMap<String, String>();
    private HashMap<String, String> predicatesHashMapping = new HashMap<String, String>();

    @Value("${bluefin.wp.services.transactions.report.max.size}")
    private String maxSizeReport;

    @PostConstruct
    public void init() {
        loadSaleTransactionMappings();
    }

    @Override
    public Page<SaleTransaction> findTransaction(String search, PageRequest page) throws ParseException {
        // Creates the query for the total and for the retrieved data
        String query = getQueryByCriteria(search);

        Map<String, Query> queriesMap = createQueries(query, page);
        Query result = queriesMap.get("result");
        Query queryTotal = queriesMap.get("queryTotal");

        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        // Set the paging for the created select
        final int countResult = (Integer) queryTotal.getSingleResult();
        result.setFirstResult(pageSize * pageNumber);
        result.setMaxResults(pageSize);

        // Brings the data and transform it into a Page value list
        @SuppressWarnings("unchecked")
        List<SaleTransaction> tr = result.getResultList();
        Page<SaleTransaction> list = new PageImpl<SaleTransaction>(tr, page, countResult);

        return list;
    }

    @Override
    public List<SaleTransaction> findTransactionsReport(String search) throws ParseException {
        String query = getQueryByCriteria(search);
        LOGGER.info("Dynamic Query {}", query);

        Map<String, Query> queriesMap = createQueries(query, null);
        Query result = queriesMap.get("result");

        result.setMaxResults(Integer.parseInt(maxSizeReport));
        @SuppressWarnings("unchecked")
        List<SaleTransaction> tr = result.getResultList();

        return tr;
    }

    public Map<String, Query> createQueries(String query, PageRequest page) throws ParseException {
        Query queryTotal = em
                .createNativeQuery("SELECT COUNT(finalCount.ApplicationTransactionID) FROM (" + query + ") finalCount");

        Query result = em.createNativeQuery(page == null ? query : query + addSort(page.getSort()),
                "CustomMappingResult");

        LOGGER.info("Dynamic Parameters {}", dynamicParametersMap);
        // Sets all parameters to the Query result
        for (Map.Entry<String, String> entry : dynamicParametersMap.entrySet()) {
            if (entry.getKey().contains("amountParam")) {
                result.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
            } else if (entry.getKey().contains("transactionDateTimeParam") || (entry.getKey().contains("reconciliationDate"))) {

                if (!validFormatDate(entry.getValue())) {
                    throw new CustomNotFoundException(
                            "Unable to process find transaction, due an error with date formatting");
                }
                // Special case for the dates
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains("legalEntityParam")) {
                // Special case for legal entity
                String value = entry.getValue().replace("[", "").replace("]", "");

                result.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
                queryTotal.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
            } else {
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            }
        }
        dynamicParametersMap.clear();
        HashMap<String, Query> queriesMap = new HashMap<String, Query>();
        queriesMap.put("result", result);
        queriesMap.put("queryTotal", queryTotal);
        return queriesMap;
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

    /**
     * Handles the type of transaction that is going to be retrieved, according
     * with the transactionType element. if transactionType is Sale will bring
     * the select from the sale table, if refund will bring the union with the
     * table sale and the table refund
     * 
     * @param search
     * @return String query
     */
    private String getQueryByCriteria(String search) {
        StringBuilder querySb = new StringBuilder();
        querySb.append(" SELECT * FROM (");

        switch (getTransactionType(search).toLowerCase()) {
        case "void":
            querySb.append(getSelectForVoidTransaction(search));
            break;
        case "refund":
            querySb.append(getSelectForRefundTransaction(search));
            break;
        case "all":
            querySb.append(getSelectForSaleTransaction(search));
            querySb.append(" UNION ");
            querySb.append(getSelectForVoidTransaction(search));
            querySb.append(" UNION ");
            querySb.append(getSelectForRefundTransaction(search));
            break;
        case "sale":
        case "tokenize":
        default:
            querySb.append(getSelectForSaleTransaction(search));
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
    private String getSelectForSaleTransaction(String search) {
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
                .append("(SELECT Count(*) FROM void_transaction WHERE saletransactionid = MAINSALE.saletransactionid AND InternalStatusCode = '1') AS IsVoided,")
                .append("(SELECT Count(*) FROM refund_transaction WHERE  saletransactionid = MAINSALE.saletransactionid AND InternalStatusCode = '1') AS IsRefunded, ")
                .append("MAINSALE.PaymentProcessorInternalStatusCodeID, MAINSALE.PaymentProcessorInternalResponseCodeID, MAINSALE.ReconciliationStatusID, MAINSALE.ReconciliationDate ")
                .append("FROM Sale_Transaction MAINSALE ");

        querySb.append(createWhereStatement(search, "MAINSALE"));

        return querySb.toString();
    }

    /**
     * Creates the select for table VOID_TRANSACTION
     * 
     * @param search
     * @return String with the select of the void transaction table
     */
    private String getSelectForVoidTransaction(String search) {
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
                .append("VOID.PaymentProcessorInternalStatusCodeID, VOID.PaymentProcessorInternalResponseCodeID, NULL AS ReconciliationStatusID, CAST(NULL AS DATETIME) AS ReconciliationDate ")
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

                .append(createWhereStatement(search, "SALEINNERVOID"))
                .append(" ) VOIDSALE ON (VOID.saleTransactionID = VOIDSALE.saleTransactionID) ")
                .append(createWhereStatement(search, "VOID"));
        return querySb.toString();

    }

    /**
     * Creates the select for table REFUND_TRANSACTION
     * 
     * @param search
     * @return String with the select of the refund transaction table
     */
    private String getSelectForRefundTransaction(String search) {
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
                .append("REFUND.PaymentProcessorInternalStatusCodeID, REFUND.PaymentProcessorInternalResponseCodeID, REFUND.ReconciliationStatusID, REFUND.ReconciliationDate ")
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
                .append("SALEINNERREFUND.UserDefinedField2,SALEINNERREFUND.UserDefinedField3,SALEINNERREFUND.DateCreated,SALEINNERREFUND.ReconciliationStatusID,SALEINNERREFUND.ReconciliationDate ")
                .append("FROM Sale_Transaction SALEINNERREFUND ")

                .append(createWhereStatement(search, "SALEINNERREFUND"))
                .append(" ) REFUNDSALE ON (REFUND.saleTransactionID = REFUNDSALE.saleTransactionID) ")
                .append(createWhereStatement(search, "REFUND"));

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
    private String createWhereStatement(String search, String prefix) {
        StringJoiner statement = new StringJoiner(" AND ");

        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(QueryDSLUtil.SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + QueryDSLUtil.SEARCH_DELIMITER_CHAR);

            while (matcher.find()) {
                final String attribute = matcher.group(1);
                final String operator = matcher.group(2);
                final String value = matcher.group(3);
                String attributeParam = attribute + "Param1";
                String predicate = getPropertyPredicate(attribute);

                if (!prefix.equalsIgnoreCase("MAINSALE") && skipFilter(attribute, prefix)) {
                    continue;
                }

                // Special scenarios, be careful when you change this
                if (attribute.equalsIgnoreCase("processUser") && (prefix.equalsIgnoreCase("REFUND") || prefix.equalsIgnoreCase("VOID"))) {
                    // Special case for pUser in VOID and REFUND tables
                    predicate = getPropertyPredicate("pUser");
                    attributeParam = "pUserParam1";
                } else if (attribute.equalsIgnoreCase("transactionDateTime") || attribute.equalsIgnoreCase("amount") || attribute.equalsIgnoreCase("reconciliationDate")) {
                    // Specific cases for transactionDateTime, amount
                    predicate = predicate.replace(":atributeOperator", getOperation(operator));
                    if (dynamicParametersMap.containsKey(attribute + "Param1")) {
                        attributeParam = attribute + "Param2";
                        predicate = predicate.replace(attribute + "Param1", attributeParam);
                    }
                } else if (attribute.equalsIgnoreCase("paymentFrequency")
                        && (PaymentFrequency.getPaymentFrequency(value) == PaymentFrequency.ONE_TIME)) {
                    // Specific case for paymentFrequency, when paymentFrequency
                    // is NOT 'Recurring' then we need to search by all the
                    // values except 'Recurring'
                    predicate = predicate.replace("=", "<>");
                } 

                statement.add(predicate.replace(":prefix", prefix));
                dynamicParametersMap.put(attributeParam, value);
            }
        }
        return statement.length() == 0 ? "" : " WHERE " + statement.toString();

    }

    public boolean skipFilter(String attribute, String prefix) {
    	// For payment processor remittance, processorName is a filter,
    	// so this should not be skipped.
    	if (prefix.equals("st") && attribute.equalsIgnoreCase("processorName")) {
    		return false;
    	}
    	// For payment processor remittance, legalEntity is not a filter.
    	if (prefix.equals("ppr") && attribute.equalsIgnoreCase("legalEntity")) {
    		return true;
    	}
        if (attribute.equalsIgnoreCase("transactionType")) {
            return true;
        }
        if (prefix.equals("REFUND") || prefix.equals("VOID")) {
            if (attribute.equalsIgnoreCase("accountNumber") || attribute.equalsIgnoreCase("amount")
                    || attribute.equalsIgnoreCase("cardType") || attribute.equalsIgnoreCase("legalEntity")
                    || attribute.equalsIgnoreCase("firstName") || attribute.equalsIgnoreCase("lastName")
                    || attribute.equalsIgnoreCase("accountPeriod") || attribute.equalsIgnoreCase("desk")
                    || attribute.equalsIgnoreCase("invoiceNumber") || attribute.equalsIgnoreCase("paymentFrequency")) {
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
     * Loads the predicates mapping the elements in the saletransaction entity
     */
    private void loadSaleTransactionMappings() {
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
        predicatesHashMapping.put("processUser", ":prefix.ProcessUser = :processUserParam1"); // This is ONLY for sale
        predicatesHashMapping.put("pUser", ":prefix.pUser = :pUserParam1"); // This is ONLY for void and refund
        predicatesHashMapping.put("accountPeriod", ":prefix.AccountPeriod = :accountPeriodParam1");
        predicatesHashMapping.put("desk", ":prefix.Desk = :deskParam1");
        predicatesHashMapping.put("invoiceNumber", ":prefix.InvoiceNumber = :invoiceNumberParam1");
        predicatesHashMapping.put("paymentFrequency", ":prefix.Origin = :paymentFrequencyParam1");
        // Payment Processor Remittance
        predicatesHashMapping.put("paymentProcessorId", ":prefix.PaymentProcessorID = :paymentProcessorIdParam1");
        predicatesHashMapping.put("processorName", ":prefix.Processor = :processorNameParam1");
        predicatesHashMapping.put("reconciliationStatusId", ":prefix.ReconciliationStatusID = :reconciliationStatusIdParam1");
        predicatesHashMapping.put("reconciliationDate", ":prefix.ReconciliationDate :atributeOperator :reconciliationDateParam1");
    }
    
    /**
     * Creates the select for table Sale_Transaction and Refund_Transaction
     * 
     * @param search
     * 
     * @return String with the select of the sale transaction table
     */
    private String getSelectForSaleRefundTransaction(String search) {
        StringBuilder querySb = new StringBuilder();
        
        querySb.append(
        		" SELECT st.SaleTransactionID,st.FirstName,st.LastName,st.ProcessUser,st.TransactionType,st.Address1,st.Address2,")
				.append("st.City,st.State,st.PostalCode,st.Country,st.CardNumberFirst6Char,st.CardNumberLast4Char,st.CardType,st.ExpiryDate,")
				.append("st.Token,st.ChargeAmount,st.LegalEntityApp,st.AccountId,st.ApplicationTransactionID,st.MerchantID,st.Processor,")
				.append("st.Application,st.Origin,st.ProcessorTransactionID,st.TransactionDateTime,st.TestMode,st.ApprovalCode,st.Tokenized,")
				.append("st.PaymentProcessorStatusCode,st.PaymentProcessorStatusCodeDescription,st.PaymentProcessorResponseCode,")
				.append("st.PaymentProcessorResponseCodeDescription,st.InternalStatusCode,st.InternalStatusDescription,st.InternalResponseCode,")
				.append("st.InternalResponseDescription,st.PaymentProcessorInternalStatusCodeID,st.PaymentProcessorInternalResponseCodeID,")
				.append("st.DateCreated,st.PaymentProcessorRuleID,st.RulePaymentProcessorID,st.RuleCardType,st.RuleMaximumMonthlyAmount,")
				.append("st.RuleNoMaximumMonthlyAmountFlag,st.RulePriority,st.AccountPeriod,st.Desk,st.InvoiceNumber,st.UserDefinedField1,")
				.append("st.UserDefinedField2,st.UserDefinedField3,st.ReconciliationStatusID,st.ReconciliationDate,st.BatchUploadID,")
				.append("0 AS IsVoided,0 AS IsRefunded ")
				.append("FROM Sale_Transaction st ")
				.append("JOIN (")
        		.append("SELECT rt.RefundTransactionID,rt.SaleTransactionID,rt.ApprovalCode,rt.Processor,rt.RefundAmount,rt.MerchantID,rt.ProcessorTransactionID,")
        		.append("rt.TransactionDateTime,rt.ApplicationTransactionID,rt.Application,rt.pUser,rt.OriginalSaleTransactionID,")
        		.append("rt.PaymentProcessorStatusCode,rt.PaymentProcessorStatusCodeDescription,rt.PaymentProcessorResponseCode,rt.PaymentProcessorResponseCodeDescription,")
        		.append("rt.InternalStatusCode,rt.InternalStatusDescription,rt.InternalResponseCode,rt.InternalResponseDescription,rt.PaymentProcessorInternalStatusCodeID,")
        		.append("rt.PaymentProcessorInternalResponseCodeID,rt.DateCreated,rt.ReconciliationStatusID,rt.ReconciliationDate ")
        		.append("FROM Refund_Transaction rt) rt ")
        		.append("ON (st.saleTransactionID = rt.saleTransactionID) ");
        
        querySb.append(createWhereStatement(search, "st"));
        
        return querySb.toString();
    }
    
    /**
     * Creates the select for table PaymentProcessor_Remittance and PaymentProcessor_Lookup
     * 
     * @param search
     * 
     * @return String with the select of the payment processor remittance table
     */
    private String getSelectForPaymentProcessorRemittance(String search) {
        StringBuilder querySb = new StringBuilder();
        
        querySb.append(
                " SELECT ppr.PaymentProcessorRemittanceID,ppr.DateCreated,ppr.ReconciliationStatusID,ppr.ReconciliationDate,ppr.PaymentMethod,ppr.TransactionAmount,ppr.TransactionType,")
        		.append("ppr.TransactionTime,ppr.AccountID,ppr.Application,ppr.ProcessorTransactionID,ppr.MerchantID,ppr.TransactionSource,ppr.FirstName,ppr.LastName,")
        		.append("ppr.RemittanceCreationDate,ppr.PaymentProcessorID, ppl.ProcessorName AS ProcessorName ")
        		.append("FROM PaymentProcessor_Remittance ppr ")
        		.append("JOIN PaymentProcessor_Lookup ppl ")
        		.append("ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
        
        querySb.append(createWhereStatement(search, "ppr"));
        
        return querySb.toString();
    }
    
    @SuppressWarnings("unchecked")
	public String getProcessorNameById(String id) {
    	
    	String processorName = null;
    	
    	String sql = "SELECT ppl.ProcessorName FROM PaymentProcessor_Lookup ppl WHERE ppl.PaymentProcessorID = " + id;
    	Query query = em.createNativeQuery(sql);
    	
    	List<String> list = query.getResultList();
    	
    	// Should return a list of one 
    	Iterator<String> iterator = list.iterator();
    	while (iterator.hasNext()) {
    		processorName = (String) iterator.next();
    		break;
    	}
    	
    	return processorName;
    }
    
    private String getQueryForSaleRefund(String search) {
    	StringBuilder querySb = new StringBuilder();
        querySb.append(" SELECT * FROM (");
        querySb.append(getSelectForSaleRefundTransaction(search));
        querySb.append(" ) RESULTINFO ");

        return querySb.toString();
    }
    
    private String getQueryForRemittance(String search) {
    	StringBuilder querySb = new StringBuilder();
        querySb.append(" SELECT * FROM (");
        querySb.append(getSelectForPaymentProcessorRemittance(search));
        querySb.append(" ) RESULTINFO ");

        return querySb.toString();
    }
    
    @Override
    public Page<SaleTransaction> findSalesRefundTransaction(String search, PageRequest page) throws ParseException {
    	
        // Creates the query for the total and for the retrieved data
    	String query = getQueryForSaleRefund(search);

        Map<String, Query> queriesMap = createQueries(query, page);
        Query result = queriesMap.get("result");
        Query queryTotal = queriesMap.get("queryTotal");

        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        // Set the paging for the created select
        final int countResult = (Integer) queryTotal.getSingleResult();
        result.setFirstResult(pageSize * pageNumber);
        result.setMaxResults(pageSize);

        // Brings the data and transform it into a Page value list
        @SuppressWarnings("unchecked")
        List<SaleTransaction> tr = result.getResultList();
        Page<SaleTransaction> list = new PageImpl<SaleTransaction>(tr, page, countResult);

        return list;
    }
    
    public Map<String, Query> createRemittanceQueries(String query, PageRequest page) throws ParseException {
        //Query queryTotal = em.createNativeQuery("SELECT COUNT(finalCount.ApplicationTransactionID) FROM (" + query + ") finalCount");
    	Query queryTotal = em.createNativeQuery(query);

        Query result = em.createNativeQuery(page == null ? query : query + addSort(page.getSort()), "PaymentProcessorRemittanceCustomMappingResult");

        LOGGER.info("Dynamic Parameters {}", dynamicParametersMap);
        // Sets all parameters to the Query result
        for (Map.Entry<String, String> entry : dynamicParametersMap.entrySet()) {
            if (entry.getKey().contains("amountParam")) {
                result.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
            } else if (entry.getKey().contains("transactionDateTimeParam") || (entry.getKey().contains("reconciliationDate"))) {

                if (!validFormatDate(entry.getValue())) {
                    throw new CustomNotFoundException(
                            "Unable to process find transaction, due an error with date formatting");
                }
                // Special case for the dates
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains("legalEntityParam")) {
                // Special case for legal entity
                String value = entry.getValue().replace("[", "").replace("]", "");

                result.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
                queryTotal.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
            } else {
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            }
        }
        dynamicParametersMap.clear();
        HashMap<String, Query> queriesMap = new HashMap<String, Query>();
        queriesMap.put("result", result);
        queriesMap.put("queryTotal", queryTotal);
        return queriesMap;
    }
    
    @Override
    public Page<PaymentProcessorRemittance> findRemittanceTransaction(String search, PageRequest page) throws ParseException {
    	
        // Creates the query for the total and for the retrieved data
    	String query = getQueryForRemittance(search);

        Map<String, Query> queriesMap = createRemittanceQueries(query, page);
        Query result = queriesMap.get("result");
        Query queryTotal = queriesMap.get("queryTotal");

        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        // Set the paging for the created select
        //final int countResult = (Integer) queryTotal.getSingleResult();
        final int countResult = 1;
        //result.setFirstResult(pageSize * pageNumber);
        //result.setMaxResults(pageSize);

        // Brings the data and transform it into a Page value list
        @SuppressWarnings("unchecked")
        List<PaymentProcessorRemittance> tr = result.getResultList();
        Page<PaymentProcessorRemittance> list = new PageImpl<PaymentProcessorRemittance>(tr, page, countResult);

        return list;
    }
}
