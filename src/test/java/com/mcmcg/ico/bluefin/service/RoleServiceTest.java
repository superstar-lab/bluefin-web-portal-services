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
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class RoleServiceTest {

    @InjectMocks
    @Autowired
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindAllSuccess() {
        List<Role> roleList = new ArrayList<Role>();
        roleList.add(new Role());
        Mockito.when(roleRepository.findAll()).thenReturn(roleList);
        roleList = roleService.getRoles();

        Assert.assertFalse(roleList.isEmpty());
        Mockito.verify(roleRepository, Mockito.times(1)).findAll();
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testFindAllFail() {
        Mockito.when(roleRepository.findAll())
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        roleService.getRoles();
        Mockito.verify(roleRepository, Mockito.times(1)).findAll();
    }
}
