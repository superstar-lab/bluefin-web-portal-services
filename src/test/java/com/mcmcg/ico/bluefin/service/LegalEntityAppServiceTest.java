package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class LegalEntityAppServiceTest {
    
    @InjectMocks
    @Autowired
    private LegalEntityAppService legalEntityAppService;
    
    @Mock
    private LegalEntityAppRepository legalEntityAppRepository;
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
     
    
    @Test
    public void testFindAllSuccess() {
        
        List<LegalEntityApp> legalEntityAppList = new ArrayList<LegalEntityApp>(); 
        legalEntityAppList.add(new LegalEntityApp());
        
        Mockito.when(legalEntityAppRepository.findAll()).thenReturn(legalEntityAppList);
        
        legalEntityAppList = legalEntityAppService.findAll();
        
        Assert.assertFalse( legalEntityAppList.isEmpty());
        
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll();
         
    } 
    
    @Test(expected=org.springframework.transaction.CannotCreateTransactionException.class)
    public void testFindAllFail() {
        
        Mockito.when(legalEntityAppRepository.findAll()).thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));
        
        legalEntityAppService.findAll();
        
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll();

    }
}
