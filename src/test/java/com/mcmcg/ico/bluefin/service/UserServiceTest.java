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
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterUserOK() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        LegalEntityApp expectedLegalEntityApp = createValidLegalEntityApp();
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppName(Mockito.anyString()))
                .thenReturn(expectedLegalEntityApp);
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleName(Mockito.anyString())).thenReturn(expectedRole);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenReturn(new UserLegalEntity());
        Mockito.when(userRoleRepository.save(Mockito.any(UserRole.class))).thenReturn(new UserRole());

        UserResource result = userService.registerNewUserAccount(newUser);

        Assert.assertEquals("test@email.com", result.getEmail());
        Assert.assertEquals("test", result.getFirstName());
        Assert.assertEquals("user", result.getLastName());
        Assert.assertEquals("userTest", result.getUsername());

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
        Mockito.verify(legalEntityAppRepository, Mockito.times(1)).findByLegalEntityAppName(Mockito.anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).findByRoleName(Mockito.anyString());
        Mockito.verify(userLegalEntityRepository, Mockito.times(1)).save(Mockito.any(UserLegalEntity.class));
        Mockito.verify(userRoleRepository, Mockito.times(1)).save(Mockito.any(UserRole.class));
    }

    @Test
    public void testRegisterUserInvalidRolesBadRequest() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppName(Mockito.anyString()))
                .thenReturn(new LegalEntityApp());
        Mockito.when(roleRepository.findByRoleName(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following role doesn't exist: ROLE_TESTING");

        userService.registerNewUserAccount(newUser);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void testRegisterUserInvalidLegalEntitiesBadRequest() throws Exception {
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppName(Mockito.anyString())).thenReturn(null);
        expectedEx.expect(CustomBadRequestException.class);
        expectedEx.expectMessage("The following legal entity doesn't exist: legalEntity1");

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
        Mockito.when(legalEntityAppRepository.findByLegalEntityAppName(Mockito.anyString()))
                .thenReturn(expectedLegalEntityApp);
        Role expectedRole = createValidRole();
        Mockito.when(roleRepository.findByRoleName(Mockito.anyString())).thenReturn(expectedRole);
        Mockito.when(userLegalEntityRepository.save(Mockito.any(UserLegalEntity.class)))
                .thenReturn(new UserLegalEntity());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.registerNewUserAccount(createValidRegisterResource());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void testUpdateUserOK() throws Exception {
        UpdateUserResource user = createValidUpdateResource();
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(createValidUser());

        UserResource result = userService.updateUserAccount("userTest", user);

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

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserFindByUsernameCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.springframework.transaction.CannotCreateTransactionException(""));

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserFindByUsernameDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserFindByUsernameJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString()))
                .thenThrow(new org.hibernate.exception.JDBCConnectionException("", null));

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.springframework.transaction.CannotCreateTransactionException.class)
    public void testUpdateUserSaveCannotCreateTransaction() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new CannotCreateTransactionException(""));

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = DataAccessResourceFailureException.class)
    public void testUpdateUserSaveDataAccessResourceFailureException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenThrow(new DataAccessResourceFailureException(""));

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test(expected = org.hibernate.exception.JDBCConnectionException.class)
    public void testUpdateUserSaveJDBCConnectionException() throws Exception {
        Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(createValidUser());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenThrow(new JDBCConnectionException("", null));

        userService.updateUserAccount("userTest", createValidUpdateResource());

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.anyString());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

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
        List<String> entities = new ArrayList<String>();
        entities.add("legalEntity1");
        newUser.setLegalEntityApps(entities);
        List<String> roles = new ArrayList<String>();
        roles.add("ROLE_TESTING");
        newUser.setRoles(roles);
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
        user.setUserLegalEntities(userLegalEntities);

        List<UserRole> userRoles = new ArrayList<UserRole>();
        userRoles.add(createValidUserRole());
        user.setUserRoles(userRoles);
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
        validLegalEntity.setLegalEntityAppId(4321);
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
        validRole.setRoleId(1234);
        return validRole;
    }

}