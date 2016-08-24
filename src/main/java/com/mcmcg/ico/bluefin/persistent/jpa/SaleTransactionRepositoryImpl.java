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

import com.mcmcg.ico.bluefin.model.StatusCode;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

class SaleTransactionRepositoryImpl implements TransactionRepositoryCustom {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaleTransactionRepositoryImpl.class);

    private static final String TRANSACTION_TYPE = "(transactionType)(:|<|>)([\\w]+)";
    private static final String EMAIL_PATTERN = "(\\w+?)@(\\w+?).(\\w+?)";
    private static final String NUMBER_LIST_REGEX = "\\[(\\d+)(,\\d+)*\\]";
    private static final String WORD_LIST_REGEX = "\\[(\\w+(-\\w+)?(,\\s?\\w+(-\\w+)?)*)*\\]";
    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
    private static final String NUMBERS_AND_WORDS_REGEX = "[\\w\\s|\\d+(?:\\.\\d+)?]+";
    private static final String SEARCH_REGEX = "(\\w+?)(:|<|>)" + "(" + DATE_REGEX + "|" + NUMBERS_AND_WORDS_REGEX + "|"
            + EMAIL_PATTERN + "|" + NUMBER_LIST_REGEX + "|" + WORD_LIST_REGEX + "),";

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

        dynamicParametersMap.clear();
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
            } else if (entry.getKey().contains("transactionDateTimeParam")) {

                if (!validFormatDate(entry.getValue())) {
                    throw new CustomNotFoundException(
                            "Unable to process find transaction, due an error with date formatting");
                }
                // Special case for the dates
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains("legalEntityParam")) {
                // Special case for legal entity
                String value = entry.getValue().replaceAll("[^\\w\\-\\,]", "");
                result.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
                queryTotal.setParameter(entry.getKey(), Arrays.asList(value.split(",")));
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

        // create select from transaction type
        querySb.append(
                " SELECT MAINSALE.SaleTransactionID,MAINSALE.ApplicationTransactionID,MAINSALE.ProcessorTransactionID,")
                .append("MAINSALE.MerchantID,MAINSALE.TransactionType,MAINSALE.Processor,MAINSALE.InternalStatusCode,")
                .append("MAINSALE.InternalStatusDescription,MAINSALE.DateCreated,MAINSALE.TransactionDateTime,MAINSALE.ChargeAmount,")
                .append("MAINSALE.FirstName,MAINSALE.LastName,MAINSALE.CardNumberLast4Char,MAINSALE.CardType,MAINSALE.LegalEntityApp,")
                .append("MAINSALE.AccountId,(SELECT Count(*) FROM void_transaction WHERE saletransactionid = MAINSALE.saletransactionid) AS IsVoided,")
                .append("(SELECT Count(*) FROM refund_transaction WHERE  saletransactionid = MAINSALE.saletransactionid) AS IsRefunded ")
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
                " SELECT VOID.SaleTransactionID,VOID.ApplicationTransactionID,VOID.ProcessorTransactionID,VOID.MerchantID,'VOID' as TransactionType,")
                .append("VOID.Processor,VOID.InternalStatusCode,VOID.InternalStatusDescription,VOID.DateCreated,VOID.TransactionDateTime,VOIDSALE.ChargeAmount,")
                .append("VOIDSALE.FirstName,VOIDSALE.LastName,VOIDSALE.CardNumberLast4Char,VOIDSALE.CardType,VOIDSALE.LegalEntityApp,VOIDSALE.AccountId,")
                .append("0 AS IsVoided, 0 AS IsRefunded FROM  Void_Transaction VOID ")

                .append(" JOIN (")

                .append(" SELECT SALEINNERVOID.SaleTransactionID,SALEINNERVOID.ApplicationTransactionID,SALEINNERVOID.ProcessorTransactionID,")
                .append("SALEINNERVOID.MerchantID,SALEINNERVOID.TransactionType,SALEINNERVOID.Processor,SALEINNERVOID.InternalStatusCode,")
                .append("SALEINNERVOID.InternalStatusDescription,SALEINNERVOID.DateCreated,SALEINNERVOID.TransactionDateTime,")
                .append("SALEINNERVOID.ChargeAmount,SALEINNERVOID.FirstName,SALEINNERVOID.LastName,SALEINNERVOID.CardNumberLast4Char,")
                .append("SALEINNERVOID.CardType,SALEINNERVOID.LegalEntityApp,SALEINNERVOID.AccountId FROM  Sale_Transaction SALEINNERVOID ")

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
                " SELECT REFUND.SaleTransactionID,REFUND.ApplicationTransactionID,REFUND.ProcessorTransactionID,REFUND.MerchantID,")
                .append("'REFUND' as TransactionType,REFUND.Processor,REFUND.InternalStatusCode,REFUND.InternalStatusDescription,")
                .append("REFUND.DateCreated,REFUND.TransactionDateTime,REFUNDSALE.ChargeAmount,REFUNDSALE.FirstName,REFUNDSALE.LastName,")
                .append("REFUNDSALE.CardNumberLast4Char,REFUNDSALE.CardType,REFUNDSALE.LegalEntityApp,REFUNDSALE.AccountId, 0 AS IsVoided,")
                .append(" 0 AS IsRefunded FROM  Refund_Transaction REFUND ")

                .append(" JOIN (")

                .append(" SELECT SALEINNERREFUND.SaleTransactionID,SALEINNERREFUND.ApplicationTransactionID,SALEINNERREFUND.ProcessorTransactionID,")
                .append("SALEINNERREFUND.MerchantID,SALEINNERREFUND.TransactionType,SALEINNERREFUND.Processor,SALEINNERREFUND.InternalStatusCode,")
                .append("SALEINNERREFUND.InternalStatusDescription,SALEINNERREFUND.DateCreated,SALEINNERREFUND.TransactionDateTime,SALEINNERREFUND.ChargeAmount,")
                .append("SALEINNERREFUND.FirstName,SALEINNERREFUND.LastName,SALEINNERREFUND.CardNumberLast4Char,SALEINNERREFUND.CardType,")
                .append("SALEINNERREFUND.LegalEntityApp,SALEINNERREFUND.AccountId FROM  Sale_Transaction SALEINNERREFUND ")

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
    private String createWhereStatement(String search, String prefix) {
        StringJoiner statement = new StringJoiner(" AND ");
        String attribute = StringUtils.EMPTY;
        String operator = StringUtils.EMPTY;
        String value = StringUtils.EMPTY;
        String attributeParam = StringUtils.EMPTY;
        String predicate = StringUtils.EMPTY;

        if (search != null && !search.isEmpty()) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");

            while (matcher.find()) {
                attribute = matcher.group(1);
                operator = matcher.group(2);
                value = matcher.group(3);
                attributeParam = attribute + "Param1";
                predicate = getPropertyPredicate(attribute);

                if (!prefix.equals("MAINSALE") && skipFilter(attribute, prefix)) {
                    continue;
                }
                // Specific cases for transactionDateTime, amount
                if (attribute.equals("transactionDateTime") || attribute.equals("amount")) {
                    predicate = predicate.replace(":atributeOperator", getOperation(operator));
                    if (dynamicParametersMap.containsKey(attribute + "Param1")) {
                        attributeParam = attribute + "Param2";
                        predicate = predicate.replace(attribute + "Param1", attributeParam);
                    }
                }
                statement.add(predicate.replace(":prefix", prefix));
                dynamicParametersMap.put(attributeParam, value);
            }
        }
        return statement.length() == 0 ? "" : " WHERE " + statement.toString();

    }

    public boolean skipFilter(String attribute, String prefix) {
        if (attribute.equalsIgnoreCase("transactionType")) {
            return true;
        }
        if (prefix.equals("REFUND") || prefix.equals("VOID")) {
            if (attribute.equalsIgnoreCase("accountNumber") || attribute.equalsIgnoreCase("amount")
                    || attribute.equalsIgnoreCase("cardType") || attribute.equalsIgnoreCase("legalEntity")
                    || attribute.equalsIgnoreCase("firstName") || attribute.equalsIgnoreCase("lastName")) {
                return true;
            }
        } else {
            if (attribute.equalsIgnoreCase("transactionId") || attribute.equalsIgnoreCase("internalStatusCode")
                    || attribute.equalsIgnoreCase("transactionDateTime")
                    || attribute.equalsIgnoreCase("processorName")) {
                return true;
            }
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
        predicatesHashMapping.put("firstName", ":prefix.FirstName =  :firstNameParam1");
        predicatesHashMapping.put("lastName", ":prefix.LastName = :lastNameParam1");
        predicatesHashMapping.put("cardType", ":prefix.CardType = :cardTypeParam1");
        predicatesHashMapping.put("legalEntity", ":prefix.LegalEntityApp IN (:legalEntityParam1)");
        predicatesHashMapping.put("accountNumber", ":prefix.AccountId = :accountNumberParam1");
    }
}
