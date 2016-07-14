package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.transaction.CannotCreateTransactionException;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserLegalEntityRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRoleRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class UserServiceTest {

    @InjectMocks
    @Autowired
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private LegalEntityAppRepository legalEntityAppRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserLegalEntityRepository userLegalEntityRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    Authentication auth;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        auth = new UsernamePasswordAuthenticationToken("omonge", "password", null);
    }

    // Get user info

    @Test
    public void testGetUserInformationSuccess() throws Exception { // 200
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
    public void testGetUserInfoNotFound() throws Exception {// 404

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
    public void testGetUsersRuntimeExceptionFindAll() throws Exception {
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
    public void testRegisterUserOK() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(expectedLegalEntityApp);
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(expectedRole);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenReturn(new UserLegalEntity());
        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class))).thenReturn(new UserRole());

        UserResource result = userService.registerNewUserAccount(newUser);

        Assert.assertEquals(newUser.getEmail(), result.getEmail());
        Assert.assertEquals(newUser.getFirstName(), result.getFirstName());
        Assert.assertEquals(newUser.getLastName(), result.getLastName());
        Assert.assertEquals(newUser.getUsername(), result.getUsername());

        Role actualRole = result.getRoles().get(0);
        Assert.assertEquals(expectedRole.getRoleId(), actualRole.getRoleId());
        Assert.assertEquals(expectedRole.getRoleName(), actualRole.getRoleName());
        Assert.assertEquals(expectedRole.getDescription(), actualRole.getDescription());

        LegalEntityApp actualLegalEntityApp = result.getLegalEntityApps().get(0);
        Assert.assertEquals(expectedLegalEntityApp.getLegalEntityAppId(), actualLegalEntityApp.getLegalEntityAppId());
        Assert.assertEquals(expectedLegalEntityApp.getLegalEntityAppName(),
                actualLegalEntityApp.getLegalEntityAppName());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(legalEntityAppRepository, Mockito.times(5)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(5)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userLegalEntityRepository, Mockito.times(5)).save(Mockito.any(UserLegalEntity.class));
        Mockito.verify(userRoleRepository, Mockito.times(5)).save(Mockito.any(UserRole.class));
    }

    @Test(expected = RuntimeException.class)
    public void testRegisterUserRuntimeExceptionFindUserName() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsFindUserName() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsFindByLegalEntity() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsFindByRoleName() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());

        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsSaveNewUser() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(createValidRole());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsSaveLegalEntity() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(createValidRole());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);

        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserDBFailsSaveRole() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(createValidRole());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenReturn(new UserLegalEntity());

        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));
        Mockito.verify(userRoleRepository, Mockito.times(1)).save(Mockito.any(UserRole.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsFindUserNameAccess() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsFindByLegalEntityAccess() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsFindByRoleNameAccess() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());

        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsSaveNewUserAccess() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(createValidRole());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testRegisterUserDBFailsSaveLegalEntityAccess() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(createValidLegalEntityApp());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(createValidRole());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);

        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
    }

    @Test
    public void testRegisterUserInvalidRolesBadRequest() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(new LegalEntityApp());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(null);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following role doesn't exist: 42");

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testRegisterUserInvalidLegalEntitiesBadRequest() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong())).thenReturn(null);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following legalEntity doesn't exist: 64");

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);

    }

    @Test
    public void testRegisterExistingUserBadRequest() throws Exception {
        User existingUser = new User();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(existingUser);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("Unable to create the account, this username already exists: userTest");

        userService.registerNewUserAccount(createValidRegisterResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testRegisterUserFail() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(expectedLegalEntityApp);
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(expectedRole);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenReturn(new UserLegalEntity());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(createValidRegisterResource());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    // Update user tests

    @Test
    public void testUpdateUserOK() throws Exception {
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
    public void testUpdateNotExistingUserBadRequest() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to update the account, this username doesn't exists: userTest");

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByUsernameCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByUsernameDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserFindByUsernameJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserSaveCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserSaveDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserSaveJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new JDBCConnectionException("", null));

        userService.updateUserProfile("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // Update user roles tests

    @Test
    public void testUpdateUserRolesOK() throws Exception {
        List<Long> roles = createValidRoleIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(expectedRole);

        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class))).thenReturn(new UserRole());

        UserResource result = userService.updateUserRoles("userTest", roles);

        Assert.assertEquals("test@email.com", result.getEmail());
        Assert.assertEquals("test", result.getFirstName());
        Assert.assertEquals("user", result.getLastName());
        Assert.assertEquals("userTest", result.getUsername());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(roleRepository, Mockito.times(5)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(userRoleRepository, Mockito.times(5)).save(Mockito.any(UserRole.class));
    }

    @Test
    public void testUpdateUserRolesInvalidRolesBadRequest() throws Exception {
        List<Long> roles = createValidRoleIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(null);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following role doesn't exist: 42");

        userService.updateUserRoles("userTest", roles);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testUpdateUserRolesNotExistingUserBadRequest() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to update roles, this username doesn't exists: userTest");

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserRolesFindByUsernameCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserRolesFindByUsernameDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserRolesFindByUsernameJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByRoleNameCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByRoleIdDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserRolesFindByRoleNameJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenThrow(new JDBCConnectionException("", null));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserRolesSaveCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(expectedRole);
        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRoleRepository, Mockito.times(1)).save(Mockito.any(UserRole.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserRolesSaveDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(expectedRole);
        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRoleRepository, Mockito.times(1)).save(Mockito.any(UserRole.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserRolesSaveJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleId(Mockito.anyLong())).thenReturn(expectedRole);
        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class)))
                .thenThrow(new JDBCConnectionException("", null));

        userService.updateUserRoles("userTest", createValidRoleIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRoleRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleId(Mockito.anyLong());
        Mockito.verify(userRoleRepository, Mockito.times(1)).save(Mockito.any(UserRole.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(roleRepository);
        Mockito.verifyNoMoreInteractions(userRoleRepository);
    }

    // Update user legalEntities tests

    @Test
    public void testUpdateUserLegalEntitiesOK() throws Exception {
        List<Long> legalEntities = createValidLegalEntityIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(expectedLegalEntityApp);

        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenReturn(new UserLegalEntity());

        UserResource result = userService.updateUserLegalEntities("userTest", legalEntities);

        Assert.assertEquals("test@email.com", result.getEmail());
        Assert.assertEquals("test", result.getFirstName());
        Assert.assertEquals("user", result.getLastName());
        Assert.assertEquals("userTest", result.getUsername());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(5)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(userLegalEntityRepository, Mockito.times(5)).save(Mockito.any(UserLegalEntity.class));
    }

    @Test
    public void testUpdateUserLegalEntitiesInvalidLegalEntitiesBadRequest() throws Exception {
        List<Long> legalEntities = createValidLegalEntityIdsList();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong())).thenReturn(null);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following legalEntity doesn't exist: 64");

        userService.updateUserLegalEntities("userTest", legalEntities);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testUpdateUserLegalEntitiesNotExistingUserBadRequest() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomNotFoundException.class);
        expectedEx.expectMessage("Unable to update legalEntities, this username doesn't exists: userTest");

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserLegalEntitiesFindByUsernameCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserLegalEntitiesFindByUsernameDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserLegalEntitiesFindByUsernameJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByLegalEntityAppIdCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByLegalEntityAppNameDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserLegalEntitiesFindByLegalEntityAppNameJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenThrow(new JDBCConnectionException("", null));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserLegalEntitiesSaveCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(expectedLegalEntityApp);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserLegalEntitiesSaveDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(expectedLegalEntityApp);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserLegalEntitiesSaveJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppId(Mockito.anyLong()))
                .thenReturn(expectedLegalEntityApp);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenThrow(new JDBCConnectionException("", null));

        userService.updateUserLegalEntities("userTest", createValidLegalEntityIdsList());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).deleteInBatch(Mockito.anyCollection());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppId(Mockito.anyLong());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
        Mockito.verifyNoMoreInteractions(userLegalEntityRepository);
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
    public void testUpdateUserLegalEntitiesAllowedByLegalEntityMe() throws Exception {

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
    public void testUpdateUserLegalEntitiesAllowedByLegalEntity() throws Exception {
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
    public void testUpdateUserLegalEntitiesNotAllowedByLegalEntity() throws Exception {
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
    public void testUpdateUserLegalEntitiesRuntimeExceptionFindUser1() throws Exception {
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
    public void testUpdateUserLegalEntitiesRuntimeExceptionFindUser2() throws Exception {
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
        List<UserLegalEntity> validUserLegalEntityList = new ArrayList<UserLegalEntity>();
        validUserLegalEntityList.add(validUserLegalEntity);
        validLegalEntity.setUserLegalEntities(validUserLegalEntityList);
        validLegalEntity.setLegalEntityAppName("legalEntity1");
        validLegalEntity.setLegalEntityAppId(4321L);
        return validLegalEntity;
    }

    private Role createValidRole() {
        Role validRole = new Role();
        UserRole validUserRole = new UserRole();
        List<UserRole> validUserRoleList = new ArrayList<UserRole>();
        validUserRoleList.add(validUserRole);
        validRole.setUserRoles(validUserRoleList);
        validRole.setRoleName("ROLE_TESTING");
        validRole.setDescription("role description");
        validRole.setRoleId(1234L);
        return validRole;
    }

    private List<Long> createValidRoleIdsList() {
        List<Long> roles = new ArrayList<Long>();
        roles.add(42L);
        roles.add(33L);
        roles.add(52L);
        roles.add(16L);
        roles.add(89L);
        return roles;
    }

    private List<Long> createValidLegalEntityIdsList() {
        List<Long> roles = new ArrayList<Long>();
        roles.add(64L);
        roles.add(77L);
        roles.add(27L);
        roles.add(87L);
        roles.add(64L);
        return roles;
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
