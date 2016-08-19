package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.RolePermission;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserLoginHistory;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.UserLoginHistoryRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenUtils.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration

public class SessionServiceTest {

    @Autowired
    @InjectMocks
    private SessionService sessionService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Mock
    private UserLoginHistoryRepository userLoginHistoryRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    private TokenUtils tokenUtils;

    private final static String TOKEN = "eyJpZCI6MTIzNDUsInVzZXJuYW1lIjoib21vbmdlIiwiZXhwaXJlcyI6MTQ2NzAzMzAwMzg2NH0=.ZrRceEEB63+4jDcLIXVUSuuOkV82pqvdXcfFZkzG1DE=";
    private final static String NEW_TOKEN = "nEwTokenpZCI6MTIzNDUsInVzZXJuYW1lIjoib21vbmdlIiwiZXhwaXJlcyI6MTQ2NzAzMzAwMzg2NH0=.ZrRceEEB63+4jDcLIXVUSuuOkV82pqvdXcfFZkzG1DE=";

    @Before
    public void initMocks() {
        tokenUtils = PowerMockito.mock(TokenUtils.class);
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(sessionService, "securityTokenExpiration", 604800);
    }

    // Authenticate

    /**
     * Tests a successful call for a valid user and password
     */
//    @Test
    public void testAuthenticateSuccess() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userLoginHistoryRepository.save(Mockito.any(UserLoginHistory.class)))
        .thenReturn(new UserLoginHistory());
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = sessionService.authenticate("nquiros",
                "pass123");

        Assert.assertEquals("nquiros", usernamePasswordAuthenticationToken.getName());
        Assert.assertEquals("pass123", usernamePasswordAuthenticationToken.getCredentials());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(1)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLoginHistoryRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when the user does not exists
     */
    @Test(expected = AccessDeniedException.class)
    public void testAuthenticateNotUserFound() {
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        sessionService.authenticate("omonge123", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(0)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when the user is null
     */
    @Test(expected = AccessDeniedException.class)
    public void testAuthenticateUserNull() {
        Mockito.when(userRepository.findByUsername(null)).thenReturn(null);

        sessionService.authenticate(null, "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(0)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = RuntimeException.class)
    public void testAuthenticateRuntimeExceptionFindUser() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(0)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when there is a Runtime Exception when finding a user
     * by username
     */
    @Test(expected = RuntimeException.class)
    public void testAuthenticateTransactionErrorFindUser() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(0)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when there is a DB error Transaction Exception when
     * trying to save the login history for a particular user
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testAuthenticateTransactionErrorLoginHistory() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(new User());
        Mockito.when(userLoginHistoryRepository.save(Mockito.any(UserLoginHistory.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(1)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLoginHistoryRepository);
    }

    /**
     * Tests the case for when there is a DB error Data Access Exception when
     * trying to find a user by user name
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testAuthenticateDataAccessErrorFindUser() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(0)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when there is a DB error Data Access Exception when
     * trying to save login history for a particular user
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testAuthenticateDataAccessErrorLoginHistory() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(new User());
        Mockito.when(userLoginHistoryRepository.save(Mockito.any(UserLoginHistory.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(1)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLoginHistoryRepository);
    }

    /**
     * Tests the case for when there is a DB error JDBC Connection Exception
     * when trying to find a user by user name
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testAuthenticateJDBCConnectionErrorFindUser() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(0)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when there is a DB error JDBC Connection Exception
     * when trying to save login history for a particular user
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testAuthenticateJDBCConnectionErrorLoginHistory() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(new User());
        Mockito.when(userLoginHistoryRepository.save(Mockito.any(UserLoginHistory.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.authenticate("omonge", "test");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(1)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(userLoginHistoryRepository);
    }

    // GenerateToken

    /**
     * Test a new token creation for a user that does not have a active token,
     * happy path
     */
    @Test
    public void testGenerateTokenNewSuccess() {
        SecurityUser securityUser = createValidSecurityUser();
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(securityUser.getUsername())).thenReturn(securityUser);
        Mockito.when(userRepository.findByUsername(securityUser.getUsername())).thenReturn(createValidUser());
        Mockito.when(tokenUtils.generateToken(Mockito.any(SecurityUser.class))).thenReturn(TOKEN);

        AuthenticationResponse response = sessionService.generateToken(securityUser.getUsername());

        Assert.assertEquals(TOKEN, response.getToken());
        Assert.assertEquals("omonge", response.getUsername());
        Assert.assertEquals("Monge", response.getFirstName());
        Assert.assertEquals("Vega", response.getLastName());

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(1)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Test a new token verification for a user that have a active token, happy
     * path
     */

    // Testing errors Transaction, Data Access and Data Base exceptions

    /**
     * Test a error when loading user by name, transaction exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGenerateTokenLoadUserTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
    }

    /**
     * Test a error when loading user by name, Data Access exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGenerateTokenLoadUserDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
    }

    /**
     * Test a error when loading user by name, JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGenerateTokenLoadUserJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
    }

    /**
     * Test a error when finding user by name, Transaction exception
     */
    @Test(expected = CustomNotFoundException.class)
    public void testGenerateTokenFindUserNameTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(0)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Test a error when finding user by name, Data access exception
     */
    @Test(expected = CustomNotFoundException.class)
    public void testGenerateTokenFindUserNameDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(0)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Test a error when finding user by name, JDBC Connection exception
     */
    @Test(expected = CustomNotFoundException.class)
    public void testGenerateTokenFindUserNameJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(0)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Test a error when creating a new token, Transaction exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGenerateTokenCreateTokenTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenUtils.generateToken(Mockito.any(SecurityUser.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(1)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Test a error when creating a new token, Data Access exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGenerateTokenCreateTokenDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(tokenUtils.generateToken(Mockito.any(SecurityUser.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(1)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Test a error when creating a new token, JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGenerateTokenCreateTokenJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(tokenUtils.generateToken(Mockito.any(SecurityUser.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(1)).generateToken(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenUtils);
    }

    /**
     * Tests the case for when a user name does not exists in the table user a
     * UsernameNotFoundException will be triggered.
     */
    @Test(expected = RuntimeException.class)
    public void testGenerateTokenUserNotFound() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername("omonge2")).thenThrow(new RuntimeException(""));

        sessionService.generateToken("omonge2");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
    }

    /**
     * Generate a new token for the user, success case
     */
    @Test
    public void generateNewTokenSuccess() {
        SecurityUser securityUser = createValidSecurityUser();
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenUtils.generateToken(securityUser)).thenReturn(TOKEN);
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);

        AuthenticationResponse response = sessionService.generateToken("omonge");

        Assert.assertEquals(response.getFirstName(), "Monge");
        Assert.assertEquals(response.getLastName(), "Vega");
        Assert.assertEquals(response.getUsername(), "omonge");
        Assert.assertNotNull(response.getToken());
    }

    /**
     * Get login response by username
     * 
     * @throws Exception
     */
//    @Test
//    public void getLoginResponseSuccess() throws Exception {
//        User user = createValidUser();
//        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
//        AuthenticationResponse response = sessionService.getLoginResponse("omonge");
//
//        Assert.assertEquals(user.getFirstName(), response.getFirstName());
//        Assert.assertEquals(user.getLastName(), response.getLastName());
//        Assert.assertEquals(user.getUsername(), response.getUsername());
//        List<Permission> permissionsResult = new ArrayList<Permission>();
//        for (UserRole role : user.getRoles()) {
//            for (RolePermission permission : role.getRole().getRolePermissions()) {
//                permissionsResult.add(permission.getPermission());
//            }
//        }
//        Assert.assertTrue(permissionsResult.equals(response.getPermissions()));
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

//    @Test(expected = java.lang.NullPointerException.class)
//    public void getLoginResponseNoUserFound() throws Exception {
//        User user = createValidUser();
//        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
//
//        sessionService.getLoginResponse("omonge1");
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Test if roles are null, NullPointerException is thrown
     * 
     * @throws Exception
     */
//    @Test(expected = java.lang.NullPointerException.class)
//    public void getLoginResponseNoRoles() throws Exception {
//        User user = createValidUser();
//        user.setRoles(null);
//        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
//        sessionService.getLoginResponse("omonge");
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Test roles as null, NullPointerException is thrown
     * 
     * @throws Exception
     */
//    @Test(expected = java.lang.NullPointerException.class)
//    public void getLoginResponseNoPermission() throws Exception {
//        User user = createValidUser();
//        user.getRoles().forEach(userRole -> userRole.setRole(null));
//        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
//        sessionService.getLoginResponse("omonge");
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Test as a user null attempting to access property, NullPointerException
     * is thrown
     * 
     * @throws Exception
     */
//    @Test(expected = java.lang.NullPointerException.class)
//    public void getLoginResponseNullUserName() throws Exception {
//        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
//        sessionService.getLoginResponse(null);
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Get login response by username but Transaction Exception
     * 
     * @throws Exception
     */

//    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
//    public void getLoginResponseErrorTransaction() throws Exception {
//        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
//                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));
//
//        sessionService.getLoginResponse("omonge");
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Get login response by username but Data Access Exception
     * 
     * @throws Exception
     */

//    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
//    public void getLoginResponseErrorDataAccess() throws Exception {
//        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
//                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));
//
//        sessionService.getLoginResponse("omonge");
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Get login response by username but JDBC Connection Exception
     * 
     * @throws Exception
     */

//    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
//    public void getLoginResponseErrorJDBC() throws Exception {
//        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
//                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));
//
//        sessionService.getLoginResponse("omonge");
//
//        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(userRepository);
//    }

    /**
     * Test the happy way of refreshing a given token
     */
    @Test
    public void refreshTokenSuccess() {

        User user = createValidUser();
        Mockito.when(tokenUtils.getUsernameFromToken(Mockito.anyString())).thenReturn("omonge");
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenUtils.generateToken(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);

        AuthenticationResponse response = sessionService.refreshToken(TOKEN);

        // asserts
        Assert.assertEquals(user.getFirstName(), response.getFirstName());
        Assert.assertEquals(user.getLastName(), response.getLastName());
        Assert.assertEquals(user.getUsername(), response.getUsername());
        List<Permission> permissionsResult = new ArrayList<Permission>();
        for (UserRole role : user.getRoles()) {
            for (RolePermission permission : role.getRole().getRolePermissions()) {
                permissionsResult.add(permission.getPermission());
            }
        }
        Assert.assertTrue(permissionsResult.equals(response.getPermissions()));
        Assert.assertFalse(response.getToken().equals(TOKEN));
        Assert.assertTrue(response.getToken().equals(NEW_TOKEN));

        Mockito.verify(tokenUtils, Mockito.times(1)).getUsernameFromToken(Mockito.anyString());
        Mockito.verify(tokenUtils, Mockito.times(1)).generateToken(Mockito.any(SecurityUser.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        // Mockito.verifyNoMoreInteractions(tokenUtils);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test when the name of the user is null and not found
     */
    @Test(expected = CustomBadRequestException.class)
    public void refreshTokenUserNameNullUserNotFound() {

        User user = createValidUser();
        user.setUsername(null);
        Mockito.when(tokenUtils.generateToken(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenUtils, Mockito.times(1)).generateToken(Mockito.any(SecurityUser.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenUtils);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    private UserLegalEntity createValidUserLegalEntity() {
        UserLegalEntity userLegalEntity = new UserLegalEntity();
        userLegalEntity.setUserLegalEntityAppId(0L);
        userLegalEntity.setLegalEntityApp(createValidLegalEntityApp());
        return userLegalEntity;
    }

    private UserRole createValidUserRole() {
        UserRole userRole = new UserRole();
        userRole.setUserRoleId(0L);
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

    private List<RolePermission> createRolePermissions() {
        List<RolePermission> rolesPermissions = new ArrayList<RolePermission>();
        RolePermission rp = new RolePermission();
        rp.setCreatedDate(new DateTime());
        rp.setRolePermissionId(234l);
        Permission permission = new Permission();
        rp.setPermission(permission);
        rolesPermissions.add(rp);
        return rolesPermissions;
    }

    private Role createValidRole() {
        Role validRole = new Role();
        UserRole validUserRole = new UserRole();
        Role role = new Role();

        role.setRolePermissions(createRolePermissions());

        validUserRole.setRole(role);
        List<UserRole> validUserRoleList = new ArrayList<UserRole>();

        validUserRoleList.add(validUserRole);
        validRole.setUserRoles(validUserRoleList);
        validRole.setRoleName("ROLE_TESTING");
        validRole.setDescription("role description");
        validRole.setRoleId(1234L);
        validRole.setRolePermissions(createRolePermissions());
        return validRole;
    }

    private SecurityUser createValidSecurityUser() {
        SecurityUser securityUser = new SecurityUser();

        User user = new User();
        user.setUsername("omonge");
        user.setFirstName("Monge");
        user.setLastName("Vega");
        user.setUserId(12345L);

        securityUser.setUser(user);

        return securityUser;
    }

    private User createValidUser() {
        User user = new User();
        user.setUsername("omonge");
        user.setFirstName("Monge");
        user.setLastName("Vega");
        user.setUserPassword("$2a$10$WMUix294mCns20D7H.JBxeb642bVWqm5JHz6cCcQnl4Et7SvGWGSG");

        List<UserLegalEntity> userLegalEntities = new ArrayList<UserLegalEntity>();
        userLegalEntities.add(createValidUserLegalEntity());
        user.setLegalEntities(userLegalEntities);

        List<UserRole> userRoles = new ArrayList<UserRole>();
        userRoles.add(createValidUserRole());
        user.setRoles(userRoles);

        return user;
    }
}
