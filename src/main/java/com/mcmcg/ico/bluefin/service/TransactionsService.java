package com.mcmcg.ico.bluefin.service;

import java.text.ParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.RefundTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
public class TransactionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsService.class);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private VoidTransactionRepository voidTransactionRepository;
    @Autowired
    private RefundTransactionRepository refundTransactionRepository;
    @Autowired
    private UserRepository userRepository;

    public Transaction getTransactionInformation(final String transactionId, TransactionType transactionType) {
        Transaction result = null;

        switch (transactionType) {
        case VOID:
            result = voidTransactionRepository.findByApplicationTransactionId(transactionId);
            break;
        case REFUND:
            result = refundTransactionRepository.findByApplicationTransactionId(transactionId);
            break;
        default:
            result = transactionRepository.findByApplicationTransactionId(transactionId);
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }

    public Iterable<SaleTransaction> getTransactions(String search, PageRequest paging) {
        Page<SaleTransaction> result;
        try {
            result = transactionRepository.findTransaction(search, paging);
        } catch (ParseException e) {
            throw new CustomNotFoundException("Unable to process find transaction, due an error with date formatting");
        }
        final int page = paging.getPageNumber();

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
