package com.mcmcg.ico.bluefin.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.math.BigDecimal;
import java.util.Date;

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

    @Test
    public void getTransactionByIdOk() throws Exception { // 200
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
    public void getLegalEntitiesNotFound() throws Exception { // 404

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomNotFoundException(""));

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isNotFound());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getLegalEntitiesBadRequest() throws Exception { // 400
        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomBadRequestException(""));
        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getLegalEntitiesBadRequestNoParam() throws Exception { // 400

        mockMvc.perform(get("/api/rest/bluefin/transactions")).andExpect(status().isBadRequest());

    }

    @Test
    public void getLegalEntitiesUnauthorized() throws Exception { // 401

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomUnauthorizedException(""));

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getLegalEntitiesInternalServerError() throws Exception { // 500

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString()))
                .thenThrow(new CustomException(""));

        mockMvc.perform(get("/api/rest/bluefin/transactions/{id}", 1234)).andExpect(status().isInternalServerError());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);
    }
}
