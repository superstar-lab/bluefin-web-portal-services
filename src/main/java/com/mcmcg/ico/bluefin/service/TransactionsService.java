package com.mcmcg.ico.bluefin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

@Service
public class TransactionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsService.class);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;

    public SaleTransaction getTransactionInformation(String transactionId) {
        SaleTransaction result = transactionRepository.findByApplicationTransactionId(transactionId);

        if (result == null) {
            LOGGER.error("Transaction not found: {}", transactionId);
            throw new CustomNotFoundException("Transaction not found: " + transactionId);
        }

        return result;
    }

    public Iterable<SaleTransaction> getTransactions(String search, PageRequest paging) {
        
        Page<SaleTransaction> result =  transactionRepository.findTransaction(search, paging);
        int page = paging.getPageNumber();
         
        if (page > result.getTotalPages() && page != 0) {
            LOGGER.error("Unable to find the page requested");
            throw new CustomNotFoundException("Unable to find the page requested");
        }
        return result;
    }

    public List<LegalEntityApp> getLegalEntitiesFromUser(String username) {
        User user = userRepository.findByUsername(username);
        List<LegalEntityApp> userLE = user.getLegalEntityApps();
        return userLE;
    }
}
