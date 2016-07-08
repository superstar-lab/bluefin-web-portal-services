package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.exception.JDBCConnectionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class LegalEntityAppServiceTest {

    @InjectMocks
    @Autowired
    private LegalEntityAppService legalEntityAppService;

    @Mock
    private LegalEntityAppRepository legalEntityAppRepository;

    @Mock
    private UserRepository userRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
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
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findAll(listOfIds)).thenReturn(legalEntityAppList);

        List<LegalEntityApp> result = legalEntityAppService.getLegalEntities("omonge");

        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(result, legalEntityAppList);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(listOfIds);

        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);

    }

    /**
     * If the user is not found a empty list should be returned
     */
    @Test
    public void testFindAllUserNameNull() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

        List<LegalEntityApp> result = legalEntityAppService.getLegalEntities("omonge");

        Assert.assertTrue(result.isEmpty());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    // DB Exceptions
    /**
     * Test the case for when trying to get the user
     * but a RuntimeException shows up
     */
    @Test(expected = RuntimeException.class)
    public void testFindByUserGeneralExceptionFindByUser() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenThrow(new RuntimeException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but a RuntimeException shows up
     */
    @Test(expected = RuntimeException.class)
    public void testFindByUserGeneralExceptionEntities() {
        List<Long> listOfIds = Arrays.asList(1L);
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findAll(listOfIds)).thenThrow(new RuntimeException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(listOfIds);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }
    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is down
     */
    @SuppressWarnings("unchecked")
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testFindByUserGeneralExceptionLegalEntity() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollection()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollection());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is down
     */
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testFindByUserTransactionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is throws a Failure exception
     */
    @Test(expected = DataAccessResourceFailureException.class)
    public void testFindByUserDataAccessException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is throws a JDBC Connection exception
     */
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testFindByUserJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new JDBCConnectionException("", null));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is down
     */
    @SuppressWarnings("unchecked")
    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testLegalEntityTransactionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollection()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollection());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is throws a Failure exception
     */
    @SuppressWarnings("unchecked")
    @Test(expected = DataAccessResourceFailureException.class)
    public void testLegalEntityDataAccessException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollection()))
                .thenThrow(new DataAccessResourceFailureException(""));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollection());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    /**
     * Test the case for when trying to get the legal entities method is called
     * but the DB is throws a JDBC Connection exception
     */
    @SuppressWarnings("unchecked")
    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testLegalEntityJDBCConnectionException() {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollection()))
                .thenThrow(new JDBCConnectionException("", null));

        legalEntityAppService.getLegalEntities("omonge");

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollection());
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
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
        validLegalEntity.setLegalEntityAppName("MCM-R2K");
        validLegalEntity.setLegalEntityAppId(1L);
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

}
