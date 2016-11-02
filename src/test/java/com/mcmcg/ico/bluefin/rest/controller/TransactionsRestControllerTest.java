package com.mcmcg.ico.bluefin.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.controller.exception.GeneralRestExceptionHandler;
import com.mcmcg.ico.bluefin.service.TransactionService;

public class TransactionsRestControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private TransactionsRestController transactionsRestControllerMock;

    @Mock
    private TransactionService transactionService;

    private Authentication auth;

    @Before
    public void initMocks() {

        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(transactionsRestControllerMock).setControllerAdvice(new GeneralRestExceptionHandler())
                .build();

        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        auth = new UsernamePasswordAuthenticationToken("omonge", "password", authorities);
    }

    private SaleTransaction getSaleTransaction() {

        DateTime date = new DateTime(1465322756555L);
        SaleTransaction result = new SaleTransaction();
        result.setAccountNumber("67326509");
        result.setAmount(new BigDecimal(4592.36));
        result.setCardNumberLast4Char("5162");
//        result.setCreatedDate(date);
        result.setFirstName("Natalia");
        result.setLastName("Quiros");
        result.setLegalEntity("MCM-R2K");
        result.setProcessorName("JETPAY");
        result.setApplicationTransactionId("532673163");
        result.setInternalStatusCode("1");
        result.setTransactionType("SALE");

        return result;
    }

    public List<LegalEntityApp> createValidLegalEntities() {
        List<LegalEntityApp> legalEntities = new ArrayList<LegalEntityApp>();
        LegalEntityApp legalEntity = new LegalEntityApp();
        legalEntity.setLegalEntityAppId(1234L);
        legalEntity.setLegalEntityAppName("MCM-R2K");
        legalEntities.add(legalEntity);
        return legalEntities;
    }

    @Test
    public void getTransactionByIdOk() throws Exception { // 200

        SaleTransaction result = getSaleTransaction();

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString(), TransactionType.SALE)).thenReturn(result);

        mockMvc.perform(get("/api/transactions/{id}", 1234)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value("67326509"))
                .andExpect(jsonPath("$.amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$.cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$.createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$.firstName").value("Natalia")).andExpect(jsonPath("$.lastName").value("Quiros"))
                .andExpect(jsonPath("$.legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$.processorName").value("JETPAY"))
                .andExpect(jsonPath("$.applicationTransactionId").value("532673163"))
                .andExpect(jsonPath("$.transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$.transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString(), TransactionType.SALE);
        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdNotFound() throws Exception { // 404
        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString(), TransactionType.SALE))
                .thenThrow(new CustomNotFoundException(""));

        mockMvc.perform(get("/api/transactions/{id}", 1234)).andExpect(status().isNotFound());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString(), TransactionType.SALE);

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdBadRequest() throws Exception { // 400
        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString(), TransactionType.SALE))
                .thenThrow(new CustomBadRequestException(""));
        mockMvc.perform(get("/api/transactions/{id}", 1234)).andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString(), TransactionType.SALE);

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionByIdBadRequestNoParam() throws Exception { // 400

        mockMvc.perform(get("/api/transactions")).andExpect(status().isBadRequest());

    }

    @Test
    public void getTransactionByIdInternalServerError() throws Exception { // 500

        Mockito.when(transactionService.getTransactionInformation(Mockito.anyString(), TransactionType.SALE))
                .thenThrow(new CustomException(""));

        mockMvc.perform(get("/api/transactions/{id}", 1234)).andExpect(status().isInternalServerError());

        Mockito.verify(transactionService, Mockito.times(1)).getTransactionInformation(Mockito.anyString(), TransactionType.SALE);

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    /****** Starts GetTransactions ******/
    @Test
    public void getTransactionsOK() throws Exception { // 200
        /*List<SaleTransaction> transactions = new ArrayList<SaleTransaction>();
        transactions.add(getSaleTransaction());

        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions").principal(auth).param("search", "accountNumber:67326509,amount > 4592")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].firstName").value("Natalia"))
                .andExpect(jsonPath("$[0].lastName").value("Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].applicationTransactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

    @Test
    public void getTransactionsOKAllParams() throws Exception { // 200
        /*List<SaleTransaction> transactions = new ArrayList<SaleTransaction>();
        transactions.add(getSaleTransaction());

        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions").principal(auth)
                .param("search",
                        "accountNumber:67326509,amount>4592,firstName:Natalia,lastName:Quiros,legalEntity:[MCM-R2K],processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].firstName").value("Natalia"))
                .andExpect(jsonPath("$[0].lastName").value("Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].applicationTransactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

    @Test
    public void getTransactionsNoSearchParam() throws Exception { // 200
        /*List<SaleTransaction> transactions = new ArrayList<SaleTransaction>();
        transactions.add(getSaleTransaction());

        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(
                get("/api/transactions").principal(auth).param("search", "").param("page", "1").param("size", "2"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].firstName").value("Natalia"))
                .andExpect(jsonPath("$[0].lastName").value("Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].applicationTransactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

    @Test
    public void getTransactionsNoParams() throws Exception { // 400

        mockMvc.perform(get("/api/transactions")).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionsNoParamsButPage() throws Exception { // 400
        mockMvc.perform(get("/api/transactions").param("page", "1")).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionsNoParamsButSize() throws Exception { // 400

        mockMvc.perform(get("/api/transactions").param("size", "2")).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionsNoParamsButNull() throws Exception { // 400

        mockMvc.perform(get("/api/transactions").param("page", "null").param("size", "null"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionsInternalServerError() throws Exception { // 500

       /* Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenThrow(new CustomException(""));

        mockMvc.perform(
                get("/api/transactions").principal(auth).param("search", "").param("page", "1").param("size", "2"))
                .andExpect(status().isInternalServerError());

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());

        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

    @Test
    public void getTransactionsNoLegalEntities() throws Exception { // 200
        /*List<SaleTransaction> transactions = new ArrayList<SaleTransaction>();
        transactions.add(getSaleTransaction());

        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions").principal(auth)
                .param("search",
                        "accountNumber:67326509,amount>4592,firstName:Natalia,lastName:Quiros,legalEntity:[],processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].accountNumber").value("67326509"))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal(4592.36)))
                .andExpect(jsonPath("$[0].cardNumberLast4Char").value("XXXX-XXXX-XXXX-5162"))
                .andExpect(jsonPath("$[0].createdDate").value("2016-06-07T18:05:56.555Z"))
                .andExpect(jsonPath("$[0].firstName").value("Natalia"))
                .andExpect(jsonPath("$[0].lastName").value("Quiros"))
                .andExpect(jsonPath("$[0].legalEntity").value("MCM-R2K"))
                .andExpect(jsonPath("$[0].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[0].applicationTransactionId").value("532673163"))
                .andExpect(jsonPath("$[0].transactionStatusCode").value("APPROVED"))
                .andExpect(jsonPath("$[0].transactionType").value("SALE"));

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verify(transactionService, Mockito.times(1)).getTransactions(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

   /* @Test
    public void getTransactionsUnauthorizedLegalEntities() throws Exception { // 401
        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        mockMvc.perform(get("/api/transactions").principal(auth)
                .param("search",
                        "accountNumber:67326509,amount>4592,firstName:Natalia,lastName:Quiros,legalEntity:[test],processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isUnauthorized());

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);
    }*/

    @Test
    public void getTransactionsAccessDenied() throws Exception { // 401
        mockMvc.perform(get("/api/transactions")
                .param("search",
                        "accountNumber:67326509,amount>4592,firstName:Natalia,lastName:Quiros,legalEntity:[test],processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isUnauthorized());
        Mockito.verify(transactionService, Mockito.times(0)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void getTransactionsEmptyLegalEntities() throws Exception { // 200
       /* List<SaleTransaction> transactions = new ArrayList<SaleTransaction>();
        transactions.add(getSaleTransaction());

        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions").principal(auth)
                .param("search",
                        "accountNumber:67326509,amount>4592,firstName:Natalia,lastName:Quiros,legalEntity:,processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

    @Test
    public void getTransactionsInvalidLegalEntities() throws Exception { // 400
        /*List<SaleTransaction> transactions = new ArrayList<SaleTransaction>();
        transactions.add(getSaleTransaction());

        Mockito.when(transactionService.getLegalEntitiesFromUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntities());
        Mockito.when(transactionService.getTransactions(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions").principal(auth)
                .param("search",
                        "accountNumber:67326509,amount>4592,firstName:Natalia,lastName:Quiros,legalEntity:test,processorName:JETPAY,transactionId:532673163,transactionStatusCode:APPROVED,transactionType:Sale")
                .param("page", "1").param("size", "2")).andExpect(status().isBadRequest());

        Mockito.verify(transactionService, Mockito.times(1)).getLegalEntitiesFromUser(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(transactionService);*/
    }

}
