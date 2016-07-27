package com.mcmcg.ico.bluefin.persistent.jpa;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.google.common.collect.ArrayListMultimap;
import com.mcmcg.ico.bluefin.model.StatusCode;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;

class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final String TRANSACTION_TYPE = "(transactionType)(:|<|>)([\\w]+)";
    private static final String EMAIL_PATTERN = "(\\w+?)@(\\w+?).(\\w+?)";
    private static final String NUMBER_LIST_REGEX = "\\[(\\d+)(,\\d+)*\\]";
    public static final String WORD_LIST_REGEX = "\\[(\\w+(-\\w+)?(,\\s?\\w+(-\\w+)?)*)*\\]";
    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private static final String NUMBERS_AND_WORDS_REGEX = "[\\w\\s|\\d+(?:\\.\\d+)?]+";
    private static final String SEARCH_REGEX = "(\\w+?)(:|<|>)" + "(" + DATE_REGEX + "|" + NUMBERS_AND_WORDS_REGEX + "|"
            + EMAIL_PATTERN + "|" + NUMBER_LIST_REGEX + "|" + WORD_LIST_REGEX + "),";

    private static final String LIKE = " LIKE ";
    private static final String EQUALS = " = ";
    private static final String OR = " OR ";
    private static final String AND = " AND ";
    
    private static final String SALE_TABLE = " Sale_Transaction ";
    private static final String VOID_TABLE = " Void_Transaction ";
    private static final String REFUND_TABLE = " Refund_Transaction ";

    private HashMap<String, String> map = new HashMap<String, String>();
    private HashMap<String, String> nativePropertyHashMapping = new HashMap<String, String>();
    
    @Override
    public Page<SaleTransaction> findTransaction(String search, PageRequest page) {
        loadSaleTransactionMapping();
        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
       //Creates the query for the total and for the retrieved data
        String query = getQueryByCriteria(search);
       
        Query queryTotal = em
                .createNativeQuery("SELECT count(finalCount.ApplicationTransactionID) from (" + query + ") finalCount");
        Query result = em.createNativeQuery(query + addSort(page.getSort()), "CustomMappingResult");
        
        // Sets all parameters to the Query result
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().contains("amountParam")) {
                result.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), new BigDecimal(entry.getValue()));
            } else if (entry.getKey().contains("transactionDateTimeParam")//Special case for the dates
                    || entry.getKey().contains("createdDateParam")) {
                try {
                    result.setParameter(entry.getKey(), getDateFormat(entry.getValue().toString()));
                    queryTotal.setParameter(entry.getKey(), getDateFormat(entry.getValue().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (entry.getKey().contains("legalEntityParam")) {//Special case for legal entity
                result.setParameter(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
                queryTotal.setParameter(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
            } else if (entry.getKey().contains("transactionStatusCodeParam")) {//Special case for status code
                result.setParameter(entry.getKey(), StatusCode.getStatusCodeByString(entry.getValue()));
                queryTotal.setParameter(entry.getKey(), StatusCode.getStatusCodeByString(entry.getValue()));
            } else {
                result.setParameter(entry.getKey(), entry.getValue());
                queryTotal.setParameter(entry.getKey(), entry.getValue());
            }
        }
        map.clear();

        //Set the paging for the created select
        Integer countResult = (Integer) queryTotal.getSingleResult();
        pageNumber = (int) ((countResult / pageSize) + 1);

        result.setFirstResult((pageNumber - 1) * pageSize);
        result.setMaxResults(pageSize);
        
        //Brings the data and transform it into a Page value list
        List<SaleTransaction> tr = result.getResultList();
        Page<SaleTransaction> list = new PageImpl<SaleTransaction>(tr, page, countResult);
        return list;
    }
    
    /**
     * Creates the sort value according with the sort object given
     * @param sort
     * @return String with the sort for the query
     */
    private String addSort(Sort sort){
        StringBuilder result = new StringBuilder(" ORDER BY ");
        Iterator<Order> list = sort.iterator();
        Order order = null;
        while (list.hasNext()) {
            order = list.next(); 
            result.append(getPropertyNativeName(order.getProperty()));
            result.append(" ");
            result.append(order.getDirection().toString()); 
            if(list.hasNext()) {
                result.append(", "); 
            } else  {
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
    private String getQueryByCriteria(String search){
        String transaction = getTransactionType(search);
        StringBuilder querySb = new StringBuilder(); 
        querySb.append(" SELECT * FROM ("); 
        switch (transaction.toLowerCase()) {
            case "sale": {
                querySb.append(getSelectForSaleTransaction(search)); 
            }break;
            case "void": {
                querySb.append(getSelectForVoidTransaction(search));
            } break;
            case "refund": {
                querySb.append(getSelectForRefundTransaction(search));
            }break;
            default: {
                querySb.append(getSelectForSaleTransaction(search));
                querySb.append(" UNION "); 
                querySb.append(getSelectForVoidTransaction(search));
                querySb.append(" UNION "); 
                querySb.append(getSelectForRefundTransaction(search));
            }break;
        }
        querySb.append(" ) RESULTINFO "); 
        
        return querySb.toString();
    }
    
    /**
     * Creates the select for tha table SALE_TRANSACTION
     * @param search
     * @return String with the select of the sale transaction table
     */
    private String getSelectForSaleTransaction(String search) {
        StringBuilder querySb = new StringBuilder(); 
        querySb.append(createSelectFromTransactionTypeBased("MAINSALE","MAINSALE","SALE",SALE_TABLE));
        querySb.append(createWhereStatement(search,"MAINSALE"));
        return querySb.toString();
       
    }
    
    /**
     * Creates the select for tha table VOID_TRANSACTION
     * @param search
     * @return String with the select of the void transaction table
     */
    private String getSelectForVoidTransaction(String search) {
        StringBuilder querySb = new StringBuilder(); 
        querySb.append(createSelectFromTransactionTypeBased("VOID","VOIDSALE","VOID",VOID_TABLE));
        querySb.append(" JOIN (");
        
            querySb.append(createSelectFromTransactionTypeBased("SALEINNERVOID","SALEINNERVOID","SALE",SALE_TABLE));
            querySb.append(createWhereStatement(search,"SALEINNERVOID")); 
        
        querySb.append(" ) VOIDSALE ");

        querySb.append("  ON ");
        querySb.append(" (VOID.saleTransactionID =  VOIDSALE.saleTransactionID) ");
        
        return querySb.toString();
       
    }
    /**
     * Creates the select for tha table REFUND_TRANSACTION
     * @param search
     * @return String with the select of the refund transaction table
     */
    private String getSelectForRefundTransaction(String search) {
        StringBuilder querySb = new StringBuilder(); 
        querySb.append(createSelectFromTransactionTypeBased("REFUND","REFUNDSALE","REFUND",REFUND_TABLE));
        querySb.append(" JOIN (");
        
            querySb.append(createSelectFromTransactionTypeBased("SALEINNERREFUND","SALEINNERREFUND","SALE",SALE_TABLE));
            querySb.append(createWhereStatement(search,"SALEINNERREFUND"));
        
        querySb.append(" ) REFUNDSALE ");
        querySb.append("  ON ");
        querySb.append(" (REFUND.saleTransactionID =  REFUNDSALE.saleTransactionID) ");
        
        return querySb.toString();
       
    }
    
    /**
     * Reaches for the transaction type element in the search and returns type.
     * If VOID is in the element transactionType, VOID will be returned, same case for SALE and REFUND
     * All will returned if this cases are not found
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
     * the search string.
     * Specials cases are taken into account, like
     * transactionId, this element will create an OR with the attributes
     * applicationTransactionId and processorTransactionId if found
     * 
     * @param search
     * @param prefix
     * @return where element that is going to be attached to the select element
     */
    private String createWhereStatement(String search,String prefix) {
        StringBuilder result = new StringBuilder(" WHERE ");
        String attribute = StringUtils.EMPTY;
        String attributeParam = StringUtils.EMPTY; 
        boolean and = false;
        int id = 1;
        
        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");
           
            while (matcher.find()) {
                attribute = matcher.group(1);
               //Transaction type is not part of the query, this criteria is filtered in the method getQueryByCriteria
                if(attribute.equalsIgnoreCase("transactionType")){
                    continue;
                }
                if (and) result.append(AND);
                
                if (attribute.equals("transactionId")) {
                    result.append(appendCriteriaToQuery(prefix + ".ApplicationTransactionID", matcher.group(2),
                            "applicationTransactionIdParam", matcher.group(3)));
                    result.append(OR);
                    result.append(appendCriteriaToQuery(prefix + ".ProcessorTransactionID", matcher.group(2),
                            "processorTransactionIdParam", matcher.group(3)));
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
        return result.toString();
    }

    /**
     * This method will append all elements and will add it to the query and
     * also fills a hashmap with the parameter values.
     * Special cases are considered like LegalEntity array, Dates and Amount value. For legal entities will add the clause IN, for dates and amount will change the like for = or > or <
     * @param name
     * @param operator
     * @param param
     * @param value
     * @return element to be added to the query
     */
    private String appendCriteriaToQuery(String name, String operator, String param, String value) {
        StringBuilder inputCriteria = new StringBuilder();
        inputCriteria.append(name);
        if(name.contains("LegalEntityApp")){
            inputCriteria.append(" IN (");
            inputCriteria.append(":");
            inputCriteria.append(param);
            inputCriteria.append(")");
            //replace all elements that are not words and commas leaving it like MCM-R2K,AA-WARDATA
            map.put(param, value.replaceAll("[^\\w\\-\\,]",""));
            return inputCriteria.toString();
        } else {
            if(name.contains("ChargeAmount") || name.contains("Date") || name.contains("StatusCode")){
                inputCriteria.append(operator.equalsIgnoreCase(":") ? EQUALS : operator+"=");
            } else {
                inputCriteria.append(operator.equalsIgnoreCase(":") ? LIKE : operator);
            }
            
        }
        inputCriteria.append(":");
        inputCriteria.append(param);
        map.put(param, value);
        return inputCriteria.toString();
    }
     
    /**
     * Gives format to the dates
     * @param dateInString
     * @return date with the right format to be entered in the parameters hash
     * @throws ParseException
     */
    private Date getDateFormat(String dateInString) throws ParseException{ 
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
       return formatter.parse(dateInString);
    }
    
    /**
     * Gives the name of the element in the entity with the ones in the data base (native elements)
     * @param property
     * @return Native name of the element passed by parameter
     */
    private String getPropertyNativeName(String property){
        String nativePropertyName = nativePropertyHashMapping.get(property);
        return nativePropertyName;
    }
    
    /**
     * Loads the native names mapping the elements in the saletransaction entity
     */
    private void loadSaleTransactionMapping() {
        nativePropertyHashMapping.put("saleTransactionId", "SaleTransactionID");
        nativePropertyHashMapping.put("applicationTransactionId", "ApplicationTransactionID");
        nativePropertyHashMapping.put("processorTransactionId", "ProcessorTransactionID");
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
        nativePropertyHashMapping.put("transactionStatusCode", "StatusCode");
        nativePropertyHashMapping.put("statusDescription", "StatusDescription");
        nativePropertyHashMapping.put("approvalCode", "ApprovalCode");
        nativePropertyHashMapping.put("amount", "ChargeAmount");
        nativePropertyHashMapping.put("responseCode", "ResponseCode");
        nativePropertyHashMapping.put("responseDescription", "ResponseDescription");
        nativePropertyHashMapping.put("createdDate", "DateCreated");
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
    private String createSelectFromTransactionTypeBased (String alias, String saleAlias,String transactionType,String tableName) {
        StringBuilder select = new StringBuilder( " SELECT ");
        select.append(alias);select.append(".SaleTransactionID,");
        select.append(alias);select.append(".ApplicationTransactionID,");
        select.append(alias);select.append(".ProcessorTransactionID,");
        select.append(alias);select.append(".MerchantID,");
        select.append("'" + transactionType +"' as TransactionType,");
        select.append(alias);select.append(".Processor,");
        select.append(alias);select.append(".StatusCode,");
        select.append(alias);select.append(".DateCreated,");
        select.append(alias);select.append(".TransactionDateTime,");
        select.append(saleAlias);select.append(".ChargeAmount,");
        select.append(saleAlias);select.append(".FirstName,");
        select.append(saleAlias);select.append(".LastName,");
        select.append(saleAlias);select.append(".CardNumberLast4Char,");
        select.append(saleAlias);select.append(".CardType,");
        select.append(saleAlias);select.append(".LegalEntityApp,");
        select.append(saleAlias);select.append(".AccountId");
        select.append(" FROM ");
        select.append(tableName);
        select.append(alias);
        select.append(" ");
                
        
        return  select.toString();
        
    }
}
