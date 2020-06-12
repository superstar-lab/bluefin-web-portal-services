package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.BasicPaymentProcessorResource;
import com.mcmcg.ico.bluefin.util.TestUtilClass;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class PaymentProcessorServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private PaymentProcessorDAO paymentProcessorRepository;

    @InjectMocks
    @Autowired
    private PaymentProcessorService paymentProcessorService;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        // http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html
        // This issue is fixed in spring version 4.3.1, but spring boot
        // 3.6-RELEASE supports 4.2.7
        PaymentProcessorService ppService = (PaymentProcessorService) TestUtilClass
                .unwrapProxy(paymentProcessorService);

        ReflectionTestUtils.setField(ppService, "paymentProcessorRepository", paymentProcessorRepository);
    }

    // Get Payment Processor by id

    /**
     * Test the success case when the get by id is performed correctly an return
     * a valid payment processor
     */
    @Test
    public void testGetPaymentProcessorSuccess() {
        PaymentProcessor paymentProcessor = createValidPaymentProcessor();

        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(1L)).thenReturn(paymentProcessor);

        paymentProcessorService.getPaymentProcessorById(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(1L);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when the object is going to be found and does not exist by
     * id in the DB
     */
    @Test
    public void testGetPaymentProcessorSuccessNotFound() {
        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(1L)).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to process request payment processor doesn't exists with given id = [1]");

        paymentProcessorService.getPaymentProcessorById(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(1L);
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to find by id
     */
    @Test
    public void testGetPaymentProcessorRuntimeExceptionSaving() {
        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(Mockito.anyLong())).thenThrow(new RuntimeException(""));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.getPaymentProcessorById(Mockito.anyLong());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    // Get Payment Processors

    /**
     * Test the success path when trying to get all payment processors from DB
     */
    @Test
    public void testGetPaymentProcessors() {
        List<PaymentProcessor> returnedPaymentProcessorList = getValidPaymentProcessorList();
        Mockito.when(paymentProcessorRepository.findAll()).thenReturn(getValidPaymentProcessorList());

        List<com.mcmcg.ico.bluefin.model.PaymentProcessor> result = paymentProcessorService.getPaymentProcessors();

        Assert.assertEquals(returnedPaymentProcessorList, result);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case for when there's not a match for the criteria given
     */
    @Test
    public void testGetPaymentProcessorsNotFound() {
        List<PaymentProcessor> returnedPaymentProcessorList = new ArrayList<PaymentProcessor>();

        Mockito.when(paymentProcessorRepository.findAll()).thenReturn(returnedPaymentProcessorList);

        List<com.mcmcg.ico.bluefin.model.PaymentProcessor> result = paymentProcessorService.getPaymentProcessors();

        Assert.assertTrue(result.isEmpty());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the success path when trying to get all payment processors from DB
     * and a Runtime exception raises
     */
    @Test(expected = RuntimeException.class)
    public void testGetPaymentProcessorsRuntimeException() {
        Mockito.when(paymentProcessorRepository.findAll()).thenThrow(new RuntimeException(""));

        paymentProcessorService.getPaymentProcessors();

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    // Create new PaymentProcessor


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
     * Test the case when a runtime exception shows up when trying to find by id
     */
    @Test
    public void testUpdatePaymentProcessorRuntimeExceptionFindId() {
        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(1L)).thenThrow(new RuntimeException(""));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.updatePaymentProcessor(1L, Mockito.any(BasicPaymentProcessorResource.class));

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to update the
     * payment processor
     */
    @Test
    public void testUpdatePaymentProcessorRuntimeExceptionSaving() {
        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(Mockito.anyLong())).thenReturn(new PaymentProcessor());
        Mockito.when(paymentProcessorRepository.save(Mockito.any(PaymentProcessor.class)))
                .thenThrow(new RuntimeException(""));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.updatePaymentProcessor(Mockito.anyLong(),
                Mockito.any(BasicPaymentProcessorResource.class));

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(Mockito.anyLong());
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

        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(1L)).thenReturn(paymentProcessorToDelete);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(paymentProcessorRepository).delete(paymentProcessorRepository.findByPaymentProcessorId(1L));

        paymentProcessorService.deletePaymentProcessor(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(1L);
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
        }).when(paymentProcessorRepository).delete(paymentProcessorRepository.findByPaymentProcessorId(1L));

        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to process request payment processor doesn't exists with given id = [1]");
        paymentProcessorService.deletePaymentProcessor(1L);

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).save(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to find by id
     */
    @Test
    public void testDeletePaymentProcessorRuntimeExceptionFindId() {
        Mockito.doThrow(new RuntimeException("")).when(paymentProcessorRepository).findByPaymentProcessorId(1L);

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.deletePaymentProcessor(Mockito.anyLong());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(1L);
        Mockito.verify(paymentProcessorRepository, Mockito.times(0)).delete(paymentProcessorRepository.findByPaymentProcessorId(Mockito.anyLong()));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test the case when a runtime exception shows up when trying to delete the
     * payment processor
     */
    @Test
    public void testDeletePaymentProcessorRuntimeExceptionSaving() {
        Mockito.when(paymentProcessorRepository.findByPaymentProcessorId(Mockito.anyLong())).thenReturn(new PaymentProcessor());
        Mockito.doThrow(new RuntimeException("")).when(paymentProcessorRepository)
                .delete(Mockito.any(PaymentProcessor.class));

        expectedEx.expect(RuntimeException.class);

        paymentProcessorService.deletePaymentProcessor(Mockito.anyLong());

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findByPaymentProcessorId(Mockito.anyLong());
        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).delete(Mockito.any(PaymentProcessor.class));
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test when the system does not have payment processor
     */
    @Test(expected = CustomBadRequestException.class)
    public void testGetPaymentProcessorsByIdsEmptyList() {
        Mockito.when(paymentProcessorRepository.findAll()).thenReturn(new ArrayList<PaymentProcessor>());

        paymentProcessorService.getPaymentProcessorsByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L)));

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    /**
     * Test when we pass a wrong payment processor id
     */
    @Test(expected = CustomBadRequestException.class)
    public void testGetPaymentProcessorsByIdsOneWrongElement() {
        Mockito.when(paymentProcessorRepository.findAll()).thenReturn(getValidPaymentProcessorList());

        paymentProcessorService.getPaymentProcessorsByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 5L)));

        Mockito.verify(paymentProcessorRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(paymentProcessorRepository);
    }

    private BasicPaymentProcessorResource createValidPaymentProcessorResource() {
        BasicPaymentProcessorResource paymentProcessorResource = new BasicPaymentProcessorResource();
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
