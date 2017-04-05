package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.exception.JDBCConnectionException;
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
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.util.TestUtilClass;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class LegalEntityAppServiceTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Mock
	private LegalEntityAppDAO legalEntityAppDAO;
	@Mock
	private UserDAO userDAO;

	@InjectMocks
	@Autowired
	private LegalEntityAppService legalEntityAppService;
	Authentication auth;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);

		// http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html
		// This issue is fixed in spring version 4.3.1, but spring boot
		// 3.6-RELEASE supports 4.2.7
		LegalEntityAppService leaService = (LegalEntityAppService) TestUtilClass.unwrapProxy(legalEntityAppService);

		ReflectionTestUtils.setField(leaService, "legalEntityAppDAO", legalEntityAppDAO);
		ReflectionTestUtils.setField(leaService, "userDAO", userDAO);

		auth = new UsernamePasswordAuthenticationToken("omonge", "password", null);
	}

	/**
	 * Test the success case, a valid username is sent and the user object is
	 * found. The returned list should match the mocked list created for the
	 * valid user and should not be empty, this case will arise an error if the
	 * parameter listOfIds is different from the one in the user's list
	 * (LegalEntities)
	 */
	@Test
	public void testFindAllSuccess() {

		List<LegalEntityApp> legalEntityAppList = findAll();
		List<Long> listOfIds = Arrays.asList(1L);
		// Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppDAO.findAll(listOfIds)).thenReturn(legalEntityAppList);

		List<LegalEntityApp> result = legalEntityAppService.getLegalEntities(auth);

		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(result, legalEntityAppList);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppDAO, Mockito.times(1)).findAll(listOfIds);

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);

	}

	/**
	 * If the user is not found a empty list should be returned
	 */
	@Test
	public void testFindAllUserNameNull() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		List<LegalEntityApp> result = legalEntityAppService.getLegalEntities(auth);

		Assert.assertTrue(result.isEmpty());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	// DB Exceptions
	/**
	 * Test the case for when trying to get the user but a RuntimeException
	 * shows up
	 */
	@Test(expected = RuntimeException.class)
	public void testFindByUserGeneralExceptionFindByUser() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but a RuntimeException shows up
	 */
	@Test(expected = RuntimeException.class)
	public void testFindByUserGeneralExceptionEntities() {
		List<Long> listOfIds = Arrays.asList(1L);
		// Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(legalEntityAppDAO.findAll(listOfIds)).thenThrow(new RuntimeException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppDAO, Mockito.times(1)).findAll(listOfIds);
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is down
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testFindByUserGeneralExceptionLegalEntity() {
		// Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollection()))
		// .thenThrow(new
		// org.springframework.transaction.CannotCreateTransactionException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollection());
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is down
	 */
	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testFindByUserTransactionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is throws a Failure exception
	 */
	@Test(expected = DataAccessResourceFailureException.class)
	public void testFindByUserDataAccessException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new DataAccessResourceFailureException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is throws a JDBC Connection exception
	 */
	@Test(expected = org.hibernate.exception.JDBCConnectionException.class)
	public void testFindByUserJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new JDBCConnectionException("", null));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is down
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testLegalEntityTransactionException() {
		// Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollection()))
		// .thenThrow(new
		// org.springframework.transaction.CannotCreateTransactionException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollection());
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is throws a Failure exception
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = DataAccessResourceFailureException.class)
	public void testLegalEntityDataAccessException() {
		// Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollection()))
		// .thenThrow(new DataAccessResourceFailureException(""));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollection());
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test the case for when trying to get the legal entities method is called
	 * but the DB is throws a JDBC Connection exception
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = org.hibernate.exception.JDBCConnectionException.class)
	public void testLegalEntityJDBCConnectionException() {
		// Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollection()))
		// .thenThrow(new JDBCConnectionException("", null));

		legalEntityAppService.getLegalEntities(auth);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollection());
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test when we send the correct data
	 */
	@Test
	public void testGetLegalEntityAppsByIds() {
		// List<LegalEntityApp> mockedLoadedLegalEntityApps =
		// getValidLegalEntityAppList();
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollectionOf(Long.class)))
		// .thenReturn(mockedLoadedLegalEntityApps);

		Set<Long> expectedLegalEntityAppIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
		List<LegalEntityApp> loadedLegalEntityApps = legalEntityAppService
				.getLegalEntityAppsByIds(expectedLegalEntityAppIds);

		Assert.assertEquals(expectedLegalEntityAppIds.size(), loadedLegalEntityApps.size());
		Assert.assertTrue(
				loadedLegalEntityApps.stream().filter(x -> !expectedLegalEntityAppIds.contains(x.getLegalEntityAppId()))
						.collect(Collectors.toSet()).isEmpty());

		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test when the system does not have roles
	 */
	@Test(expected = CustomBadRequestException.class)
	public void testGetLegalEntityAppsByIdsEmptyList() {
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollectionOf(Long.class)))
		// .thenReturn(new ArrayList<LegalEntityApp>());

		legalEntityAppService.getLegalEntityAppsByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L)));

		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	/**
	 * Test when we pass a wrong role ids
	 */
	@Test
	public void testGetLegalEntityAppsByIdsOneWrongElement() {
		// Mockito.when(legalEntityAppDAO.findAll(Mockito.anyCollectionOf(Long.class)))
		// .thenReturn(getValidLegalEntityAppList());
		expectedEx.expect(CustomBadRequestException.class);
		expectedEx.expectMessage("The following legal entity apps don't exist.  List = [5, 7]");

		legalEntityAppService.getLegalEntityAppsByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 5L, 7L)));

		// Mockito.verify(legalEntityAppDAO,
		// Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
		Mockito.verifyNoMoreInteractions(legalEntityAppDAO);
	}

	private List<LegalEntityApp> findAll() {
		List<LegalEntityApp> list = new ArrayList<LegalEntityApp>();
		LegalEntityApp lea = new LegalEntityApp();
		lea.setLegalEntityAppId(1L);
		lea.setLegalEntityAppName("MCM-R2K");
		list.add(lea);
		return list;
	}

	private User createValidUser() {
		User user = new User();
		user.setEmail("test@email.com");
		user.setFirstName("test");
		user.setLastName("user");
		user.setUsername("userTest");

		Set<UserLegalEntityApp> userLegalEntities = new HashSet<UserLegalEntityApp>();
		// userLegalEntities.add(createValidUserLegalEntity());
		// user.setLegalEntities(userLegalEntities);

		Set<UserRole> userRoles = new HashSet<UserRole>();
		userRoles.add(createValidUserRole());
		// user.setRoles(userRoles);
		return user;
	}

	private UserLegalEntityApp createValidUserLegalEntity() {
		UserLegalEntityApp userLegalEntityApp = new UserLegalEntityApp();
		userLegalEntityApp.setUserLegalEntityAppId(0L);
		// userLegalEntity.setLegalEntityApp(createValidLegalEntityApp());
		return userLegalEntityApp;
	}

	private UserRole createValidUserRole() {
		UserRole userRole = new UserRole();
		userRole.setUserRoleId(0L);
		// userRole.setRole(createValidRole());
		return userRole;
	}

	private List<LegalEntityApp> getValidLegalEntityAppList() {
		List<LegalEntityApp> validLegalEntityAppList = new ArrayList<LegalEntityApp>();
		validLegalEntityAppList.add(createValidLegalEntityApp());

		LegalEntityApp validLegalEntity = new LegalEntityApp();
		validLegalEntity.setLegalEntityAppName("MCM-SSC");
		validLegalEntity.setLegalEntityAppId(2L);
		validLegalEntityAppList.add(validLegalEntity);

		validLegalEntity = new LegalEntityApp();
		validLegalEntity.setLegalEntityAppName("SSC");
		validLegalEntity.setLegalEntityAppId(3L);
		validLegalEntityAppList.add(validLegalEntity);

		return validLegalEntityAppList;
	}

	private LegalEntityApp createValidLegalEntityApp() {
		LegalEntityApp validLegalEntity = new LegalEntityApp();
		UserLegalEntityApp validUserLegalEntity = new UserLegalEntityApp();
		Set<UserLegalEntityApp> validUserLegalEntityList = new HashSet<UserLegalEntityApp>();

		validUserLegalEntityList.add(validUserLegalEntity);
		// validLegalEntity.setUserLegalEntities(validUserLegalEntityList);
		validLegalEntity.setLegalEntityAppName("MCM-R2K");
		validLegalEntity.setLegalEntityAppId(1L);
		return validLegalEntity;
	}

	private Role createValidRole() {
		Role validRole = new Role();
		UserRole validUserRole = new UserRole();
		List<UserRole> validUserRoleList = new ArrayList<UserRole>();
		validUserRoleList.add(validUserRole);
		// validRole.setUserRoles(validUserRoleList);
		validRole.setRoleName("ROLE_TESTING");
		validRole.setDescription("role description");
		validRole.setRoleId(1234L);
		return validRole;
	}
}
