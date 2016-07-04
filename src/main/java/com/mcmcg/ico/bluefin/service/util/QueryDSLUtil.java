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
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mysema.query.types.expr.BooleanExpression;

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

    private static final String LEGAL_ENTITIES_FILTER = "legalEntity:";

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

    public static String getValidSearchBasedOnLegalEntities(List<LegalEntityApp> userLE, String search) {
        List<String> userLENames = userLE.stream().map(current -> current.getLegalEntityAppName())
                .collect(Collectors.toList());

        if (!search.contains(LEGAL_ENTITIES_FILTER)) {
            if (!search.isEmpty()) {
                search = search + ",";
            }
            search = search + LEGAL_ENTITIES_FILTER + userLENames;
        } else {
            String LEFilterValue = getLEFilterValue(search, userLENames);
            search = search.replace(LEGAL_ENTITIES_FILTER + LEFilterValue,
                    LEGAL_ENTITIES_FILTER + generateValidLEFilter(LEFilterValue, userLENames));
        }
        return search;
    }

    private static String generateValidLEFilter(String filterValue, List<String> userLENames) {
        List<String> listFilterValue = getLEListFilterValue(filterValue);
        if (listFilterValue == null || listFilterValue.isEmpty()) {
            listFilterValue = userLENames;
        } else {
            for (String currentLE : listFilterValue) {
                if (!userLENames.contains(currentLE)) {
                    LOGGER.error("User doesn't have access to filter by this legal entity: ,", currentLE);
                    throw new AccessDeniedException(
                            "User doesn't have access to filter by this legal entity: " + currentLE);
                }
            }
        }
        return "[" + String.join(",", listFilterValue) + "]";
    }

    private static String getLEFilterValue(String search, List<String> userLegalEntitiesNames) {
        String result = "";
        Boolean validSearch = false;
        Pattern pattern = Pattern.compile(SEARCH_REGEX);
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            if (LEGAL_ENTITIES_FILTER.contains(matcher.group(1).toString())) {
                result = matcher.group(3);
                validSearch = true;
            }
        }

        if (!validSearch) {
            LOGGER.error("Unable to parse value of legalEntity, correct format example [XXXXX,YYYYYY,ZZZZZ]");
            throw new CustomBadRequestException(
                    "Unable to parse value of legalEntity, correct format example [XXXXX,YYYYYY,ZZZZZ]");
        } else {
            return result;
        }
    }

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
