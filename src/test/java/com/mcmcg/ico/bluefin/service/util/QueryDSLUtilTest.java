package com.mcmcg.ico.bluefin.service.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;

import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mysema.query.types.expr.BooleanExpression;

public class QueryDSLUtilTest {
    @Test
    public void createExpressionSuccess() {

        BooleanExpression be = QueryDSLUtil.createExpression("accountNumber:67326509");
        
        Assert.notNull(be); 
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

    @Test
    public void getPageRequestSortNull() {
        int page = 1, size = 1;
        String sort = null;

        PageRequest pr = QueryDSLUtil.getPageRequest(page, size, sort); 

        Assert.notNull(pr); 

        assertEquals(page, pr.getPageNumber());
        assertEquals(size, pr.getPageSize()); 
    }
}
