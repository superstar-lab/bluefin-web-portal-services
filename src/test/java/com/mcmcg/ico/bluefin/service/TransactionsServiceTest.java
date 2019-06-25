package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.repository.SaleTransactionDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mysema.query.types.Predicate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class TransactionsServiceTest {

	@InjectMocks
	@Autowired
	private TransactionService transactionsService;

	@Mock
	private SaleTransactionDAO saleTransactionDAO;
	@Mock
	private UserDAO userDAO;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/****** Starts GetTransactionInformation ******/
	@Test
	public void testGetTransactionInformationSuccess() {

		SaleTransaction result = null;

		// Mockito.when(saleTransactionDAO.findByApplicationTransactionId(Mockito.anyString()))
		// .thenReturn(new SaleTransaction());

		result = (SaleTransaction) transactionsService.getTransactionInformation(Mockito.anyString(),
				TransactionTypeCode.SALE);

		Assert.assertNotNull(result);

		Mockito.verify(saleTransactionDAO, Mockito.times(1)).findByApplicationTransactionId(Mockito.anyString());

		Mockito.verifyNoMoreInteractions(saleTransactionDAO);

	}

	@Test(expected = CustomNotFoundException.class)
	public void testGetTransactionInformationError() {

		Mockito.when(saleTransactionDAO.findByApplicationTransactionId(Mockito.anyString())).thenReturn(null);

		transactionsService.getTransactionInformation(Mockito.anyString(), TransactionTypeCode.SALE);

		Mockito.verify(saleTransactionDAO, Mockito.times(1)).findByApplicationTransactionId(Mockito.anyString());

		Mockito.verifyNoMoreInteractions(saleTransactionDAO);

	}

	@Test(expected = CustomNotFoundException.class)
	public void testGetTransactionInformationNuLLParam() {

		Mockito.when(saleTransactionDAO.findByApplicationTransactionId(null)).thenReturn(null);

		transactionsService.getTransactionInformation(null, TransactionTypeCode.SALE);

		Mockito.verify(saleTransactionDAO, Mockito.times(1)).findByApplicationTransactionId(null);

		Mockito.verifyNoMoreInteractions(saleTransactionDAO);

	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testGetTransactionInformationDBAccessFail() {

		Mockito.when(saleTransactionDAO.findByApplicationTransactionId(Mockito.anyString()))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(null));

		transactionsService.getTransactionInformation(Mockito.anyString(), TransactionTypeCode.SALE);

		Mockito.verify(saleTransactionDAO, Mockito.times(1)).findByApplicationTransactionId(Mockito.anyString());

		Mockito.verifyNoMoreInteractions(saleTransactionDAO);

	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testGetTransactionInformationDBConnectionFail() {

		Mockito.when(saleTransactionDAO.findByApplicationTransactionId(Mockito.anyString()))
				.thenThrow(new DataAccessResourceFailureException("", null));

		transactionsService.getTransactionInformation(Mockito.anyString(), TransactionTypeCode.SALE);

		Mockito.verify(saleTransactionDAO, Mockito.times(1)).findByApplicationTransactionId(Mockito.anyString());

		Mockito.verifyNoMoreInteractions(saleTransactionDAO);

	}

	/****** Ends GetTransactionInformation ******/
	/****** Starts GetTransactions ******/

	@Test
	public void testGetTransactionsSuccess() {
		/*
		 * List<SaleTransaction> resultList = new ArrayList<SaleTransaction>();
		 * resultList.add(new SaleTransaction());
		 * 
		 * Page<SaleTransaction> result = new
		 * PageImpl<SaleTransaction>(resultList);
		 * 
		 * Mockito.when(saleTransactionDAO.findAll(Mockito.any(Predicate.
		 * class), Mockito.any(Pageable.class))) .thenReturn(result);
		 * 
		 * Iterable<SaleTransaction> transactions = transactionsService
		 * .getTransactions(QueryDSLUtil.createExpression("search",
		 * SaleTransaction.class), 1, 1, null);
		 * 
		 * Assert.assertNotNull(transactions);
		 * 
		 * Mockito.verify(saleTransactionDAO,
		 * Mockito.times(1)).findAll(Mockito.any(Predicate.class),
		 * Mockito.any(Pageable.class));
		 * 
		 * Mockito.verifyNoMoreInteractions(saleTransactionDAO);
		 */

	}

	/*
	 * @Test(expected = CustomNotFoundException.class) public void
	 * testGetTransactionsNotFound() {
	 * 
	 * List<SaleTransaction> resultList = new ArrayList<SaleTransaction>();
	 * Page<SaleTransaction> result = new PageImpl<SaleTransaction>(resultList);
	 * 
	 * Mockito.when(saleTransactionDAO.findAll(Mockito.any(Predicate. class),
	 * Mockito.any(Pageable.class))) .thenReturn(result);
	 * 
	 * transactionsService.getTransactions(QueryDSLUtil.createExpression(
	 * "search", SaleTransaction.class), 2, 1, null);
	 * 
	 * Mockito.verify(saleTransactionDAO,
	 * Mockito.times(1)).findAll(Mockito.any(Predicate.class),
	 * Mockito.any(Pageable.class));
	 * 
	 * Mockito.verifyNoMoreInteractions(saleTransactionDAO); }
	 */

	/*
	 * @Test(expected =
	 * org.springframework.transaction.CannotCreateTransactionException.class)
	 * public void testGetTransactionsDBFail() {
	 * 
	 * Mockito.when(saleTransactionDAO.findAll(Mockito.any(Predicate. class),
	 * Mockito.any(Pageable.class))) .thenThrow(new
	 * org.springframework.transaction.CannotCreateTransactionException(""));
	 * 
	 * transactionsService.getTransactions(QueryDSLUtil.createExpression(
	 * "search", SaleTransaction.class), 2, 1, null);
	 * 
	 * Mockito.verify(saleTransactionDAO,
	 * Mockito.times(1)).findAll(Mockito.any(Predicate.class),
	 * Mockito.any(Pageable.class));
	 * 
	 * Mockito.verifyNoMoreInteractions(saleTransactionDAO);
	 * 
	 * }
	 */

	/****** Ends GetTransactions ******/
	/****** Starts GetLegalEntitiesFromUser ******/

	@Test
	public void testGetLegalEntitiesFromUserSuccess() {
		User expected = createValidUser();

		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(expected);

		List<LegalEntityApp> result = transactionsService.getLegalEntitiesFromUser("nquiros");

		Assert.assertEquals("legalEntity1", result.get(0).getLegalEntityAppName());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());

		Mockito.verifyNoMoreInteractions(userDAO);

	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testGetLegalEntitiesFromUserDBFail() {

		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		transactionsService.getLegalEntitiesFromUser("nquiros");

		// Mockito.verify(saleTransactionDAO,
		// Mockito.times(1)).findAll(Mockito.any(Predicate.class),
		// Mockito.any(Pageable.class));

		Mockito.verifyNoMoreInteractions(saleTransactionDAO);

	}

	private User createValidUser() {
		User user = new User();
		user.setEmail("test@email.com");
		user.setFirstName("test");
		user.setLastName("user");
		user.setUsername("userTest");

		List<UserLegalEntityApp> userLegalEntities = new ArrayList<UserLegalEntityApp>();
		userLegalEntities.add(createValidUserLegalEntity());
		// user.setLegalEntities(userLegalEntities);
		return user;
	}

	private UserLegalEntityApp createValidUserLegalEntity() {
		UserLegalEntityApp userLegalEntity = new UserLegalEntityApp();
		userLegalEntity.setUserLegalEntityAppId(0L);
		// userLegalEntity.setLegalEntityApp(createValidLegalEntityApp());
		return userLegalEntity;
	}

	private LegalEntityApp createValidLegalEntityApp() {
		LegalEntityApp validLegalEntity = new LegalEntityApp();
		UserLegalEntityApp validUserLegalEntity = new UserLegalEntityApp();
		Set<UserLegalEntityApp> validUserLegalEntityList = new HashSet<UserLegalEntityApp>();
		validUserLegalEntityList.add(validUserLegalEntity);
		// validLegalEntity.setUserLegalEntities(validUserLegalEntityList);
		validLegalEntity.setLegalEntityAppName("legalEntity1");
		validLegalEntity.setLegalEntityAppId(4321L);
		return validLegalEntity;
	}

}
