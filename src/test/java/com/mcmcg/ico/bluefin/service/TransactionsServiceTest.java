package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.Predicate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class TransactionsServiceTest {

    @InjectMocks
    @Autowired
    private TransactionsService transactionsService;

    @Mock
    private TransactionRepository transactionRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /****** Starts GetTransactionInformation ******/
    @Test
    public void testGetTransactionInformationSuccess() {

        TransactionView result = null;

        Mockito.when(transactionRepository.findByTransactionId(Mockito.anyString())).thenReturn(new TransactionView());

        result = transactionsService.getTransactionInformation(Mockito.anyString());

        Assert.notNull(result);

        Mockito.verify(transactionRepository, Mockito.times(1)).findByTransactionId(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    @Test(expected = CustomNotFoundException.class)
    public void testGetTransactionInformationError() {

        Mockito.when(transactionRepository.findByTransactionId(Mockito.anyString())).thenReturn(null);

        transactionsService.getTransactionInformation(Mockito.anyString());

        Mockito.verify(transactionRepository, Mockito.times(1)).findByTransactionId(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    @Test(expected = CustomNotFoundException.class)
    public void testGetTransactionInformationNuLLParam() {

        Mockito.when(transactionRepository.findByTransactionId(null)).thenReturn(null);

        transactionsService.getTransactionInformation(null);

        Mockito.verify(transactionRepository, Mockito.times(1)).findByTransactionId(null);

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGetTransactionInformationDBAccessFail() {

        Mockito.when(transactionRepository.findByTransactionId(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(null));

        transactionsService.getTransactionInformation(Mockito.anyString());

        Mockito.verify(transactionRepository, Mockito.times(1)).findByTransactionId(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGetTransactionInformationDBConnectionFail() {

        Mockito.when(transactionRepository.findByTransactionId(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        transactionsService.getTransactionInformation(Mockito.anyString());

        Mockito.verify(transactionRepository, Mockito.times(1)).findByTransactionId(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    /****** Ends GetTransactionInformation ******/
    /****** Starts GetTransactions ******/

    @Test
    public void testGetTransactionsSuccess() {
        List<TransactionView> resultList = new ArrayList<TransactionView>();
        resultList.add(new TransactionView());

        Page<TransactionView> result = new PageImpl<TransactionView>(resultList);

        Mockito.when(transactionRepository.findAll(Mockito.any(Predicate.class), Mockito.any(Pageable.class)))
                .thenReturn(result);

        Iterable<TransactionView> transactions = transactionsService
                .getTransactions(QueryDSLUtil.createExpression("search", TransactionView.class), 1, 1, null);

        Assert.notNull(transactions);

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    @Test(expected = CustomNotFoundException.class)
    public void testGetTransactionsNotFound() {

        List<TransactionView> resultList = new ArrayList<TransactionView>();
        Page<TransactionView> result = new PageImpl<TransactionView>(resultList);

        Mockito.when(transactionRepository.findAll(Mockito.any(Predicate.class), Mockito.any(Pageable.class)))
                .thenReturn(result);

        transactionsService.getTransactions(QueryDSLUtil.createExpression("search", TransactionView.class), 2, 1, null);

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetTransactionsDBFail() {

        Mockito.when(transactionRepository.findAll(Mockito.any(Predicate.class), Mockito.any(Pageable.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        transactionsService.getTransactions(QueryDSLUtil.createExpression("search", TransactionView.class), 2, 1, null);

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

}
