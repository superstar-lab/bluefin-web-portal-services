package com.mcmcg.ico.bluefin.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.RoleService;

public class RoleRestControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private RoleRestController roleControllerMock;

	@Mock
	private RoleService roleService;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		mockMvc = standaloneSetup(roleControllerMock).addFilters().build();
	}

	@Test
	public void getRolesOK() throws Exception { // 200
		List<Role> roleList = new ArrayList<Role>();
		Role role = new Role();
		role.setRoleId(1L);
		role.setRoleName("ROLE_TESTING");
		role.setDescription("test description");
		roleList.add(role);

		Mockito.when(roleService.getRoles()).thenReturn(roleList);

		mockMvc.perform(get("/api/roles")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].roleId").value(1)).andExpect(jsonPath("$[0].roleName").value("ROLE_TESTING"))
				.andExpect(jsonPath("$[0].description").value("test description"));

		Mockito.verify(roleService, Mockito.times(1)).getRoles();
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test
	public void getRolesNotFound() throws Exception { // 404
		Mockito.when(roleService.getRoles()).thenThrow(new CustomNotFoundException(""));

		mockMvc.perform(get("/api/roles")).andExpect(status().isNotFound());

		Mockito.verify(roleService, Mockito.times(1)).getRoles();
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test
	public void getRolesBadRequest() throws Exception { // 400
		Mockito.when(roleService.getRoles()).thenThrow(new CustomBadRequestException(""));

		mockMvc.perform(get("/api/roles")).andExpect(status().isBadRequest());

		Mockito.verify(roleService, Mockito.times(1)).getRoles();
		Mockito.verifyNoMoreInteractions(roleService);
	}

	@Test
	public void getRolesInternalServerError() throws Exception { // 500
		Mockito.when(roleService.getRoles()).thenThrow(new CustomException(""));

		mockMvc.perform(get("/api/roles")).andExpect(status().isInternalServerError());

		Mockito.verify(roleService, Mockito.times(1)).getRoles();
		Mockito.verifyNoMoreInteractions(roleService);
	}
}
