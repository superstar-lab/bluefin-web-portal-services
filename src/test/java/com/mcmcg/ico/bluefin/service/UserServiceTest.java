package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.CannotCreateTransactionException;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.util.TestUtilClass;
import com.mysema.query.types.expr.BooleanExpression;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class UserServiceTest {
	@Mock
	private UserDAO userDAO;
	@Mock
	private RoleService roleService;
	@Mock
	private SessionService sessionService;
	@Mock
	private LegalEntityAppService legalEntityAppService;
	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@InjectMocks
	@Autowired
	private UserService userService;

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	Authentication auth;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);

		// http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html
		// This issue is fixed in spring version 4.3.1, but spring boot
		// 3.6-RELEASE supports 4.2.7
		UserService uService = (UserService) TestUtilClass.unwrapProxy(userService);

		ReflectionTestUtils.setField(uService, "userDAO", userDAO);
		ReflectionTestUtils.setField(uService, "roleService", roleService);
		ReflectionTestUtils.setField(uService, "legalEntityAppService", legalEntityAppService);
		ReflectionTestUtils.setField(uService, "sessionService", sessionService);
		ReflectionTestUtils.setField(uService, "passwordEncoder", passwordEncoder);

		auth = new UsernamePasswordAuthenticationToken("omonge", "password", null);
	}

	// Get user info

	@Test
	public void testGetUserInformationSuccess() { // 200
		User user = createValidUser();
		Mockito.when(userDAO.findByUsername("userTest")).thenReturn(user);

		UserResource userResource = userService.getUserInfomation("userTest");

		Assert.assertEquals(user.getEmail(), userResource.getEmail());
		Assert.assertEquals(user.getFirstName(), userResource.getFirstName());
		Assert.assertEquals(user.getLastName(), userResource.getLastName());
		Assert.assertEquals(user.getUsername(), userResource.getUsername());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = CustomBadRequestException.class)
	public void testGetUserInfoNotFound() {// 404

		Mockito.when(userDAO.findByUsername("omonge")).thenReturn(new User());

		userService.getUserInfomation("mytest");

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = RuntimeException.class)
	public void testFindByUsernameRuntimeException() { // 500

		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

		userService.getUserInfomation("mytest");

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);

	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testFindByUsername() { // 500

		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.getUserInfomation("mytest");

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);

	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testFindByUsernameDBAccessFail() {// 500

		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(null));

		userService.getUserInfomation("mytest");

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);

	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testFindByUsernameDBConnectionFail() {// 500

		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new DataAccessResourceFailureException("", null));

		userService.getUserInfomation("mytest");

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);

	}

	// Get Legal Entities by user name

	/**
	 * Test success path for Legal Entities by user name
	 */
	@Test
	public void testGetLegalEntitiesByUser() {
		User user = createValidUser();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(user);

		// List<LegalEntityApp> result =
		// userService.getLegalEntitiesByUser("omonge");

		// Assert.assertEquals(user.getLegalEntityApps(), result);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test success path for Legal Entities by user name, empty list is return
	 * if user not found
	 */
	@Test
	public void testGetLegalEntitiesByUserNotFound() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// List<LegalEntityApp> result =
		// userService.getLegalEntitiesByUser("omonge");

		// Assert.assertTrue(result.isEmpty());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test runtime exception when trying to get information
	 */
	@Test
	public void testGetLegalEntitiesByUserRunTimeException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException());
		expectedEx.expect(RuntimeException.class);

		userService.getLegalEntitiesByUser("omonge");

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	// Get Users

	/**
	 * Test the success case where the user is allowed to get the list of users
	 * according with the LE that are owned, in other words, all LE that are
	 * sent in the search criteria belong to the consultan user
	 */
	@Test
	public void testGetUsers() {// 200
		Page<User> list = new PageImpl<User>(getValidUsers());
		User searchUser = createValidUser();
		Mockito.when(userDAO.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
				.thenReturn(list);

		Iterable<User> result = null;
		//userService				.getUsers(QueryDSLUtil.createExpression("legalEntities:[64,77,27,87]", User.class), 1, 1, null);

		for (User resultUser : result) {
			Assert.assertEquals("test@email.com", resultUser.getEmail());
			Assert.assertEquals("test", resultUser.getFirstName());
			Assert.assertEquals("user", resultUser.getLastName());
			Assert.assertEquals("userTest", resultUser.getUsername());
			// Assert.assertEquals(searchUser.getLegalEntityApps(),
			// resultUser.getLegalEntityApps());
			// Assert.assertEquals(searchUser.getRoleNames(),
			// resultUser.getRoleNames());

		}
		Mockito.verify(userDAO, Mockito.times(1)).findAll(Mockito.any(BooleanExpression.class),
				Mockito.any(PageRequest.class));
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case where no information was found with the criteria used in
	 * the search parameter
	 */
	@Test
	public void testGetUsersNotFound() {// 404
		Page<User> list = new PageImpl<User>(new ArrayList<User>());

		Mockito.when(userDAO.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
				.thenReturn(list);

		expectedEx.expect(CustomNotFoundException.class);
		expectedEx.expectMessage("Unable to find the page requested");

		//userService.getUsers(QueryDSLUtil.createExpression("legalEntities:[64,77,27,87]", User.class), 2, 1, null);

		Mockito.verify(userDAO, Mockito.times(1)).findAll(Mockito.any(BooleanExpression.class),
				Mockito.any(PageRequest.class));
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case where a RuntimeException rises for when get all the list of
	 * users according with the criteria given
	 */
	@Test
	public void testGetUsersRuntimeExceptionFindAll() {
		Mockito.when(userDAO.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
				.thenThrow(new RuntimeException());

		expectedEx.expect(RuntimeException.class);

	//	userService.getUsers(QueryDSLUtil.createExpression("legalEntities:[64,77,27,87]", User.class), 1, 1, null);

		Mockito.verify(userDAO, Mockito.times(1)).findAll(Mockito.any(BooleanExpression.class),
				Mockito.any(PageRequest.class));
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	// Register user tests

	@Test
	public void testRegisterUserOK() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		// Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(createValidUser());
		List<LegalEntityApp> expectedLegalEntityApps = new ArrayList<LegalEntityApp>();
		expectedLegalEntityApps.add(createValidLegalEntityApp());
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(expectedLegalEntityApps);
		List<Role> expectedRoles = new ArrayList<Role>();
		expectedRoles.add(createValidRole());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(expectedRoles);
		Mockito.when(
				sessionService.generateNewToken(Mockito.anyString(), Mockito.any(TokenType.class), Mockito.anyString()))
				.thenReturn("testToken");

		UserResource result = userService.registerNewUserAccount(newUser);

		Assert.assertEquals(newUser.getEmail(), result.getEmail());
		Assert.assertEquals(newUser.getFirstName(), result.getFirstName());
		Assert.assertEquals(newUser.getLastName(), result.getLastName());
		Assert.assertEquals(newUser.getUsername(), result.getUsername());

		// Optional<Role> actualRole = result.getRoles().stream().findFirst();
		// Assert.assertTrue(actualRole.isPresent());
		// Assert.assertEquals(expectedRoles.get(0).getRoleId(),
		// actualRole.get().getRoleId());
		// Assert.assertEquals(expectedRoles.get(0).getRoleName(),
		// actualRole.get().getRoleName());
		// Assert.assertEquals(expectedRoles.get(0).getDescription(),
		// actualRole.get().getDescription());

		// Optional<LegalEntityApp> actualLegalEntityApp =
		// result.getLegalEntityApps().stream().findFirst();
		// Assert.assertTrue(actualLegalEntityApp.isPresent());
		// Assert.assertEquals(expectedLegalEntityApps.get(0).getLegalEntityAppId(),
		// actualLegalEntityApp.get().getLegalEntityAppId());
		// Assert.assertEquals(expectedLegalEntityApps.get(0).getLegalEntityAppName(),
		// actualLegalEntityApp.get().getLegalEntityAppName());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
	}

	@Test(expected = RuntimeException.class)
	public void testRegisterUserRuntimeExceptionFindUserName() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserDBFailsFindUserName() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserDBFailsFindByRoleId() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserDBFailsFindByRoleName() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());

		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserDBFailsSaveNewUser() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserDBFailsSaveLegalEntity() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserDBFailsSaveRole() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testRegisterUserDBFailsFindUserNameAccess() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testRegisterUserDBFailsFindByRole() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testRegisterUserDBFailsFindByRoleNameAccess() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());

		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testRegisterUserDBFailsSaveNewUserAccess() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class)))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
	public void testRegisterUserDBFailsSaveLegalEntityAccess() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(null);

		Mockito.when(userDAO.saveUser(Mockito.any(User.class)))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));

		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException.class)
	public void testRegisterUserInvalidRolesBadRequest() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(null);
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new CustomBadRequestException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException.class)
	public void testRegisterUserInvalidLegalEntitiesBadRequest() {
		RegisterUserResource newUser = createValidRegisterResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(null);
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new CustomBadRequestException(""));

		userService.registerNewUserAccount(newUser);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test
	public void testRegisterExistingUserBadRequest() {
		User existingUser = new User();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(existingUser);
		expectedEx.expect(CustomBadRequestException.class);
		expectedEx.expectMessage("Unable to create the account, this username already exists: userTest");

		userService.registerNewUserAccount(createValidRegisterResource());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testRegisterUserFail() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(null);
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());

		Mockito.when(userDAO.saveUser(Mockito.any(User.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.registerNewUserAccount(createValidRegisterResource());

		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
	}

	// Update user tests

	@Test
	public void testUpdateUserOK() {
		UpdateUserResource user = createValidUpdateResource();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(createValidUser());

		UserResource result = userService.updateUserProfile("userTest", user, null);

		Assert.assertEquals("test@email.com", result.getEmail());
		Assert.assertEquals("test", result.getFirstName());
		Assert.assertEquals("user", result.getLastName());
		Assert.assertEquals("userTest", result.getUsername());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
	}

	@Test
	public void testUpdateNotExistingUserBadRequest() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		expectedEx.expect(CustomBadRequestException.class);
		expectedEx.expectMessage("Unable to find user by username provided: userTest");

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserFindByUsernameCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserFindByUsernameDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserFindByUsernameJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserSaveCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserSaveDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserSaveJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserProfile("userTest", createValidUpdateResource(), null);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	// Update user roles tests

	@Test
	public void testUpdateUserRolesOK() {
		Set<Long> roles = createValidRoleIdsList();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		// Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(createValidUser());

		User result = userService.updateUserRoles("userTest", roles);

		Assert.assertEquals("test@email.com", result.getEmail());
		Assert.assertEquals("test", result.getFirstName());
		Assert.assertEquals("user", result.getLastName());
		Assert.assertEquals("userTest", result.getUsername());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verify(userDAO, Mockito.times(2)).saveUser(Mockito.any(User.class));
	}

	@Test(expected = com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException.class)
	public void testUpdateUserRolesInvalidRolesBadRequest() {
		Set<Long> roles = createValidRoleIdsList();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new CustomBadRequestException(""));

		userService.updateUserRoles("userTest", roles);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test
	public void testUpdateUserRolesNotExistingUserBadRequest() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		expectedEx.expect(CustomBadRequestException.class);
		expectedEx.expectMessage("Unable to find user by username provided: userTest");

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserRolesFindByUsernameCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserRolesFindByUsernameDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserRolesFindByUsernameJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserFindByRoleNameCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserFindByRoleIdDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserRolesFindByRoleNameJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserRolesSaveCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserRolesSaveDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());

		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserRolesSaveJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserRoles("userTest", createValidRoleIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(roleService);
	}

	// Update user legalEntities tests

	@Test
	public void testUpdateUserLegalEntitiesOK() {
		Set<Long> legalEntities = createValidLegalEntityIdsList();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());

		// Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenReturn(createValidUser());

		User result = userService.updateUserLegalEntities("userTest", legalEntities);

		Assert.assertEquals("test@email.com", result.getEmail());
		Assert.assertEquals("test", result.getFirstName());
		Assert.assertEquals("user", result.getLastName());
		Assert.assertEquals("userTest", result.getUsername());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
	}

	@Test(expected = CustomBadRequestException.class)
	public void testUpdateUserLegalEntitiesInvalidLegalEntitiesBadRequest() {
		Set<Long> legalEntities = createValidLegalEntityIdsList();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new CustomBadRequestException(""));

		userService.updateUserLegalEntities("userTest", legalEntities);

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(userDAO, Mockito.times(1)).saveUser(Mockito.any(User.class));
	}

	@Test
	public void testUpdateUserLegalEntitiesNotExistingUserBadRequest() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);
		expectedEx.expect(CustomBadRequestException.class);
		expectedEx.expectMessage("Unable to find user by username provided: userTest");

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserLegalEntitiesFindByUsernameCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserLegalEntitiesFindByUsernameDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserLegalEntitiesFindByUsernameJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString()))
				.thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserFindByLegalEntityAppIdCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserFindByLegalEntityAppNameDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserLegalEntitiesFindByLegalEntityAppNameJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
				.thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
	public void testUpdateUserLegalEntitiesSaveCannotCreateTransaction() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class)))
				.thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserLegalEntitiesSaveDataAccessResourceFailureException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new DataAccessResourceFailureException(""));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void testUpdateUserLegalEntitiesSaveJDBCConnectionException() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
		// Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
		// .thenReturn(createValidLegalEntityAppList());
		Mockito.when(userDAO.saveUser(Mockito.any(User.class))).thenThrow(new DataAccessResourceFailureException("", null));

		userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
		Mockito.verifyNoMoreInteractions(userDAO);
		Mockito.verifyNoMoreInteractions(legalEntityAppService);
	}

	/**
	 * Test the success case when the users exists and have a valid list of
	 * legal entities
	 */
	@Test
	public void testGetLegalEntitiesByUserNameSuccess() {
		User user = createValidUser();
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(createValidUser());

		// List<LegalEntityApp> result =
		// userService.getLegalEntitiesByUser("omonge");

		// Assert.assertEquals(user.getLegalEntityApps().get(0).getLegalEntityAppId(),
		// result.get(0).getLegalEntityAppId());
		// Assert.assertEquals("legalEntity1",
		// result.get(0).getLegalEntityAppName());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case when the users does not exists, empty list will be returned
	 */
	@Test
	public void testGetLegalEntitiesByUserNameNoUserFound() {
		Mockito.when(userDAO.findByUsername(Mockito.anyString())).thenReturn(null);

		// List<LegalEntityApp> result =
		// userService.getLegalEntitiesByUser("omonge");

		// Assert.assertTrue(result.isEmpty());

		Mockito.verify(userDAO, Mockito.times(1)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	// Legal Entity verification

	/**
	 * Test the case when the user is allowed to update a user with some legal
	 * entity related
	 */
	@Test
	public void testUpdateUserLegalEntitiesAllowedByLegalEntityMe() {

		Boolean result = userService.belongsToSameLegalEntity(auth, "omonge");

		Assert.assertTrue(result);

		Mockito.verify(userDAO, Mockito.times(0)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case when the user is allowed to update a user with some legal
	 * entity related
	 */
	@Test
	public void testUpdateUserLegalEntitiesAllowedByLegalEntity() {
		// Adds 1 element equals as the for the consultant user's legal entity
		// list
		LegalEntityApp legalEntity = new LegalEntityApp();
		legalEntity.setLegalEntityAppId(1L);
		User userToUpdate = getUserOneLegalEntity();
		User requestUser = getUserMoreLegalEntities();

		Mockito.when(userDAO.findByUsername("omonge")).thenReturn(requestUser);
		Mockito.when(userDAO.findByUsername("nquiros")).thenReturn(userToUpdate);

		Boolean result = userService.belongsToSameLegalEntity(auth, "nquiros");

		Assert.assertTrue(result);

		Mockito.verify(userDAO, Mockito.times(2)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case when the user is not allowed to update a user because are
	 * not related with some lega entity
	 */
	@Test
	public void testUpdateUserLegalEntitiesNotAllowedByLegalEntity() {
		Mockito.when(userDAO.findByUsername("omonge")).thenReturn(getUserMoreLegalEntities());
		Mockito.when(userDAO.findByUsername("nquiros")).thenReturn(createValidUser());

		Boolean result = userService.belongsToSameLegalEntity(auth, "nquiros");

		Assert.assertFalse(result);

		Mockito.verify(userDAO, Mockito.times(2)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case when a error raises when consulting database
	 */
	@Test
	public void testUpdateUserLegalEntitiesRuntimeExceptionFindUser1() {
		// Adds 1 element equals as the for the consultant user's legal entity
		// list
		LegalEntityApp legalEntity = new LegalEntityApp();
		legalEntity.setLegalEntityAppId(1L);
		User userToUpdate = getUserOneLegalEntity();

		Mockito.when(userDAO.findByUsername("omonge")).thenThrow(new RuntimeException());
		Mockito.when(userDAO.findByUsername("nquiros")).thenReturn(userToUpdate);
		expectedEx.expect(RuntimeException.class);

		userService.belongsToSameLegalEntity(auth, "nquiros");

		Mockito.verify(userDAO, Mockito.times(2)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	/**
	 * Test the case when a error raises when consulting database
	 */
	@Test
	public void testUpdateUserLegalEntitiesRuntimeExceptionFindUser2() {
		// Adds 1 element equals as the for the consultant user's legal entity
		// list
		LegalEntityApp legalEntity = new LegalEntityApp();
		legalEntity.setLegalEntityAppId(1L);
		User userToUpdate = getUserOneLegalEntity();

		Mockito.when(userDAO.findByUsername("omonge")).thenReturn(userToUpdate);
		Mockito.when(userDAO.findByUsername("nquiros")).thenThrow(new RuntimeException());
		expectedEx.expect(RuntimeException.class);

		userService.belongsToSameLegalEntity(auth, "nquiros");

		Mockito.verify(userDAO, Mockito.times(2)).findByUsername(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(userDAO);
	}

	//

	private UpdateUserResource createValidUpdateResource() {
		UpdateUserResource user = new UpdateUserResource();
		user.setEmail("test@email.com");
		user.setFirstName("test");
		user.setLastName("user");
		return user;
	}

	private RegisterUserResource createValidRegisterResource() {
		RegisterUserResource newUser = new RegisterUserResource();
		newUser.setEmail("test@email.com");
		newUser.setFirstName("test");
		newUser.setLastName("user");
		newUser.setLegalEntityApps(createValidLegalEntityIdsList());
		newUser.setRoles(createValidRoleIdsList());
		newUser.setUsername("userTest");
		return newUser;
	}

	private User createValidUser() {
		User user = new User();
		user.setEmail("test@email.com");
		user.setFirstName("test");
		user.setLastName("user");
		user.setUsername("userTest");

		List<UserLegalEntityApp> userLegalEntities = new ArrayList<UserLegalEntityApp>();
		userLegalEntities.add(createValiduserLegalEntityApp());
		// user.setLegalEntities(userLegalEntities);

		List<UserRole> userRoles = new ArrayList<UserRole>();
		userRoles.add(createValidUserRole());
		// user.setRoles(userRoles);
		return user;
	}

	private UserLegalEntityApp createValiduserLegalEntityApp() {
		UserLegalEntityApp userLegalEntityApp = new UserLegalEntityApp();
		// userLegalEntityApp.setuserLegalEntityAppAppId(0L);
		// userLegalEntityApp.setLegalEntityApp(createValidLegalEntityApp());
		return userLegalEntityApp;
	}

	private UserRole createValidUserRole() {
		UserRole userRole = new UserRole();
		userRole.setUserRoleId(0L);
		// userRole.setRole(createValidRole());
		return userRole;
	}

	private LegalEntityApp createValidLegalEntityApp() {
		LegalEntityApp validLegalEntity = new LegalEntityApp();
		UserLegalEntityApp validuserLegalEntityApp = new UserLegalEntityApp();
		Set<UserLegalEntityApp> validuserLegalEntityAppList = new HashSet<UserLegalEntityApp>();
		validuserLegalEntityAppList.add(validuserLegalEntityApp);
		// validLegalEntity.setUserLegalEntities(validuserLegalEntityAppList);
		validLegalEntity.setLegalEntityAppName("legalEntity1");
		validLegalEntity.setLegalEntityAppId(4321L);
		return validLegalEntity;
	}

	private Role createValidRole() {
		UserRole validUserRole = new UserRole();
		List<UserRole> validUserRoleList = new ArrayList<UserRole>();
		validUserRoleList.add(validUserRole);

		Role validRole = new Role();
		// validRole.setUserRoles(validUserRoleList);
		validRole.setRoleName("ROLE_TESTING");
		validRole.setDescription("role description");
		validRole.setRoleId(1234L);

		return validRole;
	}

	private Set<Long> createValidRoleIdsList() {
		Set<Long> roles = new LinkedHashSet<Long>();
		roles.add(42L);
		roles.add(33L);
		roles.add(52L);
		roles.add(16L);
		roles.add(89L);
		return roles;
	}

	private List<Role> createValidRoleList() {
		List<Role> roles = new ArrayList<Role>();

		for (Long id : createValidRoleIdsList()) {
			Role validRole = new Role();
			validRole.setRoleName("ROLE_TESTING_" + id);
			validRole.setDescription("role description " + id);
			validRole.setRoleId(id);

			roles.add(validRole);
		}

		return roles;
	}

	private Set<Long> createValidLegalEntityIdsList() {
		Set<Long> roles = new LinkedHashSet<Long>();
		roles.add(64L);
		roles.add(77L);
		roles.add(27L);
		roles.add(87L);
		roles.add(62L);
		return roles;
	}

	private List<LegalEntityApp> createValidLegalEntityAppList() {
		List<LegalEntityApp> legalEntityApps = new ArrayList<LegalEntityApp>();

		for (Long id : createValidLegalEntityIdsList()) {
			LegalEntityApp validLegalEntityApp = new LegalEntityApp();
			validLegalEntityApp.setLegalEntityAppName("legalEntity_" + id);
			validLegalEntityApp.setLegalEntityAppId(id);

			legalEntityApps.add(validLegalEntityApp);
		}

		return legalEntityApps;
	}

	/**
	 * Create a list with valid users
	 * 
	 * @return List of valid users
	 */
	private List<User> getValidUsers() {
		List<User> resultList = new ArrayList<User>();
		resultList.add(createValidUser());
		return resultList;
	}

	private User getUserMoreLegalEntities() {

		User user = createValidUser();
		List<UserLegalEntityApp> userLegalEntities = new ArrayList<UserLegalEntityApp>();
		UserLegalEntityApp userLegalEntityApp = new UserLegalEntityApp();
		LegalEntityApp validLegalEntity = new LegalEntityApp();

		validLegalEntity.setLegalEntityAppName("legalEntity64");
		validLegalEntity.setLegalEntityAppId(1L);
		// userLegalEntityApp.setLegalEntityApp(validLegalEntity);
		userLegalEntities.add(userLegalEntityApp);

		userLegalEntityApp = new UserLegalEntityApp();
		validLegalEntity = new LegalEntityApp();
		validLegalEntity.setLegalEntityAppName("legalEntity77");
		validLegalEntity.setLegalEntityAppId(2l);
		// userLegalEntityApp.setLegalEntityApp(validLegalEntity);
		userLegalEntities.add(userLegalEntityApp);

		userLegalEntityApp = new UserLegalEntityApp();
		validLegalEntity = new LegalEntityApp();
		validLegalEntity.setLegalEntityAppName("legalEntity27");
		validLegalEntity.setLegalEntityAppId(3l);
		// userLegalEntityApp.setLegalEntityApp(validLegalEntity);
		userLegalEntities.add(userLegalEntityApp);

		userLegalEntityApp = new UserLegalEntityApp();
		validLegalEntity = new LegalEntityApp();
		validLegalEntity.setLegalEntityAppName("legalEntity87");
		validLegalEntity.setLegalEntityAppId(4l);
		// userLegalEntityApp.setLegalEntityApp(validLegalEntity);
		userLegalEntities.add(userLegalEntityApp);

		// user.setLegalEntities(userLegalEntities);

		return user;
	}

	private User getUserOneLegalEntity() {
		User user = createValidUser();
		List<UserLegalEntityApp> userLegalEntities = new ArrayList<UserLegalEntityApp>();
		UserLegalEntityApp userLegalEntityApp = new UserLegalEntityApp();
		LegalEntityApp validLegalEntity = new LegalEntityApp();

		validLegalEntity.setLegalEntityAppName("legalEntity1");
		validLegalEntity.setLegalEntityAppId(1L);
		// userLegalEntityApp.setLegalEntityApp(validLegalEntity);
		userLegalEntities.add(userLegalEntityApp);

		// user.setLegalEntities(userLegalEntities);

		return user;
	}
}
