package com.mcmcg.ico.bluefin.security.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.RolePermission;
import com.mcmcg.ico.bluefin.persistent.Token;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserLoginHistory;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.TokenRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserLoginHistoryRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomForbiddenException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.security.TokenHandler;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenHandler.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration

public class SessionServiceTest {

    @Autowired
    @InjectMocks
    private SessionService sessionService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Mock
    private UserLoginHistoryRepository userLoginHistoryRepository;

    private TokenHandler tokenHandler;

    private final static String TOKEN = "eyJpZCI6MTIzNDUsInVzZXJuYW1lIjoib21vbmdlIiwiZXhwaXJlcyI6MTQ2NzAzMzAwMzg2NH0=.ZrRceEEB63+4jDcLIXVUSuuOkV82pqvdXcfFZkzG1DE=";
    private final static String NEW_TOKEN = "nEwTokenpZCI6MTIzNDUsInVzZXJuYW1lIjoib21vbmdlIiwiZXhwaXJlcyI6MTQ2NzAzMzAwMzg2NH0=.ZrRceEEB63+4jDcLIXVUSuuOkV82pqvdXcfFZkzG1DE=";

    @Before
    public void initMocks() {
        tokenHandler = PowerMockito.mock(TokenHandler.class);
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(sessionService, "securityTokenExpiration", 604800);
    }

    // Authenticate

    /**
     * Tests a successful call for a valid user and password
     */
    @Test
    public void testAuthenticateSuccess() {
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        Mockito.when(userLoginHistoryRepository.save(Mockito.any(UserLoginHistory.class)))
                .thenReturn(new UserLoginHistory());

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = sessionService
                .authenticate(user.getUsername(), "test");

        Assert.assertEquals("omonge", usernamePasswordAuthenticationToken.getName());
        Assert.assertEquals("test", usernamePasswordAuthenticationToken.getCredentials());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userLoginHistoryRepository, Mockito.times(1)).save(Mockito.any(UserLoginHistory.class));

