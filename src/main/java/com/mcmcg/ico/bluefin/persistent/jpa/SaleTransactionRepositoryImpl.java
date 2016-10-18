package com.mcmcg.ico.bluefin.persistent.jpa;

import java.math.BigDecimal;
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

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

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

    @Autowired
    private PaymentProcessorRepository paymentProcessorRepository;

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

    private List<String> getOriginFromPaymentFrequency(String paymentFrequency) {
        Query queryTotal = em
                .createNativeQuery("SELECT Origin FROM OriginPaymentFrequency_Lookup where PaymentFrequency = lower('"
                        + paymentFrequency + "')");
        @SuppressWarnings("unchecked")
        List<String> origins = queryTotal.getResultList();
        return origins;
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
            } else if (entry.getKey().contains("transactionDateTimeParam")
                    || (entry.getKey().contains("remittanceCreationDate"))) {

                if (!validFormatDate(entry.getValue())) {
                    throw new CustomNotFoundException(
                            "Unable to process find transaction, due an error with date formatting");
                }
                // Special case for the dates
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            } else if (entry.getKey().contains("legalEntityParam")
                    || entry.getKey().contains("paymentFrequencyParam")) {
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
                .append("(SELECT Count(*) FROM Void_Transaction WHERE Saletransactionid = MAINSALE.Saletransactionid AND InternalStatusCode = '1') AS IsVoided,")
                .append("(SELECT Count(*) FROM Refund_Transaction WHERE Saletransactionid = MAINSALE.Saletransactionid AND InternalStatusCode = '1') AS IsRefunded, ")
                .append("MAINSALE.PaymentProcessorInternalStatusCodeID, MAINSALE.PaymentProcessorInternalResponseCodeID, MAINSALE.ReconciliationStatusID, MAINSALE.ReconciliationDate, MAINSALE.BatchUploadID ")
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
                        value = paymentProcessorRepository.findByPaymentProcessorId(Long.parseLong(value))
                                .getProcessorName();
                        predicate = predicate.replace("PaymentProcessorID", "Processor");
                        predicate = predicate.replace(attribute, "processorName");
                    }
                } else if (attribute.equalsIgnoreCase("paymentFrequency")) {
                    // Specific case for paymentFrequency, when paymentFrequency
                    // is NOT 'Recurring' then we need to search by all the
                    // values except 'Recurring'
                    value = getOriginFromPaymentFrequency(value.toLowerCase()).toString().toLowerCase();
                } else if (prefix.equals("ppr") && attribute.equalsIgnoreCase("processorName")) {
                    Long paymentProcessorId = paymentProcessorRepository.getPaymentProcessorByProcessorName(value).getPaymentProcessorId();
                    value = paymentProcessorId.toString();
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
        if ((prefix.equals("st") && attribute.equalsIgnoreCase("processorName")) || (prefix.equals("ppr") && attribute.equalsIgnoreCase("processorName"))) {
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
        predicatesHashMapping.put("processUser", ":prefix.ProcessUser = :processUserParam1");
        predicatesHashMapping.put("batchUploadId", ":prefix.BatchUploadID = :batchUploadIdParam1"); // This
                                                                                                    // is
                                                                                                    // ONLY
                                                                                                    // for
                                                                                                    // sale
        predicatesHashMapping.put("pUser", ":prefix.pUser = :pUserParam1"); // This
                                                                            // is
                                                                            // ONLY
                                                                            // for
                                                                            // void
                                                                            // and
                                                                            // refund
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
    }

    public Map<String, Query> createRemittanceQueries(String query, PageRequest page) throws ParseException {

        Query queryTotal = em
                .createNativeQuery("SELECT COUNT(finalCount.ProcessorTransactionID) FROM (" + query + ") finalCount");
        Query result = em.createNativeQuery(page == null ? query : query + addSort(page.getSort()),
                "PaymentProcessorRemittanceCustomMappingResult");

        LOGGER.info("Dynamic Parameters {}", dynamicParametersMap);

        // Sets all parameters to the Query result
        for (Map.Entry<String, String> entry : dynamicParametersMap.entrySet()) {
            if (entry.getKey().contains("amountParam")) {
                result.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
            } else if (entry.getKey().contains("transactionDateTimeParam")
                    || (entry.getKey().contains("remittanceCreationDate"))) {

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

    private String getQueryForRemittanceSaleRefundVoid(String search) {
        StringBuilder querySb = new StringBuilder();
        querySb.append(" SELECT * FROM (");
        querySb.append(
                " SELECT ppr.PaymentProcessorRemittanceID,ppr.DateCreated,ppr.ReconciliationStatusID,ppr.ReconciliationDate,ppr.PaymentMethod,ppr.TransactionAmount,ppr.TransactionType,")
                .append("ppr.TransactionTime,ppr.AccountID,ppr.Application,ppr.ProcessorTransactionID,ppr.MerchantID,ppr.TransactionSource,ppr.FirstName,ppr.LastName,")
                .append("ppr.RemittanceCreationDate,ppr.PaymentProcessorID, ppl.ProcessorName AS ProcessorName,")
                .append("st.SaleTransactionID AS SaleTransactionID,st.FirstName AS SaleFirstName,st.LastName AS SaleLastName,st.ProcessUser AS SaleProcessUser,st.TransactionType AS SaleTransactionType,")
                .append("st.Address1 AS SaleAddress1,st.Address2 AS SaleAddress2,st.City AS SaleCity,st.State AS SaleState,st.PostalCode AS SalePostalCode,st.Country AS SaleCountry,")
                .append("st.CardNumberFirst6Char AS SaleCardNumberFirst6Char,st.CardNumberLast4Char AS SaleCardNumberLast4Char,st.CardType AS SaleCardType,st.ExpiryDate AS SaleExpiryDate,st.Token AS SaleToken,")
                .append("st.ChargeAmount AS SaleChargeAmount,st.LegalEntityApp AS SaleLegalEntityApp,st.AccountId AS SaleAccountId,st.ApplicationTransactionID AS SaleApplicationTransactionID,")
                .append("st.MerchantID AS SaleMerchantID,st.Processor AS SaleProcessor,st.Application AS SaleApplication,st.Origin AS SaleOrigin,st.ProcessorTransactionID AS SaleProcessorTransactionID,")
                .append("st.TransactionDateTime AS SaleTransactionDateTime,st.TestMode AS SaleTestMode,st.ApprovalCode AS SaleApprovalCode,st.Tokenized AS SaleTokenized,st.PaymentProcessorStatusCode AS SalePaymentProcessorStatusCode,")
                .append("st.PaymentProcessorStatusCodeDescription AS SalePaymentProcessorStatusCodeDescription,st.PaymentProcessorResponseCode AS SalePaymentProcessorResponseCode,")
                .append("st.PaymentProcessorResponseCodeDescription AS SalePaymentProcessorResponseCodeDescription,st.InternalStatusCode AS SaleInternalStatusCode,st.InternalStatusDescription AS SaleInternalStatusDescription,")
                .append("st.InternalResponseCode AS SaleInternalResponseCode,st.InternalResponseDescription AS SaleInternalResponseDescription,st.PaymentProcessorInternalStatusCodeID AS SalePaymentProcessorInternalStatusCodeID,")
                .append("st.PaymentProcessorInternalResponseCodeID AS SalePaymentProcessorInternalResponseCodeID,st.DateCreated AS SaleDateCreated,st.PaymentProcessorRuleID AS SalePaymentProcessorRuleID,")
                .append("st.RulePaymentProcessorID AS SaleRulePaymentProcessorID,st.RuleCardType AS SaleRuleCardType,st.RuleMaximumMonthlyAmount AS SaleRuleMaximumMonthlyAmount,st.RuleNoMaximumMonthlyAmountFlag AS SaleRuleNoMaximumMonthlyAmountFlag,")
                .append("st.RulePriority AS SaleRulePriority,st.AccountPeriod AS SaleAccountPeriod,st.Desk AS SaleDesk,st.InvoiceNumber AS SaleInvoiceNumber,st.UserDefinedField1 AS SaleUserDefinedField1,st.UserDefinedField2 AS SaleUserDefinedField2,")
                .append("st.UserDefinedField3 AS SaleUserDefinedField3,st.ReconciliationStatusID AS SaleReconciliationStatusID,st.ReconciliationDate AS SaleReconciliationDate,st.BatchUploadID AS SaleBatchUploadID,0 AS SaleIsVoided,0 AS SaleIsRefunded ")
                .append("FROM PaymentProcessor_Remittance ppr ").append("JOIN PaymentProcessor_Lookup ppl ")
                .append("ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
        querySb.append(getSaleRefundVoidQueryForRemittance(search));

        querySb.append(createWhereStatement(search, "ppr"));
        querySb.append(" ) RESULTINFO ");

        return querySb.toString();
    }

    private String getSaleRefundVoidQueryForRemittance(String search) {
        StringBuilder querySb = new StringBuilder();

        querySb.append(" FULL JOIN (");
        querySb.append(getSelectForSaleTransaction(search));
        querySb.append(" UNION ");
        querySb.append(getSelectForVoidTransaction(search));
        querySb.append(" UNION ");
        querySb.append(getSelectForRefundTransaction(search));
        querySb.append(") st");
        querySb.append(" ON (ppr.ProcessorTransactionID = st.ProcessorTransactionID)");

        return querySb.toString();
    }

    @Override
    public Page<PaymentProcessorRemittance> findRemittanceSaleRefundVoidTransactions(String search, PageRequest page,
            boolean negate) throws ParseException {

        // Creates the query for the total and for the retrieved data
        String query = getQueryForRemittanceSaleRefundVoid(search);

        // Currently this is only used if the user selects 'Not Reconcilied' on
        // the UI.
        // Change to: WHERE ReconciliationID != 'Reconciled'
        if (negate) {
            query = query.replaceAll("ppr.ReconciliationStatusID =", "ppr.ReconciliationStatusID !=");
        }
        
        // For payment processor remittance, processorName is a filter.
        // processorName is set as Processor, which is correct for sales and related tables,
        // however, it needs to be changed to processorName for payment processor remittance.
        // Otherwise there will be an invalid column error.
        if (query.contains("ppr.Processor")) {
            query = query.replaceAll("ppr.Processor =", "ppr.PaymentProcessorID =");
        }

        Map<String, Query> queriesMap = createRemittanceQueries(query, page);
        Query result = queriesMap.get("result");

        // Brings the data and transform it into a Page value list
        @SuppressWarnings("unchecked")
        List<PaymentProcessorRemittance> tr = result.getResultList();

        int countResult = tr.size();
        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();

        List<PaymentProcessorRemittance> onePage = new ArrayList<PaymentProcessorRemittance>();
        int index = pageSize * pageNumber;
        int increment = pageSize;
        // Check upper bound to avoid IndexOutOfBoundsException
        if ((index + increment) > countResult) {
            int adjustment = (index + increment) - countResult;
            increment -= adjustment;
        }
        for (int i = index; i < (index + increment); i++) {
            onePage.add(tr.get(i));
        }

        Page<PaymentProcessorRemittance> list = new PageImpl<PaymentProcessorRemittance>(onePage, page, countResult);

        return list;
    }

    @Override
    public List<PaymentProcessorRemittance> findRemittanceSaleRefundVoidTransactionsReport(String search)
            throws ParseException {
        String query = getQueryForRemittanceSaleRefundVoid(search);
        LOGGER.info("Dynamic Query {}", query);

        Map<String, Query> queriesMap = createRemittanceQueries(query, null);
        Query result = queriesMap.get("result");

        result.setMaxResults(Integer.parseInt(maxSizeReport));
        @SuppressWarnings("unchecked")
        List<PaymentProcessorRemittance> tr = result.getResultList();

        return tr;
    }
}
