package com.mcmcg.ico.bluefin.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.mysema.query.types.expr.BooleanExpression;

public class QueryDSLUtil {

    private static final String SEARCH_REGEX = "(\\w+?)(:|<|>)(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}|[\\w\\s|\\d+(?:\\.\\d+)?]+),";
    private static final String SORT_REGEX = "(\\w+?)(:)(\\w+?),";
    
    public static BooleanExpression createExpression(String search){
        TransactionPredicatesBuilder builder = new TransactionPredicatesBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");
            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }
        return builder.build();
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
}