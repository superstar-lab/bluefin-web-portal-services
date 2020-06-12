package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.util.TestUtilClass;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class RoleServiceTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Mock
	private RoleDAO roleDAO;

	@InjectMocks
	@Autowired
	private RoleService roleService;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);

		// http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html
		// This issue is fixed in spring version 4.3.1, but spring boot
		// 3.6-RELEASE supports 4.2.7
		RoleService rService = (RoleService) TestUtilClass.unwrapProxy(roleService);

		ReflectionTestUtils.setField(rService, "roleDAO", roleDAO);
	}

	@Test
	public void testFindAllSuccess() {
		List<Role> roleList = new ArrayList<Role>();
		roleList.add(new Role());
		Mockito.when(roleDAO.findAll()).thenReturn(roleList);
		roleList = roleService.getRoles();

		Assert.assertFalse(roleList.isEmpty());
		Mockito.verify(roleDAO, Mockito.times(1)).findAll();
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testFindAllFail() {
		Mockito.when(roleDAO.findAll())
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		roleService.getRoles();
		Mockito.verify(roleDAO, Mockito.times(1)).findAll();
	}
}
