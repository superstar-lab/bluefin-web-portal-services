package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.TransactionPredicatesBuilder;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class TransactionsService {

    @Autowired
    private TransactionRepository transactionRepository;
    private static final String SEARCH_REGEX = "(\\w+?)(:|<|>)(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}|[\\w\\s|\\d+(?:\\.\\d+)?]+),";
    private static final String SORT_REGEX = "(\\w+?)(:)(\\w+?),";

    public Iterable<TransactionView> getTransactions(String search, Integer page, Integer size, String sort) {
        TransactionPredicatesBuilder builder = new TransactionPredicatesBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_REGEX);
            Matcher matcher = pattern.matcher(search + ",");
            while (matcher.find()) {
                builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }
        BooleanExpression exp = builder.build();
        Page<TransactionView> result = transactionRepository.findAll(exp, getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }
        return result;
    }

    private List<Order> getOrderList(String sort) {
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

    private PageRequest getPageRequest(int page, int size, String sort) {
        List<Order> orderList = getOrderList(sort);
        if (orderList.isEmpty()) {
            return new PageRequest(page, size);
        } else {
            Sort finalSort = new Sort(orderList);
            return new PageRequest(page, size, finalSort);
        }
    }

}
