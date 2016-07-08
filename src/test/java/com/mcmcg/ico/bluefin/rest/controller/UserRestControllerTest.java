package com.mcmcg.ico.bluefin.rest.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.UserRole;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.controller.exception.GeneralRestExceptionHandler;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.service.UserService;
import com.mysema.query.types.expr.BooleanExpression;

public class UserRestControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private UserRestController userControllerMock;
    @Mock
    private UserService userService;

    private Authentication auth;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(userControllerMock).setControllerAdvice(new GeneralRestExceptionHandler()).build();
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        auth = new UsernamePasswordAuthenticationToken("omonge", "password", authorities);// Change
                                                                                          // authorities
                                                                                          // when
                                                                                          // set
    }

    @After
    public void teardown() {
        SecurityContextHolder.clearContext();
    }

    // Get User

    @Test
    public void testGetUserAccountSuccessMe() throws Exception {// 200
        Mockito.when(userService.getUserInfomation(Mockito.anyString())).thenReturn(createValidUserResource());

        mockMvc.perform(get("/api/users/{userName}", "me").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"))
                .andExpect(jsonPath("lastName").value("user"))
                .andExpect(jsonPath("$.legalEntityApps[0].legalEntityAppId").value(1234))
                .andExpect(jsonPath("$.legalEntityApps[0].legalEntityAppName").value("legalEntity1"))
                .andExpect(jsonPath("$.roles[0].roleId").value(4321))
                .andExpect(jsonPath("$.roles[0].roleName").value("ROLE_TESTING"));

    }

    @Test
    public void getUserAccountSuccess() throws Exception { // 200
        Mockito.when(userService.havePermissionToGetOtherUsersInformation(Mockito.anyObject(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(userService.getUserInfomation(Mockito.anyString())).thenReturn(createValidUserResource());

        mockMvc.perform(get("/api/users/{userName}", "omonge").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"))
                .andExpect(jsonPath("lastName").value("user"))
                .andExpect(jsonPath("$.legalEntityApps[0].legalEntityAppId").value(1234))
                .andExpect(jsonPath("$.legalEntityApps[0].legalEntityAppName").value("legalEntity1"))
                .andExpect(jsonPath("$.roles[0].roleId").value(4321))
                .andExpect(jsonPath("$.roles[0].roleName").value("ROLE_TESTING"));

        Mockito.verify(userService, Mockito.times(1)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUserAccountUnauthorizedNoObjectSupplied() throws Exception { // 401

        mockMvc.perform(get("/api/users/{userName}", "omonge").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(0)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(0)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUserAccountErrorUnAuthorized() throws Exception { // 401
        Mockito.when(userService.havePermissionToGetOtherUsersInformation(Mockito.anyObject(), Mockito.anyString()))
                .thenReturn(false);

        mockMvc.perform(get("/api/users/{userName}", "omonge").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(0)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUserAccountNotFound() throws Exception { // 404
        Mockito.when(userService.havePermissionToGetOtherUsersInformation(Mockito.anyObject(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(userService.getUserInfomation(Mockito.anyString())).thenThrow(new CustomNotFoundException(""));

        mockMvc.perform(get("/api/users/{userName}", "omonge").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUserAccountErrorInternalServerErrorCheckingPermission() throws Exception { // 500
        Mockito.when(userService.havePermissionToGetOtherUsersInformation(Mockito.anyObject(), Mockito.anyString()))
                .thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/users/{userName}", "omonge").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(0)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUserAccountErrorInternalServerErrorGettingUserData() throws Exception { // 500
        Mockito.when(userService.havePermissionToGetOtherUsersInformation(Mockito.anyObject(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(userService.getUserInfomation(Mockito.anyString())).thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/users/{userName}", "omonge").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUserAccountError() throws Exception { // 500
        Mockito.when(userService.havePermissionToGetOtherUsersInformation(Mockito.anyObject(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(userService.getUserInfomation(Mockito.anyString())).thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/users/nquiros").principal(auth).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).havePermissionToGetOtherUsersInformation(Mockito.anyObject(),
                Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUserInfomation(Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    // Get users tests

    @Test
    public void getUsersOK() throws Exception { // 200
        List<User> users = new ArrayList<User>();
        users.add(createValidUser());
        Mockito.when(userService.getLegalEntitiesByUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntityByUserName());
        Mockito.when(userService.getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(users);

        mockMvc.perform(get("/api/users").principal(auth).param("search", "email:test@email.com,firstName:test")
                .param("page", "1").param("size", "2")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("test@email.com"))
                .andExpect(jsonPath("$[0].username").value("userTest"))
                .andExpect(jsonPath("$[0].firstName").value("test")).andExpect(jsonPath("$[0].lastName").value("user"))
                .andExpect(jsonPath("$[0].roles[0].roleId").value(1234))
                .andExpect(jsonPath("$[0].roles[0].roleName").value("ROLE_TESTING"))
                .andExpect(jsonPath("$[0].roles[0].description").value("role description"))
                .andExpect(jsonPath("$[0].legalEntityApps[0].legalEntityAppId").value(4321))
                .andExpect(jsonPath("$[0].legalEntityApps[0].legalEntityAppName").value("legalEntity1"));

        Mockito.verify(userService, Mockito.times(1)).getLegalEntitiesByUser(Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersOKAllParams() throws Exception { // 200
        List<User> users = new ArrayList<User>();
        users.add(createValidUser());

        Mockito.when(userService.getLegalEntitiesByUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntityByUserName());
        Mockito.when(userService.getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(users);

        mockMvc.perform(get("/api/users").principal(auth)
                .param("search",
                        "email:test@email.com,username:userTest,firstName:test,lastName:user,roles:[1234],legalEntities:[4321]")
                .param("page", "0").param("size", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@email.com"))
                .andExpect(jsonPath("$[0].username").value("userTest"))
                .andExpect(jsonPath("$[0].firstName").value("test")).andExpect(jsonPath("$[0].lastName").value("user"))
                .andExpect(jsonPath("$[0].roles[0].roleId").value(1234))
                .andExpect(jsonPath("$[0].roles[0].roleName").value("ROLE_TESTING"))
                .andExpect(jsonPath("$[0].roles[0].description").value("role description"))
                .andExpect(jsonPath("$[0].legalEntityApps[0].legalEntityAppId").value(4321))
                .andExpect(jsonPath("$[0].legalEntityApps[0].legalEntityAppName").value("legalEntity1"));

        Mockito.verify(userService, Mockito.times(1)).getLegalEntitiesByUser(Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersNoSearchParam() throws Exception { // 200
        List<User> users = new ArrayList<User>();
        users.add(createValidUser());

        Mockito.when(userService.getLegalEntitiesByUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntityByUserName());
        Mockito.when(userService.getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenReturn(users);

        mockMvc.perform(get("/api/users").principal(auth).param("search", "").param("page", "1").param("size", "2"))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value("test@email.com"))
                .andExpect(jsonPath("$[0].username").value("userTest"))
                .andExpect(jsonPath("$[0].firstName").value("test")).andExpect(jsonPath("$[0].lastName").value("user"))
                .andExpect(jsonPath("$[0].roles[0].roleId").value(1234))
                .andExpect(jsonPath("$[0].roles[0].roleName").value("ROLE_TESTING"))
                .andExpect(jsonPath("$[0].roles[0].description").value("role description"))
                .andExpect(jsonPath("$[0].legalEntityApps[0].legalEntityAppId").value(4321))
                .andExpect(jsonPath("$[0].legalEntityApps[0].legalEntityAppName").value("legalEntity1"));
        Mockito.verify(userService, Mockito.times(1)).getLegalEntitiesByUser(Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersNoParams() throws Exception { // 400

        mockMvc.perform(get("/api/users")).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersNoParamsButPage() throws Exception { // 400
        mockMvc.perform(get("/api/users").param("page", "1")).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersNoParamsButSize() throws Exception { // 400

        mockMvc.perform(get("/api/users").param("size", "2")).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersNoParamsButNull() throws Exception { // 400

        mockMvc.perform(get("/api/users").param("page", "null").param("size", "null"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    /**
     * Validates if the user has been authorized search users, exception will be
     * thrown if not authorized
     * 
     * @throws Exception
     */
    @Test
    public void getUsersUnauthorized() throws Exception {

        mockMvc.perform(get("/api/users").param("search", "").param("page", "1").param("size", "2"))
                .andExpect(status().isUnauthorized()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(userService, Mockito.times(0)).getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    /**
     * Test if the user is allowed to get information with Legal Entities that
     * are now owned by the consultant user
     * 
     * @throws Exception
     */
    @Test
    public void getUsersUnauthorizedByLegaEntity() throws Exception { // 401
        List<User> users = new ArrayList<User>();
        users.add(createValidUser());

        Mockito.when(userService.getLegalEntitiesByUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntityByUserName());

        mockMvc.perform(get("/api/users").principal(auth)
                .param("search",
                        "email:test@email.com,username:userTest,firstName:test,lastName:user,roles:[1234],legalEntities:[1,2,3,4]")
                .param("page", "0").param("size", "2")).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).getLegalEntitiesByUser(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersInternalServerError() throws Exception {
        Mockito.when(userService.getLegalEntitiesByUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntityByUserName());
        Mockito.when(userService.getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/users").principal(auth).param("search", "").param("page", "1").param("size", "2"))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).getLegalEntitiesByUser(Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void getUsersInternalServerErrorGettingUsers() throws Exception {
        Mockito.when(userService.getLegalEntitiesByUser(Mockito.anyString()))
                .thenReturn(createValidLegalEntityByUserName());
        Mockito.when(userService.getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString())).thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/users").principal(auth).param("search", "").param("page", "1").param("size", "2"))
                .andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).getLegalEntitiesByUser(Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).getUsers(Mockito.any(BooleanExpression.class), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    // Register user tests

    @Test
    public void registerUserOK() throws Exception { // 201
        RegisterUserResource newUser = createValidRegisterResource();
        UserResource returnedUser = createValidUserResource();
        Mockito.when(userService.hasUserPrivilegesOverLegalEntities(Mockito.anyString(), Mockito.anySet()))
                .thenReturn(true);
        Mockito.when(userService.registerNewUserAccount(newUser)).thenReturn(returnedUser);

        mockMvc.perform(post("/api/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser))).andExpect(status().isCreated())
                .andExpect(jsonPath("username").value("userTest")).andExpect(jsonPath("email").value("test@email.com"))
                .andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1)).hasUserPrivilegesOverLegalEntities(Mockito.anyString(),
                Mockito.anySet());
        Mockito.verify(userService, Mockito.times(1)).registerNewUserAccount(newUser);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void registerUserBadRequestInvalidRequestBody() throws Exception { // 400
        RegisterUserResource newUser = createInvalidRegisterResource();
        UserResource returnedUser = createValidUserResource();
        Mockito.when(userService.hasUserPrivilegesOverLegalEntities(Mockito.anyString(), Mockito.anySet()))
                .thenReturn(true);
        Mockito.when(userService.registerNewUserAccount(newUser)).thenReturn(returnedUser);

        String validationErros = mockMvc
                .perform(post("/api/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newUser)))
                .andExpect(status().isBadRequest()).andReturn().getResolvedException().getMessage();

        assertThat(validationErros, containsString("email must not be empty"));
        assertThat(validationErros, containsString("firstName must not be empty"));
        assertThat(validationErros, containsString("lastName must not be empty"));
        assertThat(validationErros, containsString("roles must not be null"));
        assertThat(validationErros, containsString("legalEntityApps must not be null"));
        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(newUser);
    }

    @Test
    public void registerUserBadRequestInvalidLists() throws Exception { // 400
        RegisterUserResource newUser = createValidRegisterResource();
        newUser.setRoles(new ArrayList<Long>());
        newUser.setLegalEntityApps(new ArrayList<Long>());
        UserResource returnedUser = createValidUserResource();
        Mockito.when(userService.hasUserPrivilegesOverLegalEntities(Mockito.anyString(), Mockito.anySet()))
                .thenReturn(true);
        Mockito.when(userService.registerNewUserAccount(newUser)).thenReturn(returnedUser);

        String validationErros = mockMvc
                .perform(post("/api/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(newUser)))
                .andExpect(status().isBadRequest()).andReturn().getResolvedException().getMessage();

        assertThat(validationErros, containsString("rolesIdsList must not be empty"));
        assertThat(validationErros, containsString("legalEntityApps must not be empty"));
        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(newUser);
    }

    @Test
    public void registerUserUnauthorized() throws Exception { // 401
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userService.registerNewUserAccount(any(RegisterUserResource.class)))
                .thenThrow(new AccessDeniedException(""));

        mockMvc.perform(
                post("/api/users").contentType(MediaType.APPLICATION_JSON).content(convertObjectToJsonBytes(newUser)))
                .andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(any(RegisterUserResource.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void registerUserAccessDeniedUnauthorized() throws Exception { // 401
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userService.hasUserPrivilegesOverLegalEntities(Mockito.anyString(), Mockito.anySet()))
                .thenReturn(false);
        mockMvc.perform(post("/api/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).hasUserPrivilegesOverLegalEntities(Mockito.anyString(),
                Mockito.anySet());
        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(newUser);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void registerUserInternalServerErrorValidatingLegalEntities() throws Exception { // 500
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userService.hasUserPrivilegesOverLegalEntities(Mockito.anyString(), Mockito.anySet()))
                .thenThrow(new RuntimeException(""));

        mockMvc.perform(post("/api/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).hasUserPrivilegesOverLegalEntities(Mockito.anyString(),
                Mockito.anySet());
        Mockito.verify(userService, Mockito.times(0)).registerNewUserAccount(any(RegisterUserResource.class));
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void registerUserInternalServerError() throws Exception { // 500
        RegisterUserResource newUser = createValidRegisterResource();
        Mockito.when(userService.hasUserPrivilegesOverLegalEntities(Mockito.anyString(), Mockito.anySet()))
                .thenReturn(true);
        Mockito.when(userService.registerNewUserAccount(any(RegisterUserResource.class)))
                .thenThrow(new RuntimeException(""));

        mockMvc.perform(post("/api/users").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(newUser))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).hasUserPrivilegesOverLegalEntities(Mockito.anyString(),
                Mockito.anySet());
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

        mockMvc.perform(put("/api/users/test").header("X-Auth-Token", "tokenTest")
                .contentType(MediaType.APPLICATION_JSON).principal(auth).content(convertObjectToJsonBytes(user)))

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

        mockMvc.perform(put("/api/users/test").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(user))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1))
                .havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserBadRequestInvalidRequestBody() throws Exception { // 400
        UpdateUserResource user = createInvalidUpdateResource();

        String validationErros = mockMvc
                .perform(put("/api/users/test").principal(auth).contentType(MediaType.APPLICATION_JSON)
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
        Mockito.when(userService.updateUserProfile("test", user)).thenThrow(new RuntimeException(""));

        mockMvc.perform(put("/api/users/test").principal(auth).contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(user))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1))
                .havePermissionToGetOtherUsersInformation(any(Authentication.class), Mockito.anyString());
        Mockito.verify(userService, Mockito.times(1)).updateUserProfile("test", user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    // Update user roles tests

    @Test
    public void updateUserRolesOK() throws Exception { // 200
        List<Long> roles = createValidRoleIdsList();
        UserResource updatedUser = createValidUserResource();
        Mockito.when(userService.updateUserRoles("test", roles)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/test/roles").header("X-Auth-Token", "tokenTest")
                .contentType(MediaType.APPLICATION_JSON).content(convertObjectToJsonBytes(roles)))

                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1)).updateUserRoles("test", roles);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserRolesUnauthorized() throws Exception { // 401
        List<Long> roles = createValidRoleIdsList();

        Mockito.when(userService.updateUserRoles("test", roles)).thenThrow(new AccessDeniedException(""));
        mockMvc.perform(put("/api/users/test/roles").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(roles))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).updateUserRoles("test", roles);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserRolesBadRequestInvalidRequestBody() throws Exception { // 400
        List<String> invalidRoleIdsList = new ArrayList<String>();
        invalidRoleIdsList.add("ROLE_TEST");

        mockMvc.perform(put("/api/users/test/roles").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(invalidRoleIdsList))).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserRolesInternalServerError() throws Exception { // 500
        List<Long> roles = createValidRoleIdsList();
        Mockito.when(userService.updateUserRoles("test", roles)).thenThrow(new RuntimeException(""));

        mockMvc.perform(put("/api/users/test/roles").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(roles))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).updateUserRoles("test", roles);
        Mockito.verifyNoMoreInteractions(userService);
    }

    // Update user legalEntities tests

    @Test
    public void updateUserLegalEntitiesOK() throws Exception { // 200
        List<Long> legalEntities = createValidLegalEntityIdsList();
        UserResource updatedUser = createValidUserResource();
        Mockito.when(userService.updateUserLegalEntities("test", legalEntities)).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/test/legal-entities").header("X-Auth-Token", "tokenTest")
                .contentType(MediaType.APPLICATION_JSON).content(convertObjectToJsonBytes(legalEntities)))

                .andExpect(status().isOk()).andExpect(jsonPath("username").value("userTest"))
                .andExpect(jsonPath("email").value("test@email.com")).andExpect(jsonPath("firstName").value("test"));

        Mockito.verify(userService, Mockito.times(1)).updateUserLegalEntities("test", legalEntities);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserLegalEntitiesUnauthorized() throws Exception { // 401
        List<Long> legalEntities = createValidLegalEntityIdsList();

        Mockito.when(userService.updateUserLegalEntities("test", legalEntities))
                .thenThrow(new AccessDeniedException(""));
        mockMvc.perform(put("/api/users/test/legal-entities").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(legalEntities))).andExpect(status().isUnauthorized());

        Mockito.verify(userService, Mockito.times(1)).updateUserLegalEntities("test", legalEntities);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserLegalEntitiesBadRequestInvalidRequestBody() throws Exception { // 400
        List<String> invalidLegalEntityIdsList = new ArrayList<String>();
        invalidLegalEntityIdsList.add("LEGAL_ENTITY_APP_1");

        mockMvc.perform(put("/api/users/test/legal-entities").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(invalidLegalEntityIdsList))).andExpect(status().isBadRequest());

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void updateUserLegalEntitiesInternalServerError() throws Exception { // 500
        List<Long> legalEntities = createValidLegalEntityIdsList();
        Mockito.when(userService.updateUserLegalEntities("test", legalEntities)).thenThrow(new RuntimeException(""));

        mockMvc.perform(put("/api/users/test/legal-entities").contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(legalEntities))).andExpect(status().isInternalServerError());

        Mockito.verify(userService, Mockito.times(1)).updateUserLegalEntities("test", legalEntities);
        Mockito.verifyNoMoreInteractions(userService);
    }

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
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
        validLegalEntity.setLegalEntityAppId(4321l);
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
        validRole.setRoleId(1234l);
        return validRole;
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
        newUser.setLegalEntityApps(createValidLegalEntityIdsList());
        newUser.setRoles(createValidRoleIdsList());
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
        legalEntity.setLegalEntityAppId(1234L);
        legalEntity.setLegalEntityAppName("legalEntity1");
        legalEntities.add(legalEntity);
        userResource.setLegalEntityApps(legalEntities);
        List<Role> roles1 = new ArrayList<Role>();
        Role role = new Role();
        role.setRoleId(4321L);
        role.setRoleName("ROLE_TESTING");
        role.setDescription("testing role description");
        roles1.add(role);
        userResource.setRoles(roles1);
        userResource.setUsername("userTest");
        return userResource;
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

    private List<LegalEntityApp> createValidLegalEntityByUserName() {
        return createValidUser().getLegalEntityApps();
    }
}
