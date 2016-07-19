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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;

import com.mcmcg.ico.bluefin.BluefinServicesApplication;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.util.TestUtilClass;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BluefinServicesApplication.class)
@WebAppConfiguration
public class LegalEntityAppServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private LegalEntityAppRepository legalEntityAppRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    @Autowired
    private LegalEntityAppService legalEntityAppService;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        // http://kim.saabye-pedersen.org/2012/12/mockito-and-spring-proxies.html
        // This issue is fixed in spring version 4.3.1, but spring boot
        // 3.6-RELEASE supports 4.2.7
        LegalEntityAppService leaService = (LegalEntityAppService) TestUtilClass.unwrapProxy(legalEntityAppService);

        ReflectionTestUtils.setField(leaService, "legalEntityAppRepository", legalEntityAppRepository);
        ReflectionTestUtils.setField(leaService, "userRepository", userRepository);
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
     * Test the case for when trying to get the user but a RuntimeException
     * shows up
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

    /**
     * Test when we send the correct data
     */
    @Test
    public void testGetLegalEntityAppsByIds() {
        List<LegalEntityApp> mockedLoadedLegalEntityApps = getValidLegalEntityAppList();
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollectionOf(Long.class)))
                .thenReturn(mockedLoadedLegalEntityApps);

        Set<Long> expectedLegalEntityAppIds = new HashSet<Long>(Arrays.asList(1L, 2L, 3L));
        List<LegalEntityApp> loadedLegalEntityApps = legalEntityAppService
                .getLegalEntityAppsByIds(expectedLegalEntityAppIds);

        Assert.assertEquals(expectedLegalEntityAppIds.size(), loadedLegalEntityApps.size());
        Assert.assertTrue(
                loadedLegalEntityApps.stream().filter(x -> !expectedLegalEntityAppIds.contains(x.getLegalEntityAppId()))
                        .collect(Collectors.toSet()).isEmpty());

        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    /**
     * Test when the system does not have roles
     */
    @Test(expected = CustomBadRequestException.class)
    public void testGetLegalEntityAppsByIdsEmptyList() {
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollectionOf(Long.class)))
                .thenReturn(new ArrayList<LegalEntityApp>());

        legalEntityAppService.getLegalEntityAppsByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L)));

        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
        Mockito.verifyNoMoreInteractions(legalEntityAppRepository);
    }

    /**
     * Test when we pass a wrong role ids
     */
    @Test
    public void testGetLegalEntityAppsByIdsOneWrongElement() {
        Mockito.when(legalEntityAppRepository.findAll(Mockito.anyCollectionOf(Long.class)))
                .thenReturn(getValidLegalEntityAppList());
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following legal entity apps don't exist.  List = [5, 7]");

        legalEntityAppService.getLegalEntityAppsByIds(new HashSet<Long>(Arrays.asList(1L, 2L, 3L, 5L, 7L)));

        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findAll(Mockito.anyCollectionOf(Long.class));
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

        Set<UserLegalEntity> userLegalEntities = new HashSet<UserLegalEntity>();
        userLegalEntities.add(createValidUserLegalEntity());
        user.setLegalEntities(userLegalEntities);

        Set<UserRole> userRoles = new HashSet<UserRole>();
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
        UserLegalEntity validUserLegalEntity = new UserLegalEntity();
        Set<UserLegalEntity> validUserLegalEntityList = new HashSet<UserLegalEntity>();

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
