package com.mcmcg.ico.bluefin.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.persistent.Permission;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomForbiddenException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.service.SessionService;

public class SessionRestControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private SessionRestController sessionRestControllerMock;

    @Mock
    private SessionService sessionService;

    private static final String TOKEN = "eyJpZCI6NCwidXNlcm5hbWUiOiJvbW9uZ2UiLCJleHBpcmVzIjoxNDY2NjE1MTUxNTExfQ==.rSFleKfMbtTy2710kxOojPlJ0DQ4V9S5NXp2TWsI8SA=";
    private final static String IP_ADDRESS = "127.0.0.1";
    private final static String API = "/api/session/";
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(sessionRestControllerMock).build(); 
        ReflectionTestUtils.setField(sessionRestControllerMock, "securityTokenHeader", "X-Auth-Token");
    }

    // Authenticating
    /**
     * Test a successful response with a valid user and password
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequest() throws Exception {// 200
        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new UsernamePasswordAuthenticationToken("omonge", "test1234"));
        Mockito.when(sessionService.generateToken(Mockito.anyString())).thenReturn(createValidAuthenticationResponse());

        mockMvc.perform(post(API)
                .content(convertObjectToJsonBytes(createValidAuthentificationRequest()))
                .contentType(MediaType.APPLICATION_JSON).param("X-FORWARDED-FOR", IP_ADDRESS))
                .andExpect(status().isOk()).andExpect(jsonPath("token").value(TOKEN))
                .andExpect(jsonPath("username").value("omonge")).andExpect(jsonPath("firstName").value("Oscar"))
                .andExpect(jsonPath("lastName").value("Monge"))
                .andExpect(jsonPath("$.permissions[0].permissionId").value(12345))
                .andExpect(jsonPath("$.permissions[0].permissionName").value("Permission Name"))
                .andExpect(jsonPath("$.permissions[0].description").value("Permission description"));

        Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(1)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }

    /**
     * Test a bad request for when only the password is sent
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestMissingUserName() throws Exception { // 400
        AuthenticationRequest request = createValidAuthentificationRequest();
        request.setUsername(null);

        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new CustomBadRequestException(""));

        mockMvc.perform(post(API)
                .content(convertObjectToJsonBytes(request))
                .param("X-FORWARDED-FOR", IP_ADDRESS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(sessionService, Mockito.times(0)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }
    /**
     * Test the case for when the user has not permissions on the Data Base,
     * wrong user
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestErrorsAuthenticating() throws Exception { // 403
        AuthenticationRequest request = createValidAuthentificationRequest();

        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new CustomBadRequestException(""));

        mockMvc.perform(post(API)
                    .content(convertObjectToJsonBytes(request))
                    .param("X-FORWARDED-FOR", IP_ADDRESS)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }
    /**
     * Test the case for when the user has not permissions on the Data Base,
     * wrong user
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestWrongPasswordAuthenticating() throws Exception { // 403
        AuthenticationRequest request = createValidAuthentificationRequest();
        request.setUsername("omonge1");

        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new CustomForbiddenException(""));

        mockMvc.perform(post(API)
                    .content(convertObjectToJsonBytes(request))
                    .header("X-FORWARDED-FOR", IP_ADDRESS)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }

    /**
     * Test the case for when the user has not permissions on the Data Base,
     * wrong password
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestWrongPasswordAuthenticatingWrongPassword() throws Exception { // 403
        AuthenticationRequest request = createValidAuthentificationRequest();
        request.setPassword("wrongPassword");

        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new CustomForbiddenException(""));

        mockMvc.perform(post(API)
                    .content(convertObjectToJsonBytes(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("X-FORWARDED-FOR", IP_ADDRESS))
                .andExpect(status().isForbidden());

        Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }

    /**
     * Test errors with data base when the user is trying to authenticate
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestException() throws Exception { // 500
        AuthenticationRequest request = new AuthenticationRequest();
        request.setPassword("test1234");
        request.setUsername("omonge");

        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new CustomException(""));

        mockMvc.perform(post(API)
                    .content(convertObjectToJsonBytes(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("X-FORWARDED-FOR", IP_ADDRESS))
            .andExpect(status().isInternalServerError());

        Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }

    // Generating token
    /**
     * Test a bad request for when only the password is sent
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestMissingUserNameGeneratingToken() throws Exception { // 400
        AuthenticationRequest request = createValidAuthentificationRequest();
        request.setUsername(null);

        mockMvc.perform(post(API).content(convertObjectToJsonBytes(request))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());

        Mockito.verify(sessionService, Mockito.times(0)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }

    /**
     * Test the case for when the user has not permissions on the Data Base,
     * wrong password
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestWrongPasswordGenerationToken() throws Exception { // 403
        AuthenticationRequest request = createValidAuthentificationRequest();
        request.setUsername("omonge1");
        request.setPassword("wrongPassword");

        Mockito.when(sessionService.authenticate(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new UsernamePasswordAuthenticationToken("omonge", "test1234"));
        Mockito.when(sessionService.generateToken(Mockito.anyString())).thenThrow(new CustomForbiddenException(""));

        mockMvc.perform(post(API).content(convertObjectToJsonBytes(request))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());

        Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(sessionService, Mockito.times(1)).generateToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }

    // Refresh token

    /**
     * Test a successful response with a valid user and password
     * 
     * @throws Exception
     */
    @Test
    public void testRefreshTokenSuccess() throws Exception {// 200

        mockMvc.perform(put(API)
                .header("X-Auth-Token", "tesate")
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test errors when a token is not supplied
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestExceptionGeneratingTokenNull() throws Exception { // 400
 
        Mockito.when(sessionService.refreshToken(Mockito.anyString()))
        .thenThrow(new CustomBadRequestException(""));

        mockMvc.perform(put(API)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());


        Mockito.verify(sessionService, Mockito.times(0)).refreshToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }
    /**
     * Test errors when a internal server error arise
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestUnauthorizedGeneratingToken() throws Exception { // 401
 
        Mockito.when(sessionService.refreshToken(Mockito.anyString()))
        .thenThrow(new CustomUnauthorizedException(""));

        mockMvc.perform(put(API)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", "tesate"))
        .andExpect(status().isUnauthorized());


        Mockito.verify(sessionService, Mockito.times(1)).refreshToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }
    /**
     * Test errors when a internal server error arise
     * 
     * @throws Exception
     */
    @Test
    public void testAuthenticationRequestExceptionGeneratingToken() throws Exception { // 500
 
        Mockito.when(sessionService.refreshToken(Mockito.anyString()))
        .thenThrow(new CustomException(""));

        mockMvc.perform(put(API)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", "tesate"))
        .andExpect(status().isInternalServerError());


        Mockito.verify(sessionService, Mockito.times(1)).refreshToken(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(sessionService);
    }
    

    private AuthenticationRequest createValidAuthentificationRequest() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setPassword("test1234");
        request.setUsername("omonge");

        return request;
    }

    private AuthenticationResponse createValidAuthenticationResponse() {
        AuthenticationResponse response = new AuthenticationResponse();
        response.setFirstName("Oscar");
        response.setLastName("Monge");
        response.setUsername("omonge");
        response.setPermissions(getPermissions());
        response.setToken(TOKEN);
        return response;
    }

    private List<Permission> getPermissions() {
        List<Permission> permissionsResult = new ArrayList<Permission>();
        permissionsResult.add(createPermission());
        return permissionsResult;
    }

    private Permission createPermission() {
        Permission permission = new Permission();
        permission.setDescription("Permission description");
        permission.setPermissionId(12345L);
        permission.setPermissionName("Permission Name");
        return permission;
    }

    private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

}
