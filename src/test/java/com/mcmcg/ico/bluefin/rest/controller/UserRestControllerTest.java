package com.mcmcg.ico.bluefin.rest.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.service.UserService;

public class UserRestControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private UserRestController userControllerMock;

    @Mock
    private UserService userService;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(userControllerMock).addFilters().build();
    }

    // Register user tests

    @Test
    public void registerUserOK() throws Exception { // 200
        RegisterUserResource newUser = createValidRegisterResource();
        UserResource returnedUser = createValidUserResource();
        Mockito.when(userService.registerNewUserAccount(newUser)).thenReturn(returnedUser);

        mockMvc.perform(post("/api/rest/bluefin/users").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser)))

                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1)).registerNewUserAccount(newUser);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void registerUserBadRequestInvalidRequestBody() throws Exception { // 400
        RegisterUserResource newUser = createInvalidRegisterResource();
        UserResource returnedUser = createValidUserResource();
        Mockito.when(userService.registerNewUserAccount(newUser)).thenReturn(returnedUser);

        String validationErros = mockMvc
                .perform(post("/api/rest/bluefin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newUser)))
                .andExpect(status().isBadRequest()).andReturn().getResolvedException().getMessage();

        assertThat(validationErros, containsString("email must not be empty"));
        assertThat(validationErros, containsString("firstName must not be empty"));
        assertThat(validationErros, containsString("lastName must not be empty"));
        assertThat(validationErros, containsString("roleList must not be null"));
        assertThat(validationErros, containsString("legalEntityAppsList must not be null"));
        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(newUser);
    }

    @Test
    public void registerUserBadRequestInvalidLists() throws Exception { // 400
        RegisterUserResource newUser = createValidRegisterResource();
        newUser.setRoles(new ArrayList<String>());
        newUser.setLegalEntityApps(new ArrayList<String>());
        UserResource returnedUser = createValidUserResource();
        Mockito.when(userService.registerNewUserAccount(newUser)).thenReturn(returnedUser);

        String validationErros = mockMvc
                .perform(post("/api/rest/bluefin/users").contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newUser)))
                .andExpect(status().isBadRequest()).andReturn().getResolvedException().getMessage();

        assertThat(validationErros, containsString("roleList must not be empty"));
        assertThat(validationErros, containsString("legalEntityAppsList must not be empty"));
        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(newUser);
    }

    @Test
    public void registerUserUnauthorized() throws Exception { // 401
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userService.registerNewUserAccount(any(RegisterUserResource.class)))
                .thenThrow(new CustomUnauthorizedException(""));

        mockMvc.perform(post("/api/rest/bluefin/users").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).registerNewUserAccount(any(RegisterUserResource.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void registerUserInternalServerError() throws Exception { // 500
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userService.registerNewUserAccount(any(RegisterUserResource.class)))
                .thenThrow(new CustomException(""));

        mockMvc.perform(post("/api/rest/bluefin/users").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).registerNewUserAccount(any(RegisterUserResource.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    // Update user tests

    @Test
    public void updateUserOK() throws Exception { // 200
        UpdateUserResource user = createValidUpdateResource();
        UserResource updatedUser = createValidUserResource();
        Mockito.when(
                userService.havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(userService.updateUserProfile("test", user)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/rest/bluefin/users/test").header("X-Auth-Token", "tokenTest")
                .contentType(MediaType.APPLICATION_JSON).content(convertObjectToJsonBytes(user)))

                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1))
                .havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).updateUserProfile("test", user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserUnauthorized() throws Exception { // 401
        RegisterUserResource user = createValidRegisterResource();
        Mockito.when(
                userService.havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString()))
                .thenReturn(false);

        mockMvc.perform(put("/api/rest/bluefin/users/test").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(user))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1))
                .havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserBadRequestInvalidRequestBody() throws Exception { // 400
        UpdateUserResource user = createInvalidUpdateResource();

        String validationErros = mockMvc
                .perform(put("/api/rest/bluefin/users/test").contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(user)))
                .andExpect(status().isBadRequest()).andReturn().getResolvedException().getMessage();

        assertThat(validationErros, containsString("email must not be empty"));
        assertThat(validationErros, containsString("firstName must not be empty"));
        assertThat(validationErros, containsString("lastName must not be empty"));
        Mockito.verify(userService, Mockito.times(0)).updateUserProfile("test", user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserInternalServerError() throws Exception { // 500
        UpdateUserResource user = createValidUpdateResource();
        Mockito.when(
                userService.havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(userService.updateUserProfile("test", user)).thenThrow(new CustomException(""));

        mockMvc.perform(put("/api/rest/bluefin/users/test").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(user))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1))
                .havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).updateUserProfile("test", user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    // Update user roles tests

    @Test
    public void updateUserRolesOK() throws Exception { // 200
        List<Integer> roles = createValidRoleIdsList();
        UserResource updatedUser = createValidUserResource();
        Mockito.when(userService.updateUserRoles("test", roles)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/rest/bluefin/users/test/roles").header("X-Auth-Token", "tokenTest")
                .contentType(MediaType.APPLICATION_JSON).content(convertObjectToJsonBytes(roles)))

                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1)).updateUserRoles("test", roles);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserRolesUnauthorized() throws Exception { // 401
        List<Integer> roles = createValidRoleIdsList();

        Mockito.when(userService.updateUserRoles("test", roles)).thenThrow(new CustomUnauthorizedException(""));
        mockMvc.perform(put("/api/rest/bluefin/users/test/roles").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(roles))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).updateUserRoles("test", roles);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserRolesBadRequestInvalidRequestBody() throws Exception { // 400
        List<String> invalidRoleIdsList = new ArrayList<String>();
        invalidRoleIdsList.add("ROLE_TEST");

        mockMvc.perform(put("/api/rest/bluefin/users/test/roles").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(invalidRoleIdsList))).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserRolesInternalServerError() throws Exception { // 500
        List<Integer> roles = createValidRoleIdsList();
        Mockito.when(userService.updateUserRoles("test", roles)).thenThrow(new CustomException(""));

        mockMvc.perform(put("/api/rest/bluefin/users/test/roles").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(roles))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).updateUserRoles("test", roles);
        Mockito.verifyNoMoreInteractions(userService);
    }

    // Update user legalEntities tests

    @Test
    public void updateUserLegalEntitiesOK() throws Exception { // 200
        List<Integer> legalEntities = createValidLegalEntityIdsList();
        UserResource updatedUser = createValidUserResource();
        Mockito.when(userService.updateUserLegalEntities("test", legalEntities)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/rest/bluefin/users/test/legal-entities").header("X-Auth-Token", "tokenTest")
                .contentType(MediaType.APPLICATION_JSON).content(convertObjectToJsonBytes(legalEntities)))

                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1)).updateUserLegalEntities("test", legalEntities);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserLegalEntitiesUnauthorized() throws Exception { // 401
        List<Integer> legalEntities = createValidLegalEntityIdsList();

        Mockito.when(userService.updateUserLegalEntities("test", legalEntities))
                .thenThrow(new CustomUnauthorizedException(""));
        mockMvc.perform(put("/api/rest/bluefin/users/test/legal-entities").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(legalEntities))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).updateUserLegalEntities("test", legalEntities);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserLegalEntitiesBadRequestInvalidRequestBody() throws Exception { // 400
        List<String> invalidLegalEntityIdsList = new ArrayList<String>();
        invalidLegalEntityIdsList.add("LEGAL_ENTITY_APP_1");

        mockMvc.perform(put("/api/rest/bluefin/users/test/legal-entities").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(invalidLegalEntityIdsList))).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserLegalEntitiesInternalServerError() throws Exception { // 500
        List<Integer> legalEntities = createValidLegalEntityIdsList();
        Mockito.when(userService.updateUserLegalEntities("test", legalEntities)).thenThrow(new CustomException(""));

        mockMvc.perform(put("/api/rest/bluefin/users/test/legal-entities").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(legalEntities))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).updateUserLegalEntities("test", legalEntities);
        Mockito.verifyNoMoreInteractions(userService);
    }

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    private RegisterUserResource createInvalidRegisterResource() {
        RegisterUserResource newUser = new RegisterUserResource();
        newUser.setEmail(null);
        newUser.setFirstName(null);
        newUser.setLastName(null);
        newUser.setRoles(null);
        newUser.setLegalEntityApps(null);
        return newUser;
    }

    private UpdateUserResource createInvalidUpdateResource() {
        UpdateUserResource user = new UpdateUserResource();
        user.setEmail(null);
        user.setFirstName(null);
        user.setLastName(null);
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

    private UpdateUserResource createValidUpdateResource() {
        UpdateUserResource user = new UpdateUserResource();
        user.setEmail("test@email.com");
        user.setFirstName("test");
        user.setLastName("user");
        return user;
    }

    private UserResource createValidUserResource() {
        UserResource userResource = new UserResource();
        userResource.setEmail("test@email.com");
        userResource.setFirstName("test");
        userResource.setLastName("user");
        List<LegalEntityApp> legalEntities = new ArrayList<LegalEntityApp>();
        LegalEntityApp legalEntity = new LegalEntityApp();
        legalEntity.setLegalEntityAppId(1234);
        legalEntity.setLegalEntityAppName("legalEntity1");
        legalEntities.add(legalEntity);
        userResource.setLegalEntityApps(legalEntities);
        List<Role> roles1 = new ArrayList<Role>();
        Role role = new Role();
        role.setRoleId(4321);
        role.setRoleName("ROLE_TESTING");
        role.setDescription("testing role description");
        roles1.add(role);
        userResource.setRoles(roles1);
        userResource.setUsername("userTest");
        return userResource;
    }

    private List<Integer> createValidRoleIdsList() {
        List<Integer> roles = new ArrayList<Integer>();
        roles.add(42);
        roles.add(33);
        roles.add(52);
        roles.add(16);
        roles.add(89);
        return roles;
    }

    private List<Integer> createValidLegalEntityIdsList() {
        List<Integer> roles = new ArrayList<Integer>();
        roles.add(64);
        roles.add(77);
        roles.add(27);
        roles.add(87);
        roles.add(64);
        return roles;
    }
}
