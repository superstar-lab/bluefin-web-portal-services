package com.mcmcg.ico.bluefin.service;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
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

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
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
    @Mock
    private UserRepository userRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /****** Starts GetTransactionInformation ******/
    @Test
    public void testGetTransactionInformationSuccess() {

        SaleTransaction result = null;

        Mockito.when(transactionRepository.findByTransactionId(Mockito.anyString())).thenReturn(new SaleTransaction());

        result = transactionsService.getTransactionInformation(Mockito.anyString());

        Assert.assertNotNull(result);

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
        List<SaleTransaction> resultList = new ArrayList<SaleTransaction>();
        resultList.add(new SaleTransaction());

        Page<SaleTransaction> result = new PageImpl<SaleTransaction>(resultList);

        Mockito.when(transactionRepository.findAll(Mockito.any(Predicate.class), Mockito.any(Pageable.class)))
                .thenReturn(result);

        Iterable<SaleTransaction> transactions = transactionsService
                .getTransactions(QueryDSLUtil.createExpression("search", SaleTransaction.class), 1, 1, null);

        Assert.assertNotNull(transactions);

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    @Test(expected = CustomNotFoundException.class)
    public void testGetTransactionsNotFound() {

        List<SaleTransaction> resultList = new ArrayList<SaleTransaction>();
        Page<SaleTransaction> result = new PageImpl<SaleTransaction>(resultList);

        Mockito.when(transactionRepository.findAll(Mockito.any(Predicate.class), Mockito.any(Pageable.class)))
                .thenReturn(result);

        transactionsService.getTransactions(QueryDSLUtil.createExpression("search", SaleTransaction.class), 2, 1, null);

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetTransactionsDBFail() {

        Mockito.when(transactionRepository.findAll(Mockito.any(Predicate.class), Mockito.any(Pageable.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        transactionsService.getTransactions(QueryDSLUtil.createExpression("search", SaleTransaction.class), 2, 1, null);

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }

    /****** Ends GetTransactions ******/
    /****** Starts GetLegalEntitiesFromUser ******/

    @Test
    public void testGetLegalEntitiesFromUserSuccess() {
        User expected = createValidUser();

        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(expected);

        List<LegalEntityApp> result = transactionsService.getLegalEntitiesFromUser("nquiros");

        Assert.assertEquals("legalEntity1", result.get(0).getLegalEntityAppName());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userRepository);

    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetLegalEntitiesFromUserDBFail() {

        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        transactionsService.getLegalEntitiesFromUser("nquiros");

        Mockito.verify(transactionRepository, Mockito.times(1)).findAll(Mockito.any(Predicate.class),
                Mockito.any(Pageable.class));

        Mockito.verifyNoMoreInteractions(transactionRepository);

    }
    

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setFirstName("test");
        user.setLastName("user");
        user.setUsername("userTest");

        List<UserLegalEntity> userLegalEntities = new ArrayList<UserLegalEntity>();
        userLegalEntities.add(createValidUserLegalEntity());
        user.setLegalEntities(userLegalEntities);
        return user;
    }

    private UserLegalEntity createValidUserLegalEntity() {
        UserLegalEntity userLegalEntity = new UserLegalEntity();
        userLegalEntity.setUserLegalEntityAppId(0);
        userLegalEntity.setLegalEntityApp(createValidLegalEntityApp());
        return userLegalEntity;
    }

    private LegalEntityApp createValidLegalEntityApp() {
        LegalEntityApp validLegalEntity = new LegalEntityApp();
        UserLegalEntity validUserLegalEntity = new UserLegalEntity();
        List<UserLegalEntity> validUserLegalEntityList = new ArrayList<UserLegalEntity>();
        validUserLegalEntityList.add(validUserLegalEntity);
        validLegalEntity.setUserLegalEntities(validUserLegalEntityList);
        validLegalEntity.setLegalEntityAppName("legalEntity1");
        validLegalEntity.setLegalEntityAppId(4321);
        return validLegalEntity;
    }

}
