package com.mcmcg.ico.bluefin.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.service.TransactionsService;

public class TransactionsRestControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private TransactionsRestController transactionsRestControllerMock;

    @Mock
    private TransactionsService transactionService;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(transactionsRestControllerMock).addFilters().build();
    }

    private TransactionView getTransactionView() {

        Date date = new Date(1465322756555L);
        TransactionView result = new TransactionView();
        result.setAccountNumber("67326509");
        result.setAmount(new BigDecimal(4592.36));
        result.setCardNumberLast4Char("5162");
        result.setCreatedDate(date);
        result.setCustomer("Natalia Quiros");
        result.setLegalEntity("MCM-R2K");
        result.setProcessorName("JETPAY");
        result.setTransactionId("532673163");
        result.setTransactionStatusCode(1);
        result.setTransactionType("SALE");

        return result;
    }

    @Test
    public void getTransactionByIdOk() throws Exception { // 200

        TransactionView result = getTransactionView();


        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString())).thenReturn(result);

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.accountNumber").value("67326509"))
                .andExpect(jsonPath("$.amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$.cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$.createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$.customer").value("Natalia Quiros"))
                .andExpect(jsonPath("$.legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$.processorName").value("JETPAY"))
                .andExpect(jsonPath("$.transactionId").value("532673163"))
                .andExpect(jsonPath("$.transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$.transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdNotFound() throws Exception { // 404
        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomNotFoundException(""));

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isNotFound());
        
        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdBadRequest() throws Exception { // 400
        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomBadRequestException(""));
        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdBadRequestNoParam() throws Exception { // 400

        mockMvc.perform(get("/api/rest/bluefin/transactions")).andExpect(status().isBadRequest());

    }

    @Test
    public void getTransactionByIdUnauthorized() throws Exception { // 401

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomUnauthorizedException(""));

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdInternalServerError() throws Exception { // 500

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomException(""));

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isInternalServerError());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    /****** Starts GetTransactions ******/
    @Test
    public void getTransactionsOK() throws Exception { // 200
        List<TransactionView> transactions = new ArrayList<TransactionView>();
        transactions.add(getTransactionView());

        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/rest/bluefin/transactions").param("search", "accountNumber:67326509,amount > 4592")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].customer").value("Natalia Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].transactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionsOKAllParams() throws Exception { // 200
        List<TransactionView> transactions = new ArrayList<TransactionView>();
        transactions.add(getTransactionView());

        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/rest/bluefin/transactions")
                .param("search",
                        "accountNumber:67326509,amount > 4592,customer:Natalia Quiros,legalEntity:MCM-R2K,processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].customer").value("Natalia Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].transactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);
    }
    @Test
    public void getTransactionsNoSearchParam() throws Exception { // 200
        List<TransactionView> transactions = new ArrayList<TransactionView>();
        transactions.add(getTransactionView());

        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/rest/bluefin/transactions").param("search", "")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].customer").value("Natalia Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].transactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);
    }
    
    @Test
    public void getTransactionsNoParams() throws Exception { // 400 

        mockMvc.perform(get("/api/rest/bluefin/transactions"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }
    
    @Test
    public void getTransactionsNoParamsButPage() throws Exception { // 400 
        mockMvc.perform(get("/api/rest/bluefin/transactions")
                .param("page", "1"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }
    @Test
    public void getTransactionsNoParamsButSize() throws Exception { // 400 

        mockMvc.perform(get("/api/rest/bluefin/transactions")
                .param("size", "2"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }
    @Test
    public void getTransactionsNoParamsButNull() throws Exception { // 400 

        mockMvc.perform(get("/api/rest/bluefin/transactions")
                .param("page", "null")
                .param("size", "null"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }
    @Test
    public void getTransactiosUnauthorized() throws Exception { // 401 
        
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenThrow(new CustomUnauthorizedException(""));
        
        mockMvc.perform(get("/api/rest/bluefin/transactions")
                .param("search", "")
                .param("page", "1")
                .param("size", "2"))
        .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void getTransactionsInternalServerError() throws Exception { // 500
        
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenThrow(new CustomException(""));
        
        mockMvc.perform(get("/api/rest/bluefin/transactions")
                .param("search", "")
                .param("page", "1")
                .param("size", "2"))
        .andExpect(status().isInternalServerError());
 
        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        
        Mockito.verifyNoMoreInteractions(transactionService);
    }
}
