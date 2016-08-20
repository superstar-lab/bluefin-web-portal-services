package com.mcmcg.ico.bluefin.persistent.jpa;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import com.mcmcg.ico.bluefin.model.StatusCode;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

class TransactionRepositoryImpl implements TransactionRepositoryCustom {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRepositoryImpl.class);

    private static final String TRANSACTION_TYPE = "(transactionType)(:|<|>)([\\w]+)";
    private static final String EMAIL_PATTERN = "(\\w+?)@(\\w+?).(\\w+?)";
    private static final String NUMBER_LIST_REGEX = "\\[(\\d+)(,\\d+)*\\]";
    private static final String WORD_LIST_REGEX = "\\[(\\w+(-\\w+)?(,\\s?\\w+(-\\w+)?)*)*\\]";
    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
    private static final String NUMBERS_AND_WORDS_REGEX = "[\\w\\s|\\d+(?:\\.\\d+)?]+";
    private static final String SEARCH_REGEX = "(\\w+?)(:|<|>)" + "(" + DATE_REGEX + "|" + NUMBERS_AND_WORDS_REGEX + "|"
            + EMAIL_PATTERN + "|" + NUMBER_LIST_REGEX + "|" + WORD_LIST_REGEX + "),";

    private static final String LIKE = " LIKE ";
    private static final String EQUALS = " = ";
    private static final String OR = " OR ";
    private static final String AND = " AND ";
    private static final String LOE = " <= ";
    private static final String GOE = " >= ";

    private static final String SALE_TABLE = " Sale_Transaction ";
    private static final String VOID_TABLE = " Void_Transaction ";
    private static final String REFUND_TABLE = " Refund_Transaction ";

    @PersistenceContext
    private EntityManager em;

    private HashMap<String, String> dynamicParametersMap = new HashMap<String, String>();
    private HashMap<String, String> nativePropertyHashMapping = new HashMap<String, String>();

    @Value("${bluefin.wp.services.transactions.report.max.size}")
    private String maxSizeReport;

    @PostConstruct
    public void init() {
        loadSaleTransactionMappings();
    }

    @Override
    public Page<SaleTransaction> findTransaction(String search, PageRequest page) throws ParseException {
        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        // Creates the query for the total and for the retrieved data
        String query = getQueryByCriteria(search);

        Query queryTotal = em
                .createNativeQuery("SELECT COUNT(finalCount.ApplicationTransactionID) FROM (" + query + ") finalCount");
        Query result = em.createNativeQuery(query + addSort(page.getSort()), "CustomMappingResult");
        LOGGER.info("Dynamic Query {}", query);
        LOGGER.info("Dynamic Parameters {}", dynamicParametersMap);
        // Sets all parameters to the Query result
        for (Map.Entry<String, String> entry : dynamicParametersMap.entrySet()) {
            if (entry.getKey().contains("amountParam")) {
                result.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
            } else if (entry.getKey().contains("transactionDateTimeParam")
                    || entry.getKey().contains("transactionDateTimeParam")) {

                if (!validFormatDate(entry.getValue())) {
                    throw new CustomNotFoundException(
                            "Unable to process find transaction, due an error with date formatting");
                }
                // Special case for the dates
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains("legalEntityParam")) {
                // Special case for legal entity
                result.setParameter(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
                queryTotal.setParameter(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
            } else if (entry.getKey().contains("internalStatusCodeParam")) {
                // Special case for status code
                result.setParameter(entry.getKey(), StatusCode.getStatusCodeByString(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), StatusCode.getStatusCodeByString(entry.getValue()));
            } else {
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            }
        }
        dynamicParametersMap.clear();

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

        Query queryTotal = em
                .createNativeQuery("SELECT COUNT(finalCount.ApplicationTransactionID) FROM (" + query + ") finalCount");
        Query result = em.createNativeQuery(query, "CustomMappingResult");
        LOGGER.info("Dynamic Query {}", query);
        LOGGER.info("Dynamic Parameters {}", dynamicParametersMap);
        // Sets all parameters to the Query result
        for (Map.Entry<String, String> entry : dynamicParametersMap.entrySet()) {
            if (entry.getKey().contains("amountParam")) {
                result.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
            } else if (entry.getKey().contains("transactionDateTimeParam")
                    || entry.getKey().contains("transactionDateTimeParam")) {

                if (!validFormatDate(entry.getValue())) {
                    throw new CustomNotFoundException(
                            "Unable to process find transaction, due an error with date formatting");
                }
                // Special case for the dates
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains("legalEntityParam")) {
                // Special case for legal entity
                result.setParameter(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
                queryTotal.setParameter(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
            } else if (entry.getKey().contains("internalStatusCodeParam")) {
                // Special case for status code
                result.setParameter(entry.getKey(), StatusCode.getStatusCodeByString(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), StatusCode.getStatusCodeByString(entry.getValue()));
            } else {
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            }
        }

        dynamicParametersMap.clear();
        result.setMaxResults(Integer.parseInt(maxSizeReport));
        @SuppressWarnings("unchecked")
        List<SaleTransaction> tr = result.getResultList();

        return tr;
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
            result.append(getPropertyNativeName(order.getProperty()));
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
        case "sale":
        case "tokenize":
            querySb.append(getSelectForSaleTransaction(search));
            break;
        case "void":
            querySb.append(getSelectForVoidTransaction(search));
            break;
        case "refund":
            querySb.append(getSelectForRefundTransaction(search));
            break;
        default:
            querySb.append(getSelectForSaleTransaction(search));
            querySb.append(" UNION ");
            querySb.append(getSelectForVoidTransaction(search));
            querySb.append(" UNION ");
            querySb.append(getSelectForRefundTransaction(search));
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
        querySb.append(createSelectFromTransactionTypeBased("MAINSALE", "MAINSALE", "SALE", SALE_TABLE));
        querySb.append(createWhereStatementForSale(search, "MAINSALE"));

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
        querySb.append(createSelectFromTransactionTypeBased("VOID", "VOIDSALE", "VOID", VOID_TABLE));
        querySb.append(" JOIN (");

        querySb.append(createSelectFromTransactionTypeBased("SALEINNERVOID", "SALEINNERVOID", "SALE", SALE_TABLE));
        querySb.append(createWhereStatement(search, "SALEINNERVOID"));

        querySb.append(" ) VOIDSALE ON (VOID.saleTransactionID = VOIDSALE.saleTransactionID) ");
        querySb.append(createWhereStatement(search, "VOID"));

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
        querySb.append(createSelectFromTransactionTypeBased("REFUND", "REFUNDSALE", "REFUND", REFUND_TABLE));
        querySb.append(" JOIN (");

        querySb.append(createSelectFromTransactionTypeBased("SALEINNERREFUND", "SALEINNERREFUND", "SALE", SALE_TABLE));
        querySb.append(createWhereStatement(search, "SALEINNERREFUND"));

        querySb.append(" ) REFUNDSALE ON (REFUND.saleTransactionID = REFUNDSALE.saleTransactionID) ");
        querySb.append(createWhereStatement(search, "REFUND"));

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
        String transactionType = "ALL";
        Pattern pattern = Pattern.compile(TRANSACTION_TYPE);
        Matcher matcher = pattern.matcher(search + ",");
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
    private String createWhereStatementForSale(String search, String prefix) {
        StringBuilder result = new StringBuilder();
        String attribute = StringUtils.EMPTY;
        String attributeParam = StringUtils.EMPTY;
        boolean and = false;
        int id = 1;

        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                attribute = matcher.group(1);

                if (and)
                    result.append(AND);

                if (attribute.equalsIgnoreCase("transactionId")) {
                    result.append("(")
                            .append(appendCriteriaToQuery(prefix + ".ApplicationTransactionID", matcher.group(2),
                                    "applicationTransactionIdParam", matcher.group(3)))
                            .append(OR).append(appendCriteriaToQuery(prefix + ".ProcessorTransactionID",
                                    matcher.group(2), "processorTransactionIdParam", matcher.group(3)))
                            .append(")");
                } else if (attribute.equalsIgnoreCase("customer")) {
                    result.append("(")
                            .append(appendCriteriaToQuery(prefix + ".FirstName", matcher.group(2), "customerParam",
                                    matcher.group(3)))
                            .append(OR).append(appendCriteriaToQuery(prefix + ".LastName", matcher.group(2),
                                    "customerParam", matcher.group(3)))
                            .append(")");
                } else {
                    attributeParam = attribute + "Param" + id;
                    attribute = getPropertyNativeName(attribute);
                    result.append(appendCriteriaToQuery(prefix + "." + attribute, matcher.group(2), attributeParam,
                            matcher.group(3)));
                }
                and = true;
                id++;
            }
        }

        return result.length() == 0 ? result.toString() : " WHERE " + result.toString();
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
        StringBuilder result = new StringBuilder();
        String attribute = StringUtils.EMPTY;
        String attributeParam = StringUtils.EMPTY;
        boolean and = false;
        int id = 1;

        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                attribute = matcher.group(1);
                // Transaction type is not part of the query, this criteria is
                // filtered in the method getQueryByCriteria
                if (attribute.equalsIgnoreCase("transactionType")) {
                    continue;
                }

                if (prefix.equals("REFUND") || prefix.equals("VOID")) {
                    if (attribute.equalsIgnoreCase("accountNumber") || attribute.equalsIgnoreCase("amount")
                            || attribute.equalsIgnoreCase("cardType") || attribute.equalsIgnoreCase("legalEntity")
                            || attribute.equalsIgnoreCase("customer")) {
                        continue;
                    }
                } else {
                    if (attribute.equalsIgnoreCase("transactionId") || attribute.equalsIgnoreCase("internalStatusCode")
                            || attribute.equalsIgnoreCase("transactionDateTime")
                            || attribute.equalsIgnoreCase("processorName")) {
                        continue;
                    }
                }

                if (and)
                    result.append(AND);

                if (attribute.equalsIgnoreCase("transactionId")) {
                    result.append("(")
                            .append(appendCriteriaToQuery(prefix + ".ApplicationTransactionID", matcher.group(2),
                                    "applicationTransactionIdParam", matcher.group(3)))
                            .append(OR).append(appendCriteriaToQuery(prefix + ".ProcessorTransactionID",
                                    matcher.group(2), "processorTransactionIdParam", matcher.group(3)))
                            .append(")");
                } else if (attribute.equalsIgnoreCase("customer")) {
                    result.append("(")
                            .append(appendCriteriaToQuery(prefix + ".FirstName", matcher.group(2), "customerParam",
                                    matcher.group(3)))
                            .append(OR).append(appendCriteriaToQuery(prefix + ".LastName", matcher.group(2),
                                    "customerParam", matcher.group(3)))
                            .append(")");
                } else {
                    attributeParam = attribute + "Param" + id;
                    attribute = getPropertyNativeName(attribute);
                    result.append(appendCriteriaToQuery(prefix + "." + attribute, matcher.group(2), attributeParam,
                            matcher.group(3)));
                }
                and = true;
                id++;
            }
        }
        return result.length() == 0 ? result.toString() : " WHERE " + result.toString();
    }

    /**
     * This method will append all elements and will add it to the query and
     * also fills a hashmap with the parameter values. Special cases are
     * considered like LegalEntity array, Dates and Amount value. For legal
     * entities will add the clause IN, for dates and amount will change the
     * like for = or > or <
     * 
     * @param name
     * @param operator
     * @param param
     * @param value
     * @return element to be added to the query
     */
    private String appendCriteriaToQuery(String name, String operator, String param, String value) {
        StringBuilder inputCriteria = new StringBuilder();
        inputCriteria.append(name);
        if (name.contains("LegalEntityApp")) {
            inputCriteria.append(" IN (");
            inputCriteria.append(":");
            inputCriteria.append(param);
            inputCriteria.append(")");
            // replace all elements that are not words and commas leaving it
            // like MCM-R2K,AA-WARDATA
            dynamicParametersMap.put(param, value.replaceAll("[^\\w\\-\\,]", ""));
            return inputCriteria.toString();
        } else {
            if (name.contains("ChargeAmount") || name.contains("Date") || name.contains("InternalStatusCode")) {
                inputCriteria.append(getOperation(operator));
            } else if (name.contains("FirstName") || name.contains("LastName")) {
                inputCriteria.append(operator.equalsIgnoreCase(":") ? LIKE : operator);
            } else {
                inputCriteria.append(operator.equalsIgnoreCase(":") ? EQUALS : operator);
            }

        }
        inputCriteria.append(":").append(param);

        dynamicParametersMap.put(param, value);

        return inputCriteria.toString();
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
    private String getPropertyNativeName(String property) {
        String nativePropertyName = nativePropertyHashMapping.get(property);
        if (nativePropertyName == null) {
            LOGGER.error("Property not found, unable to parse {}", property);
            throw new CustomBadRequestException(String.format("Property not found, unable to parse [%s]", property));
        }
        return nativePropertyName;
    }

    /**
     * Loads the native names mapping the elements in the saletransaction entity
     */
    private void loadSaleTransactionMappings() {
        nativePropertyHashMapping.put("saleTransactionId", "SaleTransactionID");
        nativePropertyHashMapping.put("applicationTransactionId", "ApplicationTransactionID");
        nativePropertyHashMapping.put("processorTransactionId", "ProcessorTransactionID");
        nativePropertyHashMapping.put("customer", "FirstName");
        nativePropertyHashMapping.put("firstName", "FirstName");
        nativePropertyHashMapping.put("lastName", "LastName");
        nativePropertyHashMapping.put("processUser", "ProcessUser");
        nativePropertyHashMapping.put("transactionType", "TransactionType");
        nativePropertyHashMapping.put("address1", "Address1");
        nativePropertyHashMapping.put("address2", "Address2");
        nativePropertyHashMapping.put("city", "City");
        nativePropertyHashMapping.put("state", "State");
        nativePropertyHashMapping.put("postalCode", "PostalCode");
        nativePropertyHashMapping.put("country", "Country");
        nativePropertyHashMapping.put("cardNumberFirst6Char", "CardNumberFirst6Char");
        nativePropertyHashMapping.put("cardNumberLast4Char", "CardNumberLast4Char");
        nativePropertyHashMapping.put("cardType", "CardType");
        nativePropertyHashMapping.put("expiryDate", "ExpiryDate");
        nativePropertyHashMapping.put("legalEntity", "LegalEntityApp");
        nativePropertyHashMapping.put("accountNumber", "AccountId");
        nativePropertyHashMapping.put("merchantId", "MerchantID");
        nativePropertyHashMapping.put("processorName", "Processor");
        nativePropertyHashMapping.put("application", "Application");
        nativePropertyHashMapping.put("origin", "Origin");
        nativePropertyHashMapping.put("transactionDateTime", "TransactionDateTime");
        nativePropertyHashMapping.put("testMode", "TestMode");
        nativePropertyHashMapping.put("internalStatusCode", "InternalStatusCode");
        nativePropertyHashMapping.put("internalStatusDescription", "InternalStatusDescription");
        nativePropertyHashMapping.put("approvalCode", "ApprovalCode");
        nativePropertyHashMapping.put("amount", "ChargeAmount");
        nativePropertyHashMapping.put("responseCode", "ResponseCode");
        nativePropertyHashMapping.put("responseDescription", "ResponseDescription");
        nativePropertyHashMapping.put("transactionDateTime", "TransactionDateTime");
    }

    /**
     * Creates the select element based on the transaction type using an alias
     * for the sale transaction table, an alias for the REFUND/VOID table
     * 
     * @param alias
     * @param saleAlias
     * @param transactionType
     * @param tableName
     * @return String with the select based on the transaction type
     */
    private String createSelectFromTransactionTypeBased(String alias, String saleAlias, String transactionType,
            String tableName) {
        StringBuilder select = new StringBuilder(" SELECT ");

        select.append(alias).append(".SaleTransactionID,");
        select.append(alias).append(".ApplicationTransactionID,");
        select.append(alias).append(".ProcessorTransactionID,");
        select.append(alias).append(".MerchantID,");
        
        if("sale".equalsIgnoreCase(transactionType) || "tokenize".equalsIgnoreCase(transactionType)) {
            select.append(alias).append(".TransactionType,");
        } else {
            select.append("'" + transactionType + "' as TransactionType,");
        }
        select.append(alias).append(".Processor,");
        select.append(alias).append(".InternalStatusCode,");
        select.append(alias).append(".InternalStatusDescription,");
        select.append(alias).append(".DateCreated,");
        select.append(alias).append(".TransactionDateTime,");
        select.append(saleAlias).append(".ChargeAmount,");
        select.append(saleAlias).append(".FirstName,");
        select.append(saleAlias).append(".LastName,");
        select.append(saleAlias).append(".CardNumberLast4Char,");
        select.append(saleAlias).append(".CardType,");
        select.append(saleAlias).append(".LegalEntityApp,");
        select.append(saleAlias).append(".AccountId");
        if (saleAlias.equals("MAINSALE")) {
            select.append(
                    ",(SELECT Count(*) FROM void_transaction WHERE saletransactionid = MAINSALE.saletransactionid) AS IsVoided,");
            select.append(
                    "(SELECT Count(*) FROM refund_transaction WHERE  saletransactionid = MAINSALE.saletransactionid) AS IsRefunded");
        } else if (saleAlias.equals("VOIDSALE") || saleAlias.equals("REFUNDSALE")) {
            select.append(", 0 AS IsVoided, 0 AS IsRefunded");
        }
        select.append(" FROM ").append(tableName).append(alias).append(" ");

        return select.toString();
    }
}
