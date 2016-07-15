package com.mcmcg.ico.bluefin.service.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.access.AccessDeniedException;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

public class QueryDSLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDSLUtil.class);

    private static final String SORT_REGEX = "(\\w+?)(:)(\\w+?),";
    private static final String EMAIL_PATTERN = "(\\w+?)@(\\w+?).(\\w+?)";
    private static final String ANY_LIST_REGEX = "\\[(.*?)\\]";
    private static final String NUMBER_LIST_REGEX = "\\[(\\d+)(,\\d+)*\\]";
    public static final String WORD_LIST_REGEX = "\\[(\\w+(-\\w+)?(,\\s?\\w+(-\\w+)?)*)*\\]";
    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
    private static final String NUMBERS_AND_WORDS_REGEX = "[\\w\\s|\\d+(?:\\.\\d+)?]+";
    private static final String SEARCH_REGEX = "(\\w+?)(:|<|>)" + "(" + DATE_REGEX + "|" + NUMBERS_AND_WORDS_REGEX + "|"
            + EMAIL_PATTERN + "|" + NUMBER_LIST_REGEX + "|" + WORD_LIST_REGEX + "),";

    private static final String LEGAL_ENTITY_FILTER = "legalEntity:";
    private static final String LEGAL_ENTITIES_FILTER = "legalEntities:";

    public static BooleanExpression createExpression(String search, Class<?> entity) {
        PredicatesBuilder builder = new PredicatesBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");
            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }
        return builder.build(entity);
    }

    public static PageRequest getPageRequest(int page, int size, String sort) {
        List<Order> orderList = getOrderList(sort);
        if (orderList.isEmpty()) {
            return new PageRequest(page, size);
        } else {
            Sort finalSort = new Sort(orderList);
            return new PageRequest(page, size, finalSort);
        }
    }

    private static List<Order> getOrderList(String sort) {
        Pattern pattern = Pattern.compile(SORT_REGEX);
        Matcher matcher = pattern.matcher(sort + ",");
        List<Order> sortList = new ArrayList<Order>();
        while (matcher.find()) {
            Sort.Direction sortDirection = null;
            switch (matcher.group(3)) {
            case "asc":
                sortDirection = Sort.Direction.ASC;
                break;
            case "desc":
                sortDirection = Sort.Direction.DESC;
                break;
            }
            sortList.add(new Order(sortDirection, matcher.group(1)));
        }
        return sortList;
    }

    /**
     * Validates by filter given. Right now the validation can be achieved by id
     * or name, this method will allow to pass the filter and execute the
     * validation of the search criteria.
     * 
     * @param search
     * @param userLegalEntities
     * @param filterKey
     * @return
     */
    private static String validateByFilter(String search, List<String> userLegalEntities, String filterKey) {
        if (!search.contains(filterKey)) {
            if (!search.isEmpty()) {
                search = search + ",";
            }
            search = search + filterKey + userLegalEntities;
        } else {
            String LEFilterValue = getLEFilterValue(search, filterKey);
            search = search.replace(filterKey + LEFilterValue,
                    filterKey + generateValidLEFilter(LEFilterValue, userLegalEntities));
        }
        return search;
    }

    /**
     * Checks the validity of the search criteria by checking the current legal
     * entities owned by the consultant user and the ones provided. This
     * verification will be executed by using LE id If the user do not have one
     * LE, an access denied exception will be raised
     * 
     * @param userLE
     * @param search
     * @return String with the valid search criteria
     */
    public static String getValidSearchBasedOnLegalEntitiesById(List<LegalEntityApp> userLE, String search) {
        List<String> userLEIds = userLE.stream().map(current -> current.getLegalEntityAppId())
                .collect(Collectors.toList()).stream().map(i -> i.toString()).collect(Collectors.toList());

        return validateByFilter(search, userLEIds, LEGAL_ENTITIES_FILTER);
    }

    /**
     * Checks the validity of the search criteria by checking the current legal
     * entities owned by the consultant user and the ones provided. This
     * verification will be executed by using LE names
     * 
     * @param userLE
     * @param search
     * @return String with the valid search criteria
     */
    public static String getValidSearchBasedOnLegalEntities(List<LegalEntityApp> userLE, String search) {
        List<String> userLENames = userLE.stream().map(current -> current.getLegalEntityAppName())
                .collect(Collectors.toList());

        return validateByFilter(search, userLENames, LEGAL_ENTITY_FILTER);
    }

    /**
     * This method ensures that the users have access to filter by the criteria
     * given, if not a access denied exception will be raised.
     * 
     * @param filterKey
     * @param userLegalEntities
     * @return String with the valid filter to consult
     */
    private static String generateValidLEFilter(String filterKey, List<String> userLegalEntities) {
        List<String> listFilterValue = getLEListFilterValue(filterKey);
        if (listFilterValue == null || listFilterValue.isEmpty()) {
            listFilterValue = userLegalEntities;
        } else {
            for (String currentLE : listFilterValue) {
                if (!userLegalEntities.contains(currentLE)) {
                    LOGGER.error("User doesn't have access to filter by this legal entity: ,", currentLE);
                    throw new AccessDeniedException(
                            "User doesn't have access to filter by this legal entity: " + currentLE);
                }
            }
        }
        return "[" + String.join(",", listFilterValue) + "]";
    }

    /**
     * Checks if the search criteria has an array with the LE and returns it. If
     * it can not parse it a bad request exception will rise
     * 
     * @param search
     * @param filter
     * @return String with the Legal Entities values
     */
    private static String getLEFilterValue(String search, String filter) {
        String result = StringUtils.EMPTY;
        Boolean validSearch = false;
        Pattern pattern = Pattern.compile(SEARCH_REGEX);
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            if (filter.contains(matcher.group(1).toString())) {
                result = matcher.group(3);
                validSearch = true;
            }
        }

        if (!validSearch) {
            LOGGER.error("Unable to parse value of legalEntity, correct format example [XXXXX,YYYYYY,ZZZZZ]");
            throw new CustomBadRequestException(
                    "Unable to parse value of legalEntity, correct format example [XXXXX,YYYYYY,ZZZZZ]");
        }

        return result;
    }

    /**
     * Checks if the search criteria has an string with the transactionId and
     * returns it. If it can not parse it a bad request exception will rise
     * 
     * @param search
     * @param filter
     * @return String with the transactionId value
     */
    public static String getTransactionIdValue(String search, String filter) {
        String result = StringUtils.EMPTY;
        Boolean validSearch = false;
        Pattern pattern = Pattern.compile(SEARCH_REGEX);
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            if (filter.contains(matcher.group(1).toString())) {
                result = matcher.group(3);
                validSearch = true;
            }
        }

        if (!validSearch) {
            LOGGER.error("Unable to parse value of transactionId");
            throw new CustomBadRequestException("Unable to parse value of transactionI");
        }
        return result;
    }

    /**
     * Generate the filter by processorTransactionId or processorTransactionId
     * 
     * @param search
     * @param value
     * @return Boolean Expression with the filter
     */
    public static BooleanExpression getTransactionIdFilter(String search, String value) {
        PathBuilder<SaleTransaction> entityPath = new PathBuilder<SaleTransaction>(SaleTransaction.class,
                "saleTransaction");
        StringPath pathApplicationTransactionId = entityPath.getString("applicationTransactionId");
        StringPath pathProcessorTransactionId = entityPath.getString("processorTransactionId");
        return pathApplicationTransactionId.containsIgnoreCase(value)
                .or(pathProcessorTransactionId.containsIgnoreCase(value));
    }

    /**
     * Creates a list that with the LE that are given in the search criteria. It
     * takes the search criteria and pulls out the parameter with the LE and
     * split it into a list to be analyzed with the own legal entities of the
     * consultant user
     * 
     * @param value
     * @return return a list of strings
     */
    private static List<String> getLEListFilterValue(String value) {
        List<String> result = null;
        if (!StringUtils.isBlank(value) && !value.equals("[]")) {
            Matcher matcher = Pattern.compile(ANY_LIST_REGEX).matcher(value);
            String criteriaValue = null;
            while (matcher.find()) {
                criteriaValue = matcher.group(1);
            }
            if (criteriaValue != null) {
                result = Arrays.asList(criteriaValue.split(",")).stream().map(String::trim)
                        .collect(Collectors.toList());
                return result;
            } else {
                LOGGER.error("Unable to parse value of legalEntity, correct format example [XXXXX,YYYYYY,ZZZZZ]");
                throw new CustomBadRequestException(
                        "Unable to parse value of legalEntity, correct format example [XXXXX,YYYYYY,ZZZZZ]");
            }
        }
        return result;

    }
}
