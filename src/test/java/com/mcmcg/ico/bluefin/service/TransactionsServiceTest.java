package com.mcmcg.ico.bluefin.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

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

    /******     Ends GetTransactionInformation      ******/
}
