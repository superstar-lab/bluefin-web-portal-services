package com.mcmcg.ico.bluefin.rest.controller;

import static org.mockito.Mockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.controller.exception.GeneralRestExceptionHandler;
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
	private final static String API = "/api/session/";

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		mockMvc = standaloneSetup(sessionRestControllerMock).setControllerAdvice(new GeneralRestExceptionHandler())
				.build();
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
		doNothing().when(sessionService).authenticate(Mockito.anyString(), Mockito.anyString());
		Mockito.when(sessionService.generateToken(Mockito.anyString())).thenReturn(createValidAuthenticationResponse());

		mockMvc.perform(post(API).content(convertObjectToJsonBytes(createValidAuthentificationRequest()))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("token").value(TOKEN)).andExpect(jsonPath("username").value("omonge"))
				.andExpect(jsonPath("firstName").value("Oscar")).andExpect(jsonPath("lastName").value("Monge"))
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

		mockMvc.perform(post(API).content(convertObjectToJsonBytes(request)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		Mockito.verify(sessionService, Mockito.times(0)).authenticate(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/**
	 * Tests the case for when a Request body is not supplied
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAuthenticationRequestIsMissing() throws Exception { // 400
		mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());

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
	public void testAuthenticationRequestWrongPasswordAuthenticating() throws Exception { // 403
		AuthenticationRequest request = createValidAuthentificationRequest();
		request.setUsername("omonge1");

		doNothing().when(sessionService).authenticate(Mockito.anyString(), Mockito.anyString());

		doThrow(new DataAccessResourceFailureException("")).when(sessionService).authenticate(Mockito.anyString(), Mockito.anyString());

		mockMvc.perform(post(API).content(convertObjectToJsonBytes(request)).contentType(MediaType.APPLICATION_JSON))
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

		doThrow(new RuntimeException("")).when(sessionService).authenticate(Mockito.anyString(), Mockito.anyString());

		mockMvc.perform(post(API).content(convertObjectToJsonBytes(request)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

		Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	// Generating token
	/**
	 * Tests the case for when there's a 404 error creating the login response
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGenerationTokenErrorCreatingResponse() throws Exception { // 404
		AuthenticationRequest request = createValidAuthentificationRequest();

		doNothing().when(sessionService).authenticate(Mockito.anyString(), Mockito.anyString());

		Mockito.when(sessionService.generateToken(Mockito.anyString())).thenThrow(new CustomNotFoundException(""));

		mockMvc.perform(post(API).content(convertObjectToJsonBytes(request)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(sessionService, Mockito.times(1)).generateToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/**
	 * Tests the case for when trying to load the user by name but no user is
	 * found, throwing UsernameNotFoundException**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGenerationTokenErrorLoadUserByUsername() throws Exception { // 500
		AuthenticationRequest request = createValidAuthentificationRequest();

		doNothing().when(sessionService).authenticate(Mockito.anyString(), Mockito.anyString());

		Mockito.when(sessionService.generateToken(Mockito.anyString())).thenThrow(new RuntimeException(""));

		mockMvc.perform(post(API).content(convertObjectToJsonBytes(request)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());

		Mockito.verify(sessionService, Mockito.times(1)).authenticate(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(sessionService, Mockito.times(1)).generateToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}


	/**
	 * Test errors when a token is not supplied
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAuthenticationRequestExceptionGeneratingTokenNull() throws Exception { // 400

		mockMvc.perform(put(API).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());

		Mockito.verify(sessionService, Mockito.times(0)).refreshToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/**
	 * Test the case for when the login response created but a
	 * CustomNotFoundException is raised.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRefreshTokenCustomNotFoundException() throws Exception { // 404

		Mockito.when(sessionService.refreshToken(Mockito.anyString())).thenThrow(new CustomNotFoundException(""));

		mockMvc.perform(put(API).contentType(MediaType.APPLICATION_JSON).header("X-Auth-Token", TOKEN))
				.andExpect(status().isNotFound());

		Mockito.verify(sessionService, Mockito.times(1)).refreshToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/**
	 * Test errors when an unauthorized exception will arise, when given token
	 * is no valid
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAuthenticationRequestUnauthorizedGeneratingToken() throws Exception { // 401

		Mockito.when(sessionService.refreshToken(Mockito.anyString())).thenThrow(new AccessDeniedException(""));

		mockMvc.perform(put(API).contentType(MediaType.APPLICATION_JSON).header("X-Auth-Token", "tesate"))
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

		Mockito.when(sessionService.refreshToken(Mockito.anyString())).thenThrow(new RuntimeException(""));

		mockMvc.perform(put(API).contentType(MediaType.APPLICATION_JSON).header("X-Auth-Token", "tesate"))
				.andExpect(status().isInternalServerError());

		Mockito.verify(sessionService, Mockito.times(1)).refreshToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/// Test log out

	/**
	 * Test a successful logout with a valid token
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUserLogoutRequest() throws Exception {// 204
		Mockito.doNothing().when(sessionService).deleteSession(Mockito.anyString());

		mockMvc.perform(delete(API).header("X-Auth-Token", "tokenTest1234")).andExpect(status().isNoContent());

		Mockito.verify(sessionService, Mockito.times(1)).deleteSession(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/**
	 * Test a bad request for no token provided
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUserLogoutRequestMissingToken() throws Exception { // 400
		mockMvc.perform(delete(API)).andExpect(status().isBadRequest());

		Mockito.verify(sessionService, Mockito.times(0)).authenticate(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(sessionService, Mockito.times(0)).generateToken(Mockito.anyString());
		Mockito.verifyNoMoreInteractions(sessionService);
	}

	/**
	 * Test errors with data base when the user is lo delete the currently
	 * session
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUserLogoutRequestException() throws Exception { // 500
		doThrow(new RuntimeException("")).when(sessionService).deleteSession(Mockito.anyString());

		mockMvc.perform(delete(API).header("X-Auth-Token", "tokenTest1234"))
				.andExpect(status().isInternalServerError());

		Mockito.verify(sessionService, Mockito.times(1)).deleteSession(Mockito.anyString());
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
		// response.setPermissions(getPermissions());
		response.setToken(TOKEN);
		return response;
	}

	private Set<Permission> getPermissions() {
		Set<Permission> permissionsResult = new HashSet<Permission>();
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
