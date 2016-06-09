package com.mcmcg.ico.bluefin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class TransactionsService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    public TransactionView getTransactionInformation(String transactionId) {
        TransactionView result = transactionRepository.findByTransactionId(transactionId);

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found: " + transactionId);
        }

        return result;
    }

    public Iterable<TransactionView> getTransactions(BooleanExpression exp, Integer page, Integer size, String sort) {
   
        Page<TransactionView> result = transactionRepository.findAll(exp, QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }
        return result;
    }
}
