package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorResource;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

public class PaymentProcessorServiceTest {

    @InjectMocks
    @Autowired
    private PaymentProcessorService paymentProcessorService;

    @Mock
    private PaymentProcessorRepository paymentProcessorRepository;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    // Get Payment Processor by id

    /**
     * Test the success case when the get by id is performed correctly an return
     * a valid payment processor
     */
    @Test
    public void testGetPaymentProcessorSuccess() {
        PaymentProcessor paymentProcessor = createValidPaymentProcessor();

        Mockito.when(paymentProcessorRepository.findOne(1L)).thenReturn(paymentProcessor);

        paymentProcessorService.getPaymentProcessorById(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when the object is going to be found and does not exist by
     * id in the DB
     */
    @Test
    public void testGetPaymentProcessorSuccessNotFound() {
        Mockito.when(paymentProcessorRepository.findOne(1L)).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to process request payment processor doesn't exists with given id: 1");

        paymentProcessorService.getPaymentProcessorById(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to find by id
     */
    @Test
    public void testGetPaymentProcessorRuntimeExceptionSaving() {
        Mockito.when(paymentProcessorRepository.findOne(Mockito.anyLong())).thenThrow(new RuntimeException(""));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.getPaymentProcessorById(Mockito.anyLong());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    // Get Payment Processors
    /**
     * Test the success path when trying to get all payment processors from DB
     */
    @Test
    public void testGetPaymentProcessors() {
        Page<PaymentProcessor> returnedPaymentProcessorList = new PageImpl<PaymentProcessor>(
                getValidPaymentProcessorList());
        BooleanExpression exp = QueryDSLUtil.createExpression("paymentProcessorId:1,processorName:PAYSCOUT",
                PaymentProcessor.class);
        PageRequest page = QueryDSLUtil.getPageRequest(0, 1, null);
        Mockito.when(paymentProcessorRepository.findAll(exp, page)).thenReturn(returnedPaymentProcessorList);

        Iterable<PaymentProcessor> result = paymentProcessorService.getPaymentProcessors(exp, 0, 1, null);

        Assert.assertEquals(returnedPaymentProcessorList, result);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll(exp, page);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case for when there's not a match for the criteria given
     */
    @Test
    public void testGetPaymentProcessorsNotFound() {
        List<PaymentProcessor> returnedPaymentProcessorList = new ArrayList<PaymentProcessor>();
        BooleanExpression exp = QueryDSLUtil.createExpression("search", PaymentProcessor.class);
        PageRequest page = QueryDSLUtil.getPageRequest(2, 1, null);

        Mockito.when(paymentProcessorRepository.findAll(exp, page))
                .thenReturn(new PageImpl<PaymentProcessor>(returnedPaymentProcessorList));

        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to find the page requested");

        paymentProcessorService.getPaymentProcessors(exp, 2, 1, null);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll(exp, page);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the success path when trying to get all payment processors from DB
     * and a Runtime exception raises
     */
    @Test(expected = RuntimeException.class)
    public void testGetPaymentProcessorsRuntimeException() {
        BooleanExpression exp = QueryDSLUtil.createExpression("search", PaymentProcessor.class);
        PageRequest page = QueryDSLUtil.getPageRequest(2, 1, null);

        Mockito.when(paymentProcessorRepository.findAll(exp, page)).thenThrow(new RuntimeException(""));

        paymentProcessorService.getPaymentProcessors(exp, 2, 1, null);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll(exp, page);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    // Create new PaymentProcessor

    /**
     * This method will test the success creation of a new payment processor, it
     * will check if the name does not exist to do the insert, bad request
     * exception otherwise
     */
    @Test
    public void testCreatePaymentProcessorSuccess() {
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        PaymentProcessor paymentProcessorExpected = paymentProcessorResource.toPaymentProcessor();

        Mockito.when(paymentProcessorRepository.getPaymentProcessorByProcessorName("PAYSCOUT")).thenReturn(null);
        Mockito.when(paymentProcessorRepository.save(paymentProcessorResource.toPaymentProcessor()))
                .thenReturn(paymentProcessorExpected);

        PaymentProcessor result = paymentProcessorService.createPaymentProcessor(createValidPaymentProcessorResource());

        Assert.assertEquals(paymentProcessorExpected, result);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).getPaymentProcessorByProcessorName("PAYSCOUT");
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).save(paymentProcessorExpected);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when the processor already exists in DB by its name, a bad
     * request exception will be thrown
     */
    @Test
    public void testCreatePaymentProcessorAlreadyExist() {
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        PaymentProcessor paymentProcessorExpected = paymentProcessorResource.toPaymentProcessor();

        Mockito.when(paymentProcessorRepository.getPaymentProcessorByProcessorName("PAYSCOUT"))
                .thenReturn(paymentProcessorExpected);

        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("Unable to create Payment Processor, this processor already exists: PAYSCOUT");

        paymentProcessorService.createPaymentProcessor(createValidPaymentProcessorResource());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).getPaymentProcessorByProcessorName("PAYSCOUT");
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(paymentProcessorExpected);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when trying to request payment processor by name but an
     * exception raises up
     */
    @Test(expected = RuntimeException.class)
    public void testCreatePaymentProcessorByNameRuntimeException() {
        Mockito.when(paymentProcessorRepository.getPaymentProcessorByProcessorName("PAYSCOUT"))
                .thenThrow(new RuntimeException(""));

        paymentProcessorService.createPaymentProcessor(createValidPaymentProcessorResource());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).getPaymentProcessorByProcessorName("PAYSCOUT");
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when trying to save a new payment processor but an
     * exception raises up
     */
    @Test(expected = RuntimeException.class)
    public void testCreatePaymentProcessorSavingRuntimeException() {
        PaymentProcessorResource paymentProcessorResource = createValidPaymentProcessorResource();
        PaymentProcessor paymentProcessorExpected = paymentProcessorResource.toPaymentProcessor();

        Mockito.when(paymentProcessorRepository.getPaymentProcessorByProcessorName("PAYSCOUT")).thenReturn(null);
        Mockito.when(paymentProcessorRepository.save(paymentProcessorResource.toPaymentProcessor()))
                .thenThrow(new RuntimeException(""));

        PaymentProcessor result = paymentProcessorService.createPaymentProcessor(createValidPaymentProcessorResource());

        Assert.assertEquals(paymentProcessorExpected, result);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).getPaymentProcessorByProcessorName("PAYSCOUT");
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).save(paymentProcessorExpected);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    // Update Payment Processor

    /**
     * Test the success case when the update is performed correctly over the
     * object
     */
    @Test
    public void testUpdatePaymentProcessorSuccess() {
        PaymentProcessorResource paymentProcessorResource = new PaymentProcessorResource();

        paymentProcessorResource.setProcessorName("DEBIT");

        PaymentProcessor oldPaymentProcessor = createValidPaymentProcessor();
        PaymentProcessor newPaymentProcessor = paymentProcessorResource.toPaymentProcessor();
        newPaymentProcessor.setPaymentProcessorId(1L);

        Mockito.when(paymentProcessorRepository.findOne(1L)).thenReturn(oldPaymentProcessor);
        Mockito.when(paymentProcessorRepository.save(newPaymentProcessor)).thenReturn(newPaymentProcessor);

        PaymentProcessor result = paymentProcessorService.updatePaymentProcessor(1L, paymentProcessorResource);

        Assert.assertEquals(oldPaymentProcessor.getProcessorName(), result.getProcessorName());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).save(newPaymentProcessor);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when the object that is going to be updated does not exist
     * by id in the DB
     */
    @Test
    public void testUpdatePaymentProcessorSuccessNotFound() {
        PaymentProcessorResource paymentProcessorResource = new PaymentProcessorResource();

        paymentProcessorResource.setProcessorName("DEBIT");

        PaymentProcessor newPaymentProcessor = paymentProcessorResource.toPaymentProcessor();
        newPaymentProcessor.setPaymentProcessorId(1L);

        Mockito.when(paymentProcessorRepository.findOne(1L)).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to process request payment processor doesn't exists with given id: 1");
        paymentProcessorService.updatePaymentProcessor(1L, paymentProcessorResource);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to find by id
     */
    @Test
    public void testUpdatePaymentProcessorRuntimeExceptionFindId() {
        Mockito.when(paymentProcessorRepository.findOne(1L)).thenThrow(new RuntimeException(""));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.updatePaymentProcessor(1L, Mockito.any(PaymentProcessorResource.class));

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to update the
     * payment processor
     */
    @Test
    public void testUpdatePaymentProcessorRuntimeExceptionSaving() {
        Mockito.when(paymentProcessorRepository.findOne(Mockito.anyLong())).thenReturn(new PaymentProcessor());
        Mockito.when(paymentProcessorRepository.save(Mockito.any(PaymentProcessor.class)))
                .thenThrow(new RuntimeException(""));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.updatePaymentProcessor(Mockito.anyLong(), Mockito.any(PaymentProcessorResource.class));

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    // Delete Payment Processor

    /**
     * Test the success case when the delete is performed correctly over the
     * object
     */
    @Test
    public void testDeletePaymentProcessorSuccess() {
        PaymentProcessor paymentProcessorToDelete = new PaymentProcessor();
        paymentProcessorToDelete.setPaymentProcessorId(1L);

        Mockito.when(paymentProcessorRepository.findOne(1L)).thenReturn(paymentProcessorToDelete);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(paymentProcessorRepository).delete(1L);

        paymentProcessorService.deletePaymentProcessor(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).delete(paymentProcessorToDelete);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when the object that is going to be deleted and does not
     * exist by id in the DB
     */
    @Test
    public void testDeletePaymentProcessorSuccessNotFound() {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(paymentProcessorRepository).delete(1L);

        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to process request payment processor doesn't exists with given id: 1");
        paymentProcessorService.deletePaymentProcessor(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to find by id
     */
    @Test
    public void testDeletePaymentProcessorRuntimeExceptionFindId() {
        Mockito.doThrow(new RuntimeException("")).when(paymentProcessorRepository).findOne(1L);

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.deletePaymentProcessor(Mockito.anyLong());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).delete(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to delete the
     * payment processor
     */
    @Test
    public void testDeletePaymentProcessorRuntimeExceptionSaving() {
        Mockito.when(paymentProcessorRepository.findOne(Mockito.anyLong())).thenReturn(new PaymentProcessor());
        Mockito.doThrow(new RuntimeException("")).when(paymentProcessorRepository)
                .delete(Mockito.any(PaymentProcessor.class));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.deletePaymentProcessor(Mockito.anyLong());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).delete(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    private PaymentProcessorResource createValidPaymentProcessorResource() {
        PaymentProcessorResource paymentProcessorResource = new PaymentProcessorResource();
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

}
