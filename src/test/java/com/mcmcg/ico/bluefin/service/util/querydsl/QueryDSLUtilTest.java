/**package com.mcmcg.ico.bluefin.service.util.querydsl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;

public class QueryDSLUtilTest {
	private PathBuilder<SaleTransaction> entityPath = new PathBuilder<SaleTransaction>(SaleTransaction.class,
			"saleTransaction");

	private SaleTransaction getSaleTransaction() {

		DateTime date = new DateTime(1465322756555L);
		SaleTransaction result = new SaleTransaction();
		//TODO Dheeraj can look into this
		result.setAccountNumber("67326509");
		result.setAmount(new BigDecimal(4592.36));
		result.setLegalEntity("MCMR2K");
		result.setProcessorName("JETPAY");
		result.setCardNumberLast4Char("5162");
		// result.setCreatedDate(date);
		result.setFirstName("Natalia");
		result.setLastName("Quiros");
		
		result.setApplicationTransactionId("532673163");
		result.setTransactionType("SALE");
		result.setCardType("DEBIT");

		return result;
	}

	*//**
	 * Generates a search with all permitted attributes without Id and
	 * CardNumberLast4Char
	 *//**
	@Test
	public void createExpressionSuccessAll() {
		String query = "accountNumber:67326509,amount>4592.3599999999996725819073617458343505859375,amount<5000,createdDate>2016-06-07 12:05:56";
		query += ",createdDate<2016-06-09 21:15:45,processorName:JETPAY,legalEntity:MCMR2K,transactionStatusCode:APPROVED,transactionType:SALE,firstName:Natalia,lastName:Quiros,cardType:DEBIT";
		final String accountNumber = "accountNumber";
		final String amount = "amount";
		final BigDecimal amountValue = new BigDecimal(5000);
		final String createdDate = "createdDate";
		final DateTime createdDateValue = new DateTime(1465528545203L);
		final String processorName = "processorName";
		final String firstName = "firstName";
		final String lastName = "lastName";
		final String legalEntity = "legalEntity";
		final String transactionStatusCode = "transactionStatusCode";
		final Integer transactionStatusCodeValue = new Integer(1);
		final String transactionType = "transactionType";
		final String cardType = "cardType";

		SaleTransaction tv = getSaleTransaction();
		// Creates the boolean expression to be compared with the one returned
		// by the method we want to test
		//BooleanExpression expected = 
				//TODO - Dheeraj Can look into this.
				entityPath.getString(accountNumber).containsIgnoreCase(tv.getAccountNumber())// accountNumber:1234
				.and(entityPath.getNumber(amount, BigDecimal.class).goe(tv.getAmount()))// amount>1234
				.and(entityPath.getNumber(amount, BigDecimal.class).loe(amountValue))// amount<1234
				// .and(entityPath.getDate(createdDate,
				// DateTime.class).goe(tv.getCreatedDate()))// createdDate>date
				.and(entityPath.getDate(createdDate, DateTime.class).loe(createdDateValue))// createdDate<date
				.and(entityPath.getString(processorName).containsIgnoreCase(tv.getProcessorName()))// processorName:test
				.and(entityPath.getString(legalEntity).containsIgnoreCase(tv.getLegalEntity())) // legalEntity:test
				// tricky one transactionStatusCode receives a String, process a
				// integer and returns String
				.and(entityPath.getNumber(transactionStatusCode, Integer.class).eq(transactionStatusCodeValue))// transactionStatusCode:1
				.and(entityPath.getString(transactionType).containsIgnoreCase(tv.getTransactionType()))// transactionType:test
				.and(entityPath.getString(firstName).containsIgnoreCase(tv.getFirstName()))// firstName:test
				.and(entityPath.getString(lastName).containsIgnoreCase(tv.getLastName()))// lastName:test
				.and(entityPath.getString(cardType).containsIgnoreCase(tv.getCardType()))// cardType:test
		;

		BooleanExpression be = QueryDSLUtil.createExpression(query, SaleTransaction.class);

		assertEquals("", be.toString());

	}

	@Test(expected = CustomBadRequestException.class)
	public void createExpressionErrorTransactionStatusCodeAsInt() {
		QueryDSLUtil.createExpression("transactionStatusCode:1", SaleTransaction.class);
	}

	@Test
	public void createExpressionErrorWrongOperation() {
		BooleanExpression expected = QueryDSLUtil.createExpression("accountNumber?67326509", SaleTransaction.class);
		assertNull(expected);
	}

	@Test(expected = CustomBadRequestException.class)
	public void createExpressionInvalidDate() {
		QueryDSLUtil.createExpression("createdDate>2016-06-07 12:05:5", SaleTransaction.class);
	}

	@Test(expected = CustomBadRequestException.class)
	public void createExpressionInvalidAmountType() {
		QueryDSLUtil.createExpression("amount:500xf", SaleTransaction.class);
	}

	@Test(expected = CustomBadRequestException.class)
	public void createExpressionNotExistingField() {
		QueryDSLUtil.createExpression("accountNumber2:67326509", SaleTransaction.class);
	}

	@Test
	public void createExpressionErrorEmpty() {
		BooleanExpression be = QueryDSLUtil.createExpression("", SaleTransaction.class);
		Assert.assertNull(be);
	}

	@Test
	public void createExpressionErrorNull() {
		BooleanExpression be = QueryDSLUtil.createExpression(null, SaleTransaction.class);
		Assert.assertNull(be);
	}

	@Test
	public void getPageRequestSuccess() {
		int page = 1, size = 1;
		String sort = "transactionId : asc";

		PageRequest pr = QueryDSLUtil.getPageRequest(page, size, sort);

		Assert.assertNotNull(pr);

		assertEquals(page, pr.getPageNumber());
		assertEquals(size, pr.getPageSize());
	}

	@Test(expected = CustomBadRequestException.class)
	public void getValidSearchBasedOnLegalEntitiesEmptyLegalEntity() {
		QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(), "legalEntity:");
	}

	@Test
	public void getValidSearchBasedOnLegalEntitiesEmptyLegalEntity2() {
		String result = QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(),
				"legalEntity:[]");
		Assert.assertEquals(result, "legalEntity:[MCM-R2K,MCM-AWA]");
	}

	@Test
	public void getValidSearchBasedOnLegalEntitiesEmptyLegalEntity3() {
		String result = QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(), "");
		Assert.assertEquals(result, "legalEntity:[MCM-R2K, MCM-AWA]");
	}

	@Test
	public void getValidSearchBasedOnLegalEntitiesEmptyLegalEntity4() {
		String result = QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(), "amount>2");
		Assert.assertEquals(result, "amount>2,legalEntity:[MCM-R2K, MCM-AWA]");
	}

	@Test(expected = AccessDeniedException.class)
	public void getValidSearchBasedOnLegalEntitiesUnauthorizedLegalEntity() {
		QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(), "legalEntity:[MCM-TTT]");
	}

	@Test(expected = CustomBadRequestException.class)
	public void getValidSearchBasedOnLegalEntitiesInvalidLegalEntity() {
		QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(), "legalEntity:[MC");
	}

	@Test
	public void getValidSearchBasedOnLegalEntitiesValidLegalEntity() {
		String result = QueryDSLUtil.getValidSearchBasedOnLegalEntities(createValidLegalEntityAppList(),
				"legalEntity:[MCM-R2K]");
		Assert.assertEquals(result, "legalEntity:[MCM-R2K]");
	}

	private List<LegalEntityApp> createValidLegalEntityAppList() {
		List<LegalEntityApp> result = new ArrayList<LegalEntityApp>();
		result.add(createValidLegalEntityApp("MCM-R2K"));
		result.add(createValidLegalEntityApp("MCM-AWA"));
		return result;
	}

	private LegalEntityApp createValidLegalEntityApp(String name) {
		LegalEntityApp validLegalEntity = new LegalEntityApp();
		UserLegalEntityApp validUserLegalEntity = new UserLegalEntityApp();
		Set<UserLegalEntityApp> validUserLegalEntityList = new HashSet<UserLegalEntityApp>();
		validUserLegalEntityList.add(validUserLegalEntity);
		// validLegalEntity.setUserLegalEntities(validUserLegalEntityList);
		validLegalEntity.setLegalEntityAppName(name);
		validLegalEntity.setLegalEntityAppId(4321L);
		return validLegalEntity;
	}
}
*/