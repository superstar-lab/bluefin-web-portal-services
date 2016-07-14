package com.mcmcg.ico.bluefin.rest.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.rest.controller.exception.GeneralRestExceptionHandler;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorService;
import com.mysema.query.types.expr.BooleanExpression;

public class PaymentProcessorControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private PaymentProcessorController PaymentProcessorControllerMock;

    @Mock
    private PaymentProcessorService paymentProcessorService;

    private Authentication auth;

    final static String API = "/api/payment-processors";

    /**
     * Initiates the services that are going to be mocked and then injected to
     * the controller. Set a list of authorities for the Authenticated user
     */
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(PaymentProcessorControllerMock).setControllerAdvice(new GeneralRestExceptionHandler())
                .build();
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        auth = new UsernamePasswordAuthenticationToken("omonge", "password", authorities);
    }

 // Get Payment Processor by id

    /**
     * Test the success path when trying to get payment processor from DB
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessor() throws Exception { // 200
        Mockito.when(paymentProcessorService.getPaymentProcessorById(1L)).thenReturn(createValidPaymentProcessor());

        mockMvc.perform(get(API+"/{id}",1L).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentProcessorId").value(1))
                .andExpect(jsonPath("$.processorName").value("PAYSCOUT"));

        Mockito.verify(paymentProcessorService, Mockito.times(1)).getPaymentProcessorById(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the search by id without authentication
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorBadRequestSearchMissing() throws Exception { // 400

        mockMvc.perform(get(API+"/{id}","test").principal(auth))
                .andExpect(status().isBadRequest());
    }

     
    /**
     * Test the case when the user has not authorization to request information
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorAuthException() throws Exception { // 401
        mockMvc.perform(get(API+"/{id}",1L))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test the case when a runtime exception raises up, like DB exceptions
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorRuntimeException() throws Exception { // 500
        Mockito.when(paymentProcessorService.getPaymentProcessorById(Mockito.anyLong())).thenThrow(new RuntimeException(""));

        mockMvc.perform(get(API+"/{id}",1L).principal(auth))
                .andExpect(status().isInternalServerError());

        Mockito.verify(paymentProcessorService, Mockito.times(1)).getPaymentProcessorById(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }
    
    // Get Payment Processors

    /**
     * Test the success path when trying to get payment processor from DB
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessors() throws Exception { // 200
        Mockito.when(paymentProcessorService.getPaymentProcessors(Mockito.any(BooleanExpression.class),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenReturn(getValidPaymentProcessorList());

        mockMvc.perform(get(API).principal(auth).param("search", "").param("page", "0").param("size", "1"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].paymentProcessorId").value(1))
                .andExpect(jsonPath("$[0].processorName").value("PAYSCOUT"))
                .andExpect(jsonPath("$[1].paymentProcessorId").value(2))
                .andExpect(jsonPath("$[1].processorName").value("JETPAY"))
                .andExpect(jsonPath("$[2].paymentProcessorId").value(3))
                .andExpect(jsonPath("$[2].processorName").value("ACI"));

        Mockito.verify(paymentProcessorService, Mockito.times(1)).getPaymentProcessors(
                Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the search is missing in the criteria given
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorsBadRequestSearchMissing() throws Exception { // 400

        mockMvc.perform(get(API).principal(auth).param("page", "0").param("size", "1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test the case when the page is missing in the criteria given
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorsBadRequestPageMissing() throws Exception { // 400

        mockMvc.perform(get(API).principal(auth).param("search", "").param("size", "1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test the case when the size is missing in the criteria given
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorsBadRequestSizeMissing() throws Exception { // 400
        mockMvc.perform(get(API).principal(auth).param("search", "").param("page", "1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test the case when the user has not authorization to request information
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorsAuthException() throws Exception { // 401
        mockMvc.perform(get(API).param("search", "").param("page", "1").param("size", "1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test the case when a runtime exception raises up, like DB exceptions
     * 
     * @throws Exception
     */
    @Test
    public void testGetPaymentProcessorsRuntimeException() throws Exception { // 500
        Mockito.when(paymentProcessorService.getPaymentProcessors(Mockito.any(BooleanExpression.class),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenThrow(new RuntimeException(""));

        mockMvc.perform(get(API).principal(auth).param("search", "").param("page", "0").param("size", "1"))
                .andExpect(status().isInternalServerError());

        Mockito.verify(paymentProcessorService, Mockito.times(1)).getPaymentProcessors(
                Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    // Create new PaymentProcessor

    /**
     * Test the success case when a new payment processor is created
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePaymentProcessor() throws Exception { // 201
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        PaymentProcessor newPaymentProcessor = paymentProcessorResource.toPaymentProcessor();
        newPaymentProcessor.setPaymentProcessorId(1L);
        Mockito.when(paymentProcessorService.createPaymentProcessor(paymentProcessorResource))
                .thenReturn(newPaymentProcessor);

        mockMvc.perform(post(API).principal(auth).content(convertObjectToJsonBytes(paymentProcessorResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.processorName").value("PAYSCOUT"))
                .andExpect(jsonPath("$.cardType").value("VISA")).andExpect(jsonPath("$.paymentProcessorId").value(1));

        Mockito.verify(paymentProcessorService, Mockito.times(1)).createPaymentProcessor(paymentProcessorResource);
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    /**
     * Test the case when user is not authorized to use this api
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePaymentProcessorUnauthorized() throws Exception { // 401
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();

        mockMvc.perform(post(API).content(convertObjectToJsonBytes(paymentProcessorResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the name of the processor is missing, bad request
     * exception is thrown
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePaymentProcessorBadRequestNameMissing() throws Exception { // 400
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        paymentProcessorResource.setProcessorName(null);

        String validationError = mockMvc
                .perform(post(API).principal(auth).content(convertObjectToJsonBytes(paymentProcessorResource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertThat(validationError, containsString("processorName must not be empty"));

        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the card type of the processor is missing, bad request
     * exception is thrown
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePaymentProcessorBadRequestCardTypeMissing() throws Exception { // 400
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        paymentProcessorResource.setCardType(null);

        String validationError = mockMvc
                .perform(post(API).principal(auth).content(convertObjectToJsonBytes(paymentProcessorResource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertThat(validationError, containsString("cardType must not be empty"));
    }

    /**
     * Test the case when all required elements of the processor are missing,
     * bad request exception is thrown
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePaymentProcessorBadRequestAllMissing() throws Exception { // 400
        PaymentProcessorResource paymentProcessorResource = new PaymentProcessorResource();

        String validationError = mockMvc
                .perform(post(API).principal(auth).content(convertObjectToJsonBytes(paymentProcessorResource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertThat(validationError, containsString("cardType must not be empty"));
        assertThat(validationError, containsString("processorName must not be empty"));
    }

    /**
     * Test the case when an exception raises up, like DB exceptions
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePaymentProcessorRuntimeException() throws Exception { // 500
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        Mockito.when(paymentProcessorService.createPaymentProcessor(paymentProcessorResource))
                .thenThrow(new RuntimeException(""));

        mockMvc.perform(post(API).principal(auth).content(convertObjectToJsonBytes(paymentProcessorResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        Mockito.verify(paymentProcessorService, Mockito.times(1)).createPaymentProcessor(paymentProcessorResource);
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    // Update payment processor

    /**
     * Test the success path when trying to update a existing payment processor
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePaymentProcessorSuccess() throws Exception {// 200
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        PaymentProcessor updatedPaymentProcessor = paymentProcessorResource.toPaymentProcessor();
        updatedPaymentProcessor.setPaymentProcessorId(1L);

        Mockito.when(paymentProcessorService.updatePaymentProcessor(1L, paymentProcessorResource))
                .thenReturn(updatedPaymentProcessor);

        mockMvc.perform(put(API + "/{id}", 1L).principal(auth)
                .content(convertObjectToJsonBytes(paymentProcessorResource)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.processorName").value("PAYSCOUT"))
                .andExpect(jsonPath("$.cardType").value("VISA")).andExpect(jsonPath("$.paymentProcessorId").value(1));

        Mockito.verify(paymentProcessorService, Mockito.times(1)).updatePaymentProcessor(Mockito.anyLong(),
                Mockito.any(PaymentProcessorResource.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the requester user is not authenticated to update a
     * payment processor
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePaymentProcessorUnauthorized() throws Exception {// 401
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        PaymentProcessor updatedPaymentProcessor = paymentProcessorResource.toPaymentProcessor();
        updatedPaymentProcessor.setPaymentProcessorId(1L);

        Mockito.when(paymentProcessorService.updatePaymentProcessor(1L, paymentProcessorResource))
                .thenReturn(updatedPaymentProcessor);

        mockMvc.perform(put(API + "/{id}", 1L).content(convertObjectToJsonBytes(paymentProcessorResource))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the request is not correct, it's missing the name of
     * the payment processor, bad request exceptions will be send
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePaymentProcessorBadRequestNameMissing() throws Exception {// 400
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        paymentProcessorResource.setProcessorName(null);

        String validationError = mockMvc
                .perform(put(API + "/{id}", 1L).principal(auth)
                        .content(convertObjectToJsonBytes(paymentProcessorResource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertThat(validationError, containsString("processorName must not be empty"));

        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the request is not correct, it's missing the card type
     * of the payment processor, bad request exceptions will be send
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePaymentProcessorBadRequestCardTypeMissing() throws Exception {// 400
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        paymentProcessorResource.setCardType(null);

        String validationError = mockMvc
                .perform(put(API + "/{id}", 1L).principal(auth)
                        .content(convertObjectToJsonBytes(paymentProcessorResource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertThat(validationError, containsString("cardType must not be empty"));

        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the request is not correct, all elements of a payment
     * processor are missing , bad request exceptions will be send
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePaymentProcessorBadRequestAllMissing() throws Exception {// 400
        PaymentProcessorResource paymentProcessorResource = new PaymentProcessorResource();

        String validationError = mockMvc
                .perform(put(API + "/{id}", 1L).principal(auth)
                        .content(convertObjectToJsonBytes(paymentProcessorResource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertThat(validationError, containsString("processorName must not be empty"));
        assertThat(validationError, containsString("cardType must not be empty"));

        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when there's an error when trying to update a payment
     * processor
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePaymentProcessorRuntimeException() throws Exception {// 500
        Mockito.when(paymentProcessorService.updatePaymentProcessor(Mockito.anyLong(),
                Mockito.any(PaymentProcessorResource.class))).thenThrow(new RuntimeException(""));

        mockMvc.perform(put(API + "/{id}", 1L).principal(auth)
                .content(convertObjectToJsonBytes(createValidPaymentProcessorResource()))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        Mockito.verify(paymentProcessorService, Mockito.times(1)).updatePaymentProcessor(Mockito.anyLong(),
                Mockito.any(PaymentProcessorResource.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    // Delete payment processor

    /**
     * Test the success path when trying to delete a existing payment processor
     * 
     * @throws Exception
     */
    @Test
    public void testDeletePaymentProcessorSuccess() throws Exception {// 200
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(paymentProcessorService).deletePaymentProcessor(1L);

        mockMvc.perform(delete(API + "/{id}", 1L).principal(auth)).andExpect(status().isOk());

        Mockito.verify(paymentProcessorService, Mockito.times(1)).deletePaymentProcessor(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    /**
     * Test the case when the requester user is not authenticated to delete a
     * payment processor
     * 
     * @throws Exception
     */
    @Test
    public void testDeletePaymentProcessorUnauthorized() throws Exception {// 401
        mockMvc.perform(delete(API + "/{id}", 1L)).andExpect(status().isUnauthorized());
        Mockito.verifyZeroInteractions(paymentProcessorService);
    }

    /**
     * Test the case when there's an error when trying to delete a payment
     * processor
     * 
     * @throws Exception
     */
    @Test
    public void testDeletePaymentProcessorRuntimeException() throws Exception {// 500
        Mockito.doThrow(new RuntimeException("")).when(paymentProcessorService).deletePaymentProcessor(1L);

        mockMvc.perform(delete(API + "/{id}", 1L).principal(auth)).andExpect(status().isInternalServerError());

        Mockito.verify(paymentProcessorService, Mockito.times(1)).deletePaymentProcessor(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorService);
    }

    private PaymentProcessorResource createValidPaymentProcessorResource() {
        PaymentProcessorResource paymentProcessorResource = new PaymentProcessorResource();
        paymentProcessorResource.setCardType("VISA");
        paymentProcessorResource.setProcessorName("PAYSCOUT");
        return paymentProcessorResource;
    }

    private List<PaymentProcessor> getValidPaymentProcessorList() {
        List<PaymentProcessor> paymentProcessorList = new ArrayList<PaymentProcessor>();
        paymentProcessorList.add(createValidPaymentProcessor());

        PaymentProcessor paymentProcessor = createValidPaymentProcessor();
        paymentProcessor.setPaymentProcessorId(2L);
        paymentProcessor.setProcessorName("JETPAY");
        paymentProcessorList.add(paymentProcessor);

        paymentProcessor = createValidPaymentProcessor();
        paymentProcessor.setPaymentProcessorId(3L);
        paymentProcessor.setProcessorName("ACI");
        paymentProcessorList.add(paymentProcessor);

        return paymentProcessorList;
    }

    private PaymentProcessor createValidPaymentProcessor() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setPaymentProcessorId(1L);
        paymentProcessor.setProcessorName("PAYSCOUT");
        return paymentProcessor;
    }

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

}
