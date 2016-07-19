package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.exception.JDBCConnectionException;
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
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;
import com.mcmcg.ico.bluefin.util.TestUtilClass;
import com.mysema.query.types.expr.BooleanExpression;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
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

        ReflectionTestUtils.setField(uService, "userRepository", userRepository);
        ReflectionTestUtils.setField(uService, "roleService", roleService);
        ReflectionTestUtils.setField(uService, "legalEntityAppService", legalEntityAppService);
        ReflectionTestUtils.setField(uService, "passwordEncoder", passwordEncoder);

        auth = new UsernamePasswordAuthenticationToken("omonge", "password", null);
    }

    // Get user info

    @Test
    public void testGetUserInformationSuccess() { // 200
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername("userTest")).thenReturn(user);

        UserResource userResource = userService.getUserInfomation("userTest");

        Assert.assertEquals(user.getEmail(), userResource.getEmail());
        Assert.assertEquals(user.getFirstName(), userResource.getFirstName());
        Assert.assertEquals(user.getLastName(), userResource.getLastName());
        Assert.assertEquals(user.getUsername(), userResource.getUsername());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = CustomNotFoundException.class)
    public void testGetUserInfoNotFound() {// 404

        Mockito.when(userRepository.findByUsername("omonge")).thenReturn(new User());

        userService.getUserInfomation("mytest");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = RuntimeException.class)
    public void testFindByUsernameRuntimeException() { // 500

        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

        userService.getUserInfomation("mytest");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);

    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testFindByUsername() { // 500

        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.getUserInfomation("mytest");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);

    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testFindByUsernameDBAccessFail() {// 500

        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(null));

        userService.getUserInfomation("mytest");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);

    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testFindByUsernameDBConnectionFail() {// 500

        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.getUserInfomation("mytest");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);

    }

    // Get Legal Entities by user name

    /**
     * Test success path for Legal Entities by user name
     */
    @Test
    public void testGetLegalEntitiesByUser() {
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);

        List<LegalEntityApp> result = userService.getLegalEntitiesByUser("omonge");

        Assert.assertEquals(user.getLegalEntityApps(), result);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test success path for Legal Entities by user name, empty list is return
     * if user not found
     */
    @Test
    public void testGetLegalEntitiesByUserNotFound() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        List<LegalEntityApp> result = userService.getLegalEntitiesByUser("omonge");

        Assert.assertTrue(result.isEmpty());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test runtime exception when trying to get information
     */
    @Test
    public void testGetLegalEntitiesByUserRunTimeException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException());
        expectedEx.expect(RuntimeException.class);

        userService.getLegalEntitiesByUser("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
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
        Mockito.when(userRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(list);

        Iterable<User> result = userService
                .getUsers(QueryDSLUtil.createExpression("legalEntities:[64,77,27,87]", User.class), 1, 1, null);

        for (User resultUser : result) {
            Assert.assertEquals("test@email.com", resultUser.getEmail());
            Assert.assertEquals("test", resultUser.getFirstName());
            Assert.assertEquals("user", resultUser.getLastName());
            Assert.assertEquals("userTest", resultUser.getUsername());
            Assert.assertEquals(searchUser.getLegalEntityApps(), resultUser.getLegalEntityApps());
            Assert.assertEquals(searchUser.getRoleNames(), resultUser.getRoleNames());

        }
        Mockito.verify(userRepository, Mockito.times(1)).findAll(Mockito.any(BooleanExpression.class),
                Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case where no information was found with the criteria used in
     * the search parameter
     */
    @Test
    public void testGetUsersNotFound() {// 404
        Page<User> list = new PageImpl<User>(new ArrayList<User>());

        Mockito.when(userRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(list);

        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to find the page requested");

        userService.getUsers(QueryDSLUtil.createExpression("legalEntities:[64,77,27,87]", User.class), 2, 1, null);

        Mockito.verify(userRepository, Mockito.times(1)).findAll(Mockito.any(BooleanExpression.class),
                Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case where a RuntimeException rises for when get all the list of
     * users according with the criteria given
     */
    @Test
    public void testGetUsersRuntimeExceptionFindAll() {
        Mockito.when(userRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenThrow(new RuntimeException());

        expectedEx.expect(RuntimeException.class);

        userService.getUsers(QueryDSLUtil.createExpression("legalEntities:[64,77,27,87]", User.class), 1, 1, null);

        Mockito.verify(userRepository, Mockito.times(1)).findAll(Mockito.any(BooleanExpression.class),
                Mockito.any(PageRequest.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // Register user tests

    @Test
    public void testRegisterUserOK() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(createValidUser());
        List<LegalEntityApp> expectedLegalEntityApps = new ArrayList<LegalEntityApp>();
        expectedLegalEntityApps.add(createValidLegalEntityApp());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(expectedLegalEntityApps);
        List<Role> expectedRoles = new ArrayList<Role>();
        expectedRoles.add(createValidRole());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(expectedRoles);

        UserResource result = userService.registerNewUserAccount(newUser);

        Assert.assertEquals(newUser.getEmail(), result.getEmail());
        Assert.assertEquals(newUser.getFirstName(), result.getFirstName());
        Assert.assertEquals(newUser.getLastName(), result.getLastName());
        Assert.assertEquals(newUser.getUsername(), result.getUsername());

        Optional<Role> actualRole = result.getRoles().stream().findFirst();
        Assert.assertTrue(actualRole.isPresent());
        Assert.assertEquals(expectedRoles.get(0).getRoleId(), actualRole.get().getRoleId());
        Assert.assertEquals(expectedRoles.get(0).getRoleName(), actualRole.get().getRoleName());
        Assert.assertEquals(expectedRoles.get(0).getDescription(), actualRole.get().getDescription());

        Optional<LegalEntityApp> actualLegalEntityApp = result.getLegalEntityApps().stream().findFirst();
        Assert.assertTrue(actualLegalEntityApp.isPresent());
        Assert.assertEquals(expectedLegalEntityApps.get(0).getLegalEntityAppId(),
                actualLegalEntityApp.get().getLegalEntityAppId());
        Assert.assertEquals(expectedLegalEntityApps.get(0).getLegalEntityAppName(),
                actualLegalEntityApp.get().getLegalEntityAppName());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
    }

    @Test(expected = RuntimeException.class)
    public void testRegisterUserRuntimeExceptionFindUserName() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsFindUserName() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsFindByRoleId() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsFindByRoleName() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());

        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsSaveNewUser() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsSaveLegalEntity() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsSaveRole() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsFindUserNameAccess() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsFindByRole() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsFindByRoleNameAccess() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());

        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsSaveNewUserAccess() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsSaveLegalEntityAccess() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException.class)
    public void testRegisterUserInvalidRolesBadRequest() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new CustomBadRequestException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException.class)
    public void testRegisterUserInvalidLegalEntitiesBadRequest() {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new CustomBadRequestException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testRegisterExistingUserBadRequest() {
        User existingUser = new User();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(existingUser);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("Unable to create the account, this username already exists: userTest");

        userService.registerNewUserAccount(createValidRegisterResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserFail() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(createValidRegisterResource());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    // Update user tests

    @Test
    public void testUpdateUserOK() {
        UpdateUserResource user = createValidUpdateResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(createValidUser());

        UserResource result = userService.updateUserProfile("userTest", user);

        Assert.assertEquals("test@email.com", result.getEmail());
        Assert.assertEquals("test", result.getFirstName());
        Assert.assertEquals("user", result.getLastName());
        Assert.assertEquals("userTest", result.getUsername());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void testUpdateNotExistingUserBadRequest() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to update the account, this username doesn't exists: userTest");

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByUsernameCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByUsernameDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserFindByUsernameJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserSaveCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserSaveDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserSaveJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new JDBCConnectionException("", null));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // Update user roles tests

    @Test
    public void testUpdateUserRolesOK() {
        Set<Long> roles = createValidRoleIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(createValidUser());

        User result = userService.updateUserRoles("userTest", roles);

        Assert.assertEquals("test@email.com", result.getEmail());
        Assert.assertEquals("test", result.getFirstName());
        Assert.assertEquals("user", result.getLastName());
        Assert.assertEquals("userTest", result.getUsername());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verify(userRepository, Mockito.times(2)).save(Mockito.any(User.class));
    }

    @Test(expected = com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException.class)
    public void testUpdateUserRolesInvalidRolesBadRequest() {
        Set<Long> roles = createValidRoleIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new CustomBadRequestException(""));

        userService.updateUserRoles("userTest", roles);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testUpdateUserRolesNotExistingUserBadRequest() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to update roles, this username doesn't exists: userTest");

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserRolesFindByUsernameCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserRolesFindByUsernameDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserRolesFindByUsernameJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByRoleNameCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByRoleIdDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserRolesFindByRoleNameJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new JDBCConnectionException("", null));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserRolesSaveCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserRolesSaveDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserRolesSaveJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleService.getRolesByIds(Mockito.anySetOf(Long.class))).thenReturn(createValidRoleList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new JDBCConnectionException("", null));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleService, Mockito.times(1)).getRolesByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleService);
    }

    // Update user legalEntities tests

    @Test
    public void testUpdateUserLegalEntitiesOK() {
        Set<Long> legalEntities = createValidLegalEntityIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(createValidUser());

        User result = userService.updateUserLegalEntities("userTest", legalEntities);

        Assert.assertEquals("test@email.com", result.getEmail());
        Assert.assertEquals("test", result.getFirstName());
        Assert.assertEquals("user", result.getLastName());
        Assert.assertEquals("userTest", result.getUsername());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
    }

    @Test(expected = CustomBadRequestException.class)
    public void testUpdateUserLegalEntitiesInvalidLegalEntitiesBadRequest() {
        Set<Long> legalEntities = createValidLegalEntityIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new CustomBadRequestException(""));

        userService.updateUserLegalEntities("userTest", legalEntities);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void testUpdateUserLegalEntitiesNotExistingUserBadRequest() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to update legalEntities, this username doesn't exists: userTest");

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserLegalEntitiesFindByUsernameCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserLegalEntitiesFindByUsernameDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserLegalEntitiesFindByUsernameJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByLegalEntityAppIdCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByLegalEntityAppNameDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserLegalEntitiesFindByLegalEntityAppNameJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenThrow(new JDBCConnectionException("", null));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserLegalEntitiesSaveCannotCreateTransaction() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserLegalEntitiesSaveDataAccessResourceFailureException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserLegalEntitiesSaveJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppService.getLegalEntityAppsByIds(Mockito.anySetOf(Long.class)))
                .thenReturn(createValidLegalEntityAppList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new JDBCConnectionException("", null));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntityAppsByIds(Mockito.anySetOf(Long.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    /**
     * Test the success case when the users exists and have a valid list of
     * legal entities
     */
    @Test
    public void testGetLegalEntitiesByUserNameSuccess() {
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());

        List<LegalEntityApp> result = userService.getLegalEntitiesByUser("omonge");

        Assert.assertEquals(user.getLegalEntityApps().get(0).getLegalEntityAppId(),
                result.get(0).getLegalEntityAppId());
        Assert.assertEquals("legalEntity1", result.get(0).getLegalEntityAppName());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case when the users does not exists, empty list will be returned
     */
    @Test
    public void testGetLegalEntitiesByUserNameNoUserFound() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        List<LegalEntityApp> result = userService.getLegalEntitiesByUser("omonge");

        Assert.assertTrue(result.isEmpty());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
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

        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
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

        Mockito.when(userRepository.findByUsername("omonge")).thenReturn(requestUser);
        Mockito.when(userRepository.findByUsername("nquiros")).thenReturn(userToUpdate);

        Boolean result = userService.belongsToSameLegalEntity(auth, "nquiros");

        Assert.assertTrue(result);

        Mockito.verify(userRepository, Mockito.times(2)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case when the user is not allowed to update a user because are
     * not related with some lega entity
     */
    @Test
    public void testUpdateUserLegalEntitiesNotAllowedByLegalEntity() {
        Mockito.when(userRepository.findByUsername("omonge")).thenReturn(getUserMoreLegalEntities());
        Mockito.when(userRepository.findByUsername("nquiros")).thenReturn(createValidUser());

        Boolean result = userService.belongsToSameLegalEntity(auth, "nquiros");

        Assert.assertFalse(result);

        Mockito.verify(userRepository, Mockito.times(2)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
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

        Mockito.when(userRepository.findByUsername("omonge")).thenThrow(new RuntimeException());
        Mockito.when(userRepository.findByUsername("nquiros")).thenReturn(userToUpdate);
        expectedEx.expect(RuntimeException.class);

        userService.belongsToSameLegalEntity(auth, "nquiros");

        Mockito.verify(userRepository, Mockito.times(2)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
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

        Mockito.when(userRepository.findByUsername("omonge")).thenReturn(userToUpdate);
        Mockito.when(userRepository.findByUsername("nquiros")).thenThrow(new RuntimeException());
        expectedEx.expect(RuntimeException.class);

        userService.belongsToSameLegalEntity(auth, "nquiros");

        Mockito.verify(userRepository, Mockito.times(2)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
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
        newUser.setPassword("password");
        return newUser;
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setFirstName("test");
        user.setLastName("user");
        user.setUsername("userTest");

        List<UserLegalEntity> userLegalEntities = new ArrayList<UserLegalEntity>();
        userLegalEntities.add(createValidUserLegalEntity());
        user.setLegalEntities(userLegalEntities);

        List<UserRole> userRoles = new ArrayList<UserRole>();
        userRoles.add(createValidUserRole());
        user.setRoles(userRoles);
        return user;
    }

    private UserLegalEntity createValidUserLegalEntity() {
        UserLegalEntity userLegalEntity = new UserLegalEntity();
        userLegalEntity.setUserLegalEntityAppId(0);
        userLegalEntity.setLegalEntityApp(createValidLegalEntityApp());
        return userLegalEntity;
    }

    private UserRole createValidUserRole() {
        UserRole userRole = new UserRole();
        userRole.setUserRoleId(0);
        userRole.setRole(createValidRole());
        return userRole;
    }

    private LegalEntityApp createValidLegalEntityApp() {
        LegalEntityApp validLegalEntity = new LegalEntityApp();
        UserLegalEntity validUserLegalEntity = new UserLegalEntity();
        Set<UserLegalEntity> validUserLegalEntityList = new HashSet<UserLegalEntity>();
        validUserLegalEntityList.add(validUserLegalEntity);
        validLegalEntity.setUserLegalEntities(validUserLegalEntityList);
        validLegalEntity.setLegalEntityAppName("legalEntity1");
        validLegalEntity.setLegalEntityAppId(4321L);
        return validLegalEntity;
    }

    private Role createValidRole() {
        UserRole validUserRole = new UserRole();
        List<UserRole> validUserRoleList = new ArrayList<UserRole>();
        validUserRoleList.add(validUserRole);

        Role validRole = new Role();
        validRole.setUserRoles(validUserRoleList);
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
        List<UserLegalEntity> userLegalEntities = new ArrayList<UserLegalEntity>();
        UserLegalEntity userLegalEntity = new UserLegalEntity();
        LegalEntityApp validLegalEntity = new LegalEntityApp();

        validLegalEntity.setLegalEntityAppName("legalEntity64");
        validLegalEntity.setLegalEntityAppId(1L);
        userLegalEntity.setLegalEntityApp(validLegalEntity);
        userLegalEntities.add(userLegalEntity);

        userLegalEntity = new UserLegalEntity();
        validLegalEntity = new LegalEntityApp();
        validLegalEntity.setLegalEntityAppName("legalEntity77");
        validLegalEntity.setLegalEntityAppId(2l);
        userLegalEntity.setLegalEntityApp(validLegalEntity);
        userLegalEntities.add(userLegalEntity);

        userLegalEntity = new UserLegalEntity();
        validLegalEntity = new LegalEntityApp();
        validLegalEntity.setLegalEntityAppName("legalEntity27");
        validLegalEntity.setLegalEntityAppId(3l);
        userLegalEntity.setLegalEntityApp(validLegalEntity);
        userLegalEntities.add(userLegalEntity);

        userLegalEntity = new UserLegalEntity();
        validLegalEntity = new LegalEntityApp();
        validLegalEntity.setLegalEntityAppName("legalEntity87");
        validLegalEntity.setLegalEntityAppId(4l);
        userLegalEntity.setLegalEntityApp(validLegalEntity);
        userLegalEntities.add(userLegalEntity);

        user.setLegalEntities(userLegalEntities);

        return user;
    }

    private User getUserOneLegalEntity() {
        User user = createValidUser();
        List<UserLegalEntity> userLegalEntities = new ArrayList<UserLegalEntity>();
        UserLegalEntity userLegalEntity = new UserLegalEntity();
        LegalEntityApp validLegalEntity = new LegalEntityApp();

        validLegalEntity.setLegalEntityAppName("legalEntity1");
        validLegalEntity.setLegalEntityAppId(1L);
        userLegalEntity.setLegalEntityApp(validLegalEntity);
        userLegalEntities.add(userLegalEntity);

        user.setLegalEntities(userLegalEntities);

        return user;
    }
}
