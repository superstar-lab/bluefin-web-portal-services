package com.mcmcg.ico.bluefin.rest.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
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
}
