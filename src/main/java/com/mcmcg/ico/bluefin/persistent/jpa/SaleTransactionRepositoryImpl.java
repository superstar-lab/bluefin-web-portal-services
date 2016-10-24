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
                String value = entry.getValue().replace("[", "").replace("]", "").replace(" ", "");

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
                    Long paymentProcessorId = paymentProcessorRepository.getPaymentProcessorByProcessorName(value)
                            .getPaymentProcessorId();
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

    @Override
    public Page<PaymentProcessorRemittance> findRemittanceSaleRefundTransactions(String search, PageRequest page,
            boolean negate) throws ParseException {

        // Creates the query for the total and for the retrieved data
        String query = getNativeQueryForRemittanceSaleRefund(search);

        // Currently this is only used if the user selects 'Not Reconcilied' on
        // the UI.
        // Change to: WHERE ReconciliationID != 'Reconciled'
        if (negate) {
            query = query.replaceAll("ppr.ReconciliationStatusID =", "ppr.ReconciliationStatusID !=");
        }

        Query result = em.createNativeQuery(page == null ? query : query + addSort(page.getSort()),
                "PaymentProcessorRemittanceCustomMappingResult");

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
    public List<PaymentProcessorRemittance> findRemittanceSaleRefundTransactionsReport(String search)
            throws ParseException {

        String query = getNativeQueryForRemittanceSaleRefund(search);
        LOGGER.info("Dynamic Query {}", query);

        Query result = em.createNativeQuery(query, "PaymentProcessorRemittanceCustomMappingResult");

        result.setMaxResults(Integer.parseInt(maxSizeReport));
        @SuppressWarnings("unchecked")
        List<PaymentProcessorRemittance> tr = result.getResultList();

        return tr;
    }

    private String getNativeQueryForRemittanceSaleRefund(String search) {

        String remittanceCreationDateBegin = null;
        String remittanceCreationDateEnd = null;
        String processorName = null;
        String[] legalEntityArray = null;
        String reconciliationStatusId = null;

        String[] searchArray = search.split("\\$\\$");

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
            if (parameter.startsWith("legalEntity")) {
                String temp = parameter.replaceAll("legalEntity:", "");
                String values = temp.replaceAll("\\[|\\]", "");
                legalEntityArray = values.split(",");
            }
            if (parameter.startsWith("reconciliationStatusId")) {
                String[] parameterArray = parameter.split(":");
                reconciliationStatusId = parameterArray[1];
            }
        }

        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT ppr.PaymentProcessorRemittanceID,ppr.DateCreated,ppr.ReconciliationStatusID,");
        querySb.append(
                "ppr.ReconciliationDate,ppr.PaymentMethod,ppr.TransactionAmount,ppr.TransactionType,ppr.TransactionTime,");
        querySb.append(
                "ppr.AccountID,ppr.Application,ppr.ProcessorTransactionID,ppr.MerchantID,ppr.TransactionSource,ppr.FirstName,");
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
        querySb.append("0 AS SaleIsVoided,0 AS SaleIsRefunded ");
        querySb.append("FROM PaymentProcessor_Remittance ppr ");
        querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
        querySb.append("LEFT JOIN Sale_Transaction st ON (ppr.ProcessorTransactionID = st.ProcessorTransactionID) ");
        querySb.append("WHERE ppr.RemittanceCreationDate >= '" + remittanceCreationDateBegin + "' ");
        querySb.append("AND ppr.RemittanceCreationDate <= '" + remittanceCreationDateEnd + "' ");
        querySb.append("AND (Upper(ppr.TransactionType) = 'SALE') ");
        if (processorName != null) {
            Long paymentProcessorId = paymentProcessorRepository.getPaymentProcessorByProcessorName(processorName)
                    .getPaymentProcessorId();
            querySb.append("AND ppr.PaymentProcessorID = " + paymentProcessorId.toString() + " ");
            querySb.append("AND st.Processor = '" + processorName + "' ");
        }
        if (legalEntityArray != null) {
            querySb.append("AND (st.LegalEntityApp IN (");
            for (int i = 0; i < legalEntityArray.length; i++) {
                querySb.append("'" + legalEntityArray[i] + "'");
                if (i != (legalEntityArray.length - 1)) {
                    querySb.append(", ");
                }
            }
            querySb.append(")) ");
        }
        querySb.append("UNION ");
        querySb.append(
                "SELECT ppr.PaymentProcessorRemittanceID,ppr.DateCreated,ppr.ReconciliationStatusID,ppr.ReconciliationDate,");
        querySb.append(
                "ppr.PaymentMethod,ppr.TransactionAmount,ppr.TransactionType,ppr.TransactionTime,ppr.AccountID,ppr.Application,");
        querySb.append(
                "ppr.ProcessorTransactionID,ppr.MerchantID,ppr.TransactionSource,ppr.FirstName,ppr.LastName,ppr.RemittanceCreationDate,");
        querySb.append(
                "ppr.PaymentProcessorID,ppl.ProcessorName AS ProcessorName,rt.SaleTransactionID AS SaleTransactionID,");
        querySb.append(
                "NULL AS SaleFirstName,NULL AS SaleLastName,NULL AS SaleProcessUser,NULL AS SaleTransactionType,NULL AS SaleAddress1,");
        querySb.append(
                "NULL AS SaleAddress2,NULL AS SaleCity,NULL AS SaleState,NULL AS SalePostalCode,NULL AS SaleCountry,NULL AS SaleCardNumberFirst6Char,");
        querySb.append(
                "NULL AS SaleCardNumberLast4Char,NULL AS SaleCardType,NULL AS SaleExpiryDate,NULL AS SaleToken,NULL AS SaleChargeAmount,");
        querySb.append(
                "NULL AS SaleLegalEntityApp,NULL AS SaleAccountId,rt.ApplicationTransactionID AS SaleApplicationTransactionID,rt.MerchantID AS SaleMerchantID,");
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
        querySb.append("0 AS SaleIsVoided,0 AS SaleIsRefunded ");
        querySb.append("FROM PaymentProcessor_Remittance ppr ");
        querySb.append("JOIN PaymentProcessor_Lookup ppl ON (ppr.PaymentProcessorID = ppl.PaymentProcessorID) ");
        querySb.append("LEFT JOIN Refund_Transaction rt ON (ppr.ProcessorTransactionID = rt.ProcessorTransactionID) ");
        querySb.append("WHERE ppr.RemittanceCreationDate >= '" + remittanceCreationDateBegin + "' ");
        querySb.append("AND ppr.RemittanceCreationDate <= '" + remittanceCreationDateEnd + "' ");
        querySb.append("AND (Upper(ppr.TransactionType) = 'REFUND') ");
        if (processorName != null) {
            Long paymentProcessorId = paymentProcessorRepository.getPaymentProcessorByProcessorName(processorName)
                    .getPaymentProcessorId();
            querySb.append("AND ppr.PaymentProcessorID = " + paymentProcessorId.toString() + " ");
            querySb.append("AND rt.Processor = '" + processorName + "' ");
        }
        querySb.append("UNION ");
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
                "SALE.ApplicationTransactionID,SALE.MerchantID,SALE.Processor,SALE.Application,SALE.Origin,SALE.ProcessorTransactionID,SALE.TransactionDateTime,");
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
        querySb.append("0 AS SaleIsVoided,0 AS SaleIsRefunded ");
        querySb.append("FROM Sale_Transaction SALE ");
        querySb.append("WHERE SALE.ReconciliationDate >= DATEADD(DAY,-2,'" + remittanceCreationDateBegin
                + "') AND SALE.ReconciliationDate <= DATEADD(DAY,-1,'" + remittanceCreationDateBegin + "') ");
        if (processorName != null) {
            querySb.append("AND SALE.Processor = '" + processorName + "' ");
        }
        if (legalEntityArray != null) {
            querySb.append("AND (SALE.LegalEntityApp IN (");
            for (int i = 0; i < legalEntityArray.length; i++) {
                querySb.append("'" + legalEntityArray[i] + "'");
                if (i != (legalEntityArray.length - 1)) {
                    querySb.append(", ");
                }
            }
            querySb.append(")) ");
        }
        if (reconciliationStatusId != null) {
            querySb.append("AND SALE.ReconciliationStatusID = " + reconciliationStatusId + " ");
        }
        querySb.append("UNION ");
        querySb.append(
                "SELECT NULL AS PaymentProcessorRemittanceID,NULL AS DateCreated,NULL AS ReconciliationStatusID,NULL AS ReconciliationDate,NULL AS PaymentMethod,");
        querySb.append(
                "NULL AS TransactionAmount,NULL AS TransactionType,NULL AS TransactionTime,NULL AS AccountID,NULL AS Application,NULL AS ProcessorTransactionID,");
        querySb.append(
                "NULL AS MerchantID,NULL AS TransactionSource,NULL AS FirstName,NULL AS LastName,NULL AS RemittanceCreationDate,NULL AS PaymentProcessorID,");
        querySb.append(
                "NULL AS ProcessorName,REFUND.SaleTransactionID,NULL AS RefundFirstName,NULL AS RefundLastName,NULL AS RefundProcessUser,NULL AS RefundTransactionType,");
        querySb.append(
                "NULL AS RefundAddress1,NULL AS RefundAddress2,NULL AS RefundCity,NULL AS RefundState,NULL AS RefundPostalCode,NULL AS RefundCountry,");
        querySb.append(
                "NULL AS RefundCardNumberFirst6Char,NULL AS RefundCardNumberLast4Char,NULL AS RefundCardType,NULL AS RefundExpiryDate,NULL AS RefundToken,");
        querySb.append(
                "NULL AS RefundChargeAmount,NULL AS RefundLegalEntityApp,NULL AS RefundAccountId,REFUND.ApplicationTransactionID,REFUND.MerchantID,");
        querySb.append(
                "REFUND.Processor,REFUND.Application,NULL AS RefundOrigin,REFUND.ProcessorTransactionID,REFUND.TransactionDateTime,NULL AS RefundTestMode,");
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
                "REFUND.ReconciliationStatusID,REFUND.ReconciliationDate,NULL AS RefundBatchUploadID,0 AS REFUNDIsVoided,0 AS REFUNDIsRefunded ");
        querySb.append("FROM REFUND_Transaction REFUND ");
        querySb.append("WHERE REFUND.ReconciliationDate >= DATEADD(DAY,-2,'" + remittanceCreationDateBegin
                + "') AND REFUND.ReconciliationDate <= DATEADD(DAY,-1,'" + remittanceCreationDateBegin + "') ");
        if (processorName != null) {
            querySb.append("AND REFUND.Processor = '" + processorName + "' ");
        }
        if (reconciliationStatusId != null) {
            querySb.append("AND REFUND.ReconciliationStatusID = " + reconciliationStatusId + " ");
        }

        return querySb.toString();
    }
}
