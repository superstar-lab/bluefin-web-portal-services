package com.mcmcg.ico.bluefin.service.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;

import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.PathBuilder;

public class QueryDSLUtilTest {
    private PathBuilder<TransactionView> entityPath = new PathBuilder<TransactionView>(TransactionView.class,
            "transactionView");

    private TransactionView getTransactionView() {

        Date date = new Date(1465322756555L);
        TransactionView result = new TransactionView();
        result.setAccountNumber("67326509");
        result.setAmount(new BigDecimal(4592.36));
        result.setCardNumberLast4Char("5162");
        result.setCreatedDate(date);
        result.setCustomer("Natalia Quiros");
        result.setLegalEntity("MCMR2K");
        result.setProcessorName("JETPAY");
        result.setTransactionId("532673163");
        result.setTransactionType("SALE");
        result.setCardType("DEBIT");

        return result;
    }

    /**
     * Generates a search with all permitted attributes without Id and
     * CardNumberLast4Char
     */
    @Test
    public void createExpressionSuccessAll() {
        String query = "accountNumber:67326509,amount>4592.3599999999996725819073617458343505859375,amount<5000,createdDate>2016-06-07 12:05:56";
        query+=",createdDate<2016-06-09 21:15:45,processorName:JETPAY,legalEntity:MCMR2K,transactionStatusCode:APPROVED,transactionType:SALE,customer:Natalia Quiros,cardType:DEBIT";
        final String accountNumber = "accountNumber";
        final String amount = "amount";
        final BigDecimal amountValue = new BigDecimal(5000);
        final String createdDate = "createdDate";
        final Date createdDateValue = new Date(1465528545203L);
        final String processorName = "processorName";
        final String customer = "customer";
        final String legalEntity = "legalEntity";
        final String transactionStatusCode = "transactionStatusCode";
        final Integer transactionStatusCodeValue = new Integer(1);
        final String transactionType = "transactionType";
        final String cardType = "cardType";
        

        TransactionView tv = getTransactionView();
        // Creates the boolean expression to be compared with the one returned
        // by the method we want to test
        BooleanExpression expected = entityPath.getString(accountNumber).containsIgnoreCase(tv.getAccountNumber())// accountNumber:1234
                .and(entityPath.getNumber(amount, BigDecimal.class).goe(tv.getAmount()))// amount>1234
                .and(entityPath.getNumber(amount, BigDecimal.class).loe(amountValue))// amount<1234
                .and(entityPath.getDate(createdDate, Date.class).goe(tv.getCreatedDate()))// createdDate>date
                .and(entityPath.getDate(createdDate, Date.class).loe(createdDateValue))// createdDate<date
                .and(entityPath.getString(processorName).containsIgnoreCase(tv.getProcessorName()))// processorName:test
                .and(entityPath.getString(legalEntity).containsIgnoreCase(tv.getLegalEntity())) // legalEntity:test
                // tricky one transactionStatusCode receives a String, process a
                // integer and returns String
                .and(entityPath.getNumber(transactionStatusCode, Integer.class).eq(transactionStatusCodeValue))// transactionStatusCode:1
                .and(entityPath.getString(transactionType).containsIgnoreCase(tv.getTransactionType()))// transactionType:test
                .and(entityPath.getString(customer).containsIgnoreCase(tv.getCustomer()))// customer:test
                .and(entityPath.getString(cardType).containsIgnoreCase(tv.getCardType()))// cardType:test
        ;

        BooleanExpression be = QueryDSLUtil.createExpression(query);

        assertEquals(expected.toString(), be.toString());

    }

    @Test(expected = CustomBadRequestException.class)
    public void createExpressionErrorTransactionStatusCodeAsInt() {
        QueryDSLUtil.createExpression("transactionStatusCode:1");
    }

    @Test
    public void createExpressionErrorWrongOperation() {
        BooleanExpression expected = QueryDSLUtil.createExpression("accountNumber?67326509");
        assertNull(expected);
    }

    @Test(expected = CustomBadRequestException.class)
    public void createExpressionInvalidDate() {
        QueryDSLUtil.createExpression("createdDate>2016-06-07 12:05:5");
    }

    @Test(expected = CustomBadRequestException.class)
    public void createExpressionInvalidAmountType() {
        QueryDSLUtil.createExpression("amount:500xf");
    }

    @Test(expected = CustomBadRequestException.class)
    public void createExpressionNotExistingField() {
        QueryDSLUtil.createExpression("accountNumber2:67326509");
    }

    @Test
    public void createExpressionErrorEmpty() {
        BooleanExpression be = QueryDSLUtil.createExpression("");
        Assert.isNull(be);
    }

    @Test
    public void createExpressionErrorNull() {
        BooleanExpression be = QueryDSLUtil.createExpression(null);
        Assert.isNull(be);
    }

    @Test
    public void getPageRequestSuccess() {
        int page = 1, size = 1;
        String sort = "transactionId : asc";

        PageRequest pr = QueryDSLUtil.getPageRequest(page, size, sort);

        Assert.notNull(pr);

        assertEquals(page, pr.getPageNumber());
        assertEquals(size, pr.getPageSize());
    }
}