        Mockito.verifyNoMoreInteractions(userLoginHistoryRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Tests the case for when the user does not exists
     */
    @Test(expected = CustomForbiddenException.class)
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
    @Test(expected = CustomForbiddenException.class)
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
        Mockito.when(tokenRepository.findByUserIdAndType(securityUser.getId(), "authentication")).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(null)).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(securityUser.getUsername())).thenReturn(createValidUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(TOKEN);

        AuthenticationResponse response = sessionService.generateToken(securityUser.getUsername());

        Assert.assertEquals(TOKEN, response.getToken());
        Assert.assertEquals("omonge", response.getUsername());
        Assert.assertEquals("Monge", response.getFirstName());
        Assert.assertEquals("Vega", response.getLastName());

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a new token verification for a user that have a active token, happy
     * path
     */
    @Test
    public void testGenerateTokenStillAliveSuccess() {
        SecurityUser securityUser = createValidSecurityUser();
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(securityUser.getUsername())).thenReturn(securityUser);
        Mockito.when(tokenRepository.findByUserIdAndType(securityUser.getId(), "authentication")).thenReturn(null);
        Mockito.when(userRepository.findByUsername(securityUser.getUsername())).thenReturn(createValidUser());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(NEW_TOKEN);

        AuthenticationResponse response = sessionService.generateToken("omonge");

        Assert.assertEquals(NEW_TOKEN, response.getToken());
        Assert.assertEquals("omonge", response.getUsername());
        Assert.assertEquals("Monge", response.getFirstName());
        Assert.assertEquals("Vega", response.getLastName());

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }
    
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
     * Test a error when finding UserId and Type, transaction exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGenerateTokenFindByUserNameTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());

        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test a error when finding UserId and Type, Data Access exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGenerateTokenFindByUserNameError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());

        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test a error when finding UserId and Type, JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGenerateTokenFindByUserNameJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());

        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test a error when finding UserId and Type, Transaction exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGenerateTokenNewDeleteTokenTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when finding UserId and Type, Data Access exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGenerateTokenNewDeleteTokenDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when finding UserId and Type, JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGenerateTokenNewDeleteTokenJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when saving Token, Transaction exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGenerateTokenNewSaveTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test a error when saving Token, Data Access exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGenerateTokenNewSaveDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test a error when saving Token, JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGenerateTokenNewSaveJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test a error when finding user by name, Transaction exception
     */
    @Test(expected = CustomNotFoundException.class)
    public void testGenerateTokenFindUserNameTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when finding user by name, Data access exception
     */
    @Test(expected = CustomNotFoundException.class)
    public void testGenerateTokenFindUserNameDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when finding user by name, JDBC Connection exception
     */
    @Test(expected = CustomNotFoundException.class)
    public void testGenerateTokenFindUserNameJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when creating a new token, Transaction exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGenerateTokenCreateTokenTransactionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when creating a new token, Data Access exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGenerateTokenCreateTokenDataAccessError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test a error when creating a new token, JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGenerateTokenCreateTokenJDBCConnectionError() {
        Mockito.when(userDetailsServiceImpl.loadUserByUsername(Mockito.anyString()))
                .thenReturn(createValidSecurityUser());
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.generateToken("omonge");

        Mockito.verify(userDetailsServiceImpl, Mockito.times(1)).loadUserByUsername(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));

        Mockito.verifyNoMoreInteractions(userDetailsServiceImpl);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
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

    // getCurrentTokenIfValid (Long)

    /**
     * Test validation of the token given by parameter, if not found return null
     */
    @Test
    public void testGetCurrentTokenIfValidNewSuccess2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);

        String result = sessionService.getCurrentTokenIfValid(1234);

        Assert.assertEquals(null, result);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, if found and still valid
     */
    @Test
    public void testGetCurrentTokenIfValidTokenFoundAndValid2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(createValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(TOKEN);

        String result = sessionService.getCurrentTokenIfValid(1234);

        Assert.assertEquals(TOKEN, result);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, if found and invalid
     */
    @Test
    public void testGetCurrentTokenIfValidTokenFoundAndInvalid2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);

        String result = sessionService.getCurrentTokenIfValid(1234);

        Assert.assertEquals(null, result);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter but Transaction Exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetCurrentTokenIfValidTokenErrorTransaction2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.getCurrentTokenIfValid(1234);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter but Data Access Exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGetCurrentTokenIfValidTokenErrorDataAccess2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.getCurrentTokenIfValid(1234);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter but JDBC Connection
     * Exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGetCurrentTokenIfValidTokenErrorJDBCConnection2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.getCurrentTokenIfValid(1234);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, found and invalid but
     * Transaction Exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetCurrentTokenIfValidTokenFoundAndInvalidErrorTransaction2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.getCurrentTokenIfValid(1234);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, found and invalid but
     * Data Access Exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGetCurrentTokenIfValidTokenFoundAndInvalidErrorDataAccess2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.getCurrentTokenIfValid(1234);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, found and invalid but
     * JDBC Connection Exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGetCurrentTokenIfValidTokenFoundAndInvalidErrorJDBCConnection2() {
        Mockito.when(tokenRepository.findByUserIdAndType(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.getCurrentTokenIfValid(1234);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByUserIdAndType(Mockito.anyLong(), Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    // getCurrentTokenIfValid (String)

    /**
     * Test validation of the token given by parameter, if not found return null
     */
    @Test
    public void testGetCurrentTokenIfValidNewSuccess() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(null);
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);

        String result = sessionService.getCurrentTokenIfValid(TOKEN);

        Assert.assertEquals(null, result);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, if found and still valid
     */
    @Test
    public void testGetCurrentTokenIfValidTokenFoundAndValid() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(createValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(TOKEN);

        String result = sessionService.getCurrentTokenIfValid(TOKEN);

        Assert.assertEquals(TOKEN, result);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, if found and invalid
     */
    @Test
    public void testGetCurrentTokenIfValidTokenFoundAndInvalid() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class))).thenReturn(null);

        String result = sessionService.getCurrentTokenIfValid(TOKEN);

        Assert.assertEquals(null, result);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter but Transaction Exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetCurrentTokenIfValidTokenErrorTransaction() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.getCurrentTokenIfValid(TOKEN);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter but Data Access Exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGetCurrentTokenIfValidTokenErrorDataAccess() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.getCurrentTokenIfValid(TOKEN);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter but JDBC Connection
     * Exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGetCurrentTokenIfValidTokenErrorJDBCConnection() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.getCurrentTokenIfValid(TOKEN);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, found and invalid but
     * Transaction Exception
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testGetCurrentTokenIfValidTokenFoundAndInvalidErrorTransaction() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.getCurrentTokenIfValid(TOKEN);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, found and invalid but
     * Data Access Exception
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void testGetCurrentTokenIfValidTokenFoundAndInvalidErrorDataAccess() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.getCurrentTokenIfValid(TOKEN);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test validation of the token given by parameter, found and invalid but
     * JDBC Connection Exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testGetCurrentTokenIfValidTokenFoundAndInvalidErrorJDBCConnection() {
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(createInValidToken());
        Mockito.when(tokenHandler.validateToken(Mockito.any(Token.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.getCurrentTokenIfValid(TOKEN);

        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).validateToken(Mockito.any(Token.class));

        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Generate a new token for the user, success case
     */
    @Test
    public void generateNewTokenSuccess() {
        SecurityUser securityUser = createValidSecurityUser();
        Mockito.when(tokenHandler.createTokenForUser(securityUser)).thenReturn(TOKEN);

        Token token = sessionService.generateNewToken(securityUser);

        // Tests if the expiration date is 7 days from now
        Assert.assertTrue(isXDaysAfterToday(token.getExpire(), 7));
        Assert.assertEquals(securityUser.getId(), token.getUserId());
        Assert.assertEquals(TOKEN, token.getToken());
        Assert.assertEquals("authentication", token.getType());
    }

    /**
     * Generate a new token for the user, Token is null
     */
    @Test
    public void generateNewTokenNull() {
        SecurityUser securityUser = createValidSecurityUser();
        Mockito.when(tokenHandler.createTokenForUser(securityUser)).thenReturn(null);

        Token token = sessionService.generateNewToken(securityUser);

        // Tests if the expiration date is 7 days from now
        Assert.assertTrue(isXDaysAfterToday(token.getExpire(), 7));
        Assert.assertEquals(securityUser.getId(), token.getUserId());
        Assert.assertEquals(null, token.getToken());
        Assert.assertEquals("authentication", token.getType());
    }

    /**
     * Generate a new token for the user, SecurityUser is null, exception
     * NullPointerException thrown
     */
    @Test(expected = java.lang.NullPointerException.class)
    public void generateNewTokenSuccessNullUser() {

        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(TOKEN);
        sessionService.generateNewToken(null);
    }

    /**
     * Get login response by username
     * 
     * @throws Exception
     */
    @Test
    public void getLoginResponseSuccess() throws Exception {
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        AuthenticationResponse response = sessionService.getLoginResponse("omonge");

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

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }
    
    @Test(expected = java.lang.NullPointerException.class)
    public void getLoginResponseNoUserFound() throws Exception {
        User user = createValidUser();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        
        sessionService.getLoginResponse("omonge1");
        
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test if roles are null, NullPointerException is thrown
     * 
     * @throws Exception
     */
    @Test(expected = java.lang.NullPointerException.class)
    public void getLoginResponseNoRoles() throws Exception {
        User user = createValidUser();
        user.setRoles(null);
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        sessionService.getLoginResponse("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test roles as null, NullPointerException is thrown
     * 
     * @throws Exception
     */
    @Test(expected = java.lang.NullPointerException.class)
    public void getLoginResponseNoPermission() throws Exception {
        User user = createValidUser();
        user.getRoles().forEach(userRole -> userRole.setRole(null));
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);
        sessionService.getLoginResponse("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test as a user null attempting to access property, NullPointerException
     * is thrown
     * 
     * @throws Exception
     */
    @Test(expected = java.lang.NullPointerException.class)
    public void getLoginResponseNullUserName() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        sessionService.getLoginResponse(null);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Get login response by username but Transaction Exception
     * 
     * @throws Exception
     */

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void getLoginResponseErrorTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.getLoginResponse("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Get login response by username but Data Access Exception
     * 
     * @throws Exception
     */

    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void getLoginResponseErrorDataAccess() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.getLoginResponse("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Get login response by username but JDBC Connection Exception
     * 
     * @throws Exception
     */

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void getLoginResponseErrorJDBC() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.getLoginResponse("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the happy way of refreshing a given token
     */
    @Test
    public void refreshTokenSuccess() {

        User user = createValidUser();
        Mockito.when(tokenHandler.parseUserFromToken(TOKEN)).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
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

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test when the name of the user is null and not found
     */
    @Test(expected = CustomNotFoundException.class)
    public void refreshTokenUserNameNullUserNotFound() {

        User user = createValidUser();
        user.setUsername(null);
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the behavior for when the token given is null
     */
    @Test(expected = java.lang.NullPointerException.class)
    public void refreshTokenNullToken() {

        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(null);

        sessionService.refreshToken(null);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(0)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(0)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
    }

    /**
     * Test the behavior for when the token given does not exists
     */
    @Test(expected = java.lang.NullPointerException.class)
    public void refreshTokenNotFoundToken() {

        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(null);

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // Data base exceptions

    /**
     * Test Transaction exception for when finding token repository
     */
    @Test(expected = RuntimeException.class)
    public void refreshTokenRuntimeExceptionFindToken() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new RuntimeException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }
    /**
     * Test Transaction exception for when finding token repository
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void refreshTokenTransactionExceptionFindToken() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test Data Access exception for when finding token repository
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void refreshTokenDataAccessExceptionFindToken() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test Data Access exception for when finding token repository
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void refreshTokenJDBCExceptionFindToken() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(0)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }
    
    /**
     * Test Runtime Exception for saving a Token
     */
    @Test(expected = RuntimeException.class)
    public void refreshTokenRuntimeExceptionSave() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new RuntimeException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }
    /**
     * Test Transaction Exception for saving a Token
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void refreshTokenTransactionExceptionSave() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test Data Access Exception for saving a Token
     */
    @Test(expected = org.springframework.dao.DataAccessResourceFailureException.class)
    public void refreshTokenDataAccessExceptionSave() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test Data Access Exception for saving a Token
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void refreshTokenJDBCExceptionSave() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class)))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(0)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
    }

    /**
     * Test Transaction exception for when trying to find a user by name
     */
    @Test(expected = CustomNotFoundException.class)
    public void refreshTokenTransactionExceptionFindUserName() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test Data Access exception for when trying to find a user by name
     */
    @Test(expected = CustomNotFoundException.class)
    public void refreshTokenTransactionDataAccessFindUserName() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(""));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test JDBC Connection exception for when trying to find a user by name
     */
    @Test(expected = CustomNotFoundException.class)
    public void refreshTokenTransactionJDBCFindUserName() {
        Mockito.when(tokenHandler.parseUserFromToken(Mockito.anyString())).thenReturn(createValidSecurityUser());
        Mockito.when(tokenHandler.createTokenForUser(Mockito.any(SecurityUser.class))).thenReturn(NEW_TOKEN);
        Mockito.when(tokenRepository.findByToken(Mockito.anyString())).thenReturn(new Token());
        Mockito.when(tokenRepository.save(Mockito.any(Token.class))).thenReturn(new Token());
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        sessionService.refreshToken(TOKEN);

        Mockito.verify(tokenHandler, Mockito.times(1)).parseUserFromToken(Mockito.anyString());
        Mockito.verify(tokenHandler, Mockito.times(1)).createTokenForUser(Mockito.any(SecurityUser.class));
        Mockito.verify(tokenRepository, Mockito.times(1)).findByToken(Mockito.anyString());
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(tokenHandler);
        Mockito.verifyNoMoreInteractions(tokenRepository);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    private boolean isXDaysAfterToday(Date tokenExpiration, int days) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, days);

        return formatter.format(tokenExpiration).equals(formatter.format(c.getTime()));
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
        validLegalEntity.setLegalEntityAppId(4321);
        return validLegalEntity;
    }

    private List<RolePermission> createRolePermissions() {
        List<RolePermission> rolesPermissions = new ArrayList<RolePermission>();
        RolePermission rp = new RolePermission();
        rp.setCreatedDate(new Date());
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
        validRole.setRoleId(1234);
        validRole.setRolePermissions(createRolePermissions());
        return validRole;
    }

    private SecurityUser createValidSecurityUser() {
        SecurityUser securityUser = new SecurityUser();
        securityUser.setUsername("omonge");
        securityUser.setId(12345L);

        return securityUser;
    }

    private User createValidUser() {
        User user = new User();
        user.setUsername("omonge");
        user.setFirstName("Monge");
        user.setLastName("Vega");

        List<UserLegalEntity> userLegalEntities = new ArrayList<UserLegalEntity>();
        userLegalEntities.add(createValidUserLegalEntity());
        user.setLegalEntities(userLegalEntities);

        List<UserRole> userRoles = new ArrayList<UserRole>();
        userRoles.add(createValidUserRole());
        user.setRoles(userRoles);

        return user;
    }

    private Token createValidToken() {
        Token token = new Token();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 30);

        token.setToken(TOKEN);
        // setting expire to 30 days from now
        token.setExpire(c.getTime());
        token.setTokenId(123l);

        return token;
    }

    private Token createInValidToken() {
        Token token = new Token();
        token.setToken(TOKEN);
        // setting expire to now
        token.setExpire(new Date());
        token.setTokenId(123l);

        return token;
    }

}
