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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.controller.exception.GeneralRestExceptionHandler;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;

public class LegalEntityAppControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private LegalEntityAppRestController legalEntityAppControllerMock;

    @Mock
    private LegalEntityAppService legalEntityAppService;
    private Authentication auth;

    /**
     * Initiates the services that are going to be mocked and then injected to
     * the controller. Set a list of authorities for the Authenticated user
     */
    @Before
    public void initMocks() {

        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(legalEntityAppControllerMock).setControllerAdvice(new GeneralRestExceptionHandler())
                .build();

        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        auth = new UsernamePasswordAuthenticationToken("omonge", "password", authorities);
    }

    /**
     * Tests the success behavior of the api. In this case there's only one
     * Legal Entity in DB. The expected result is being checked when data is
     * returned
     * 
     * @throws Exception
     */
    @Test
    public void getLegalEntitiesOK() throws Exception { // 200

        List<LegalEntityApp> legalEntityAppList = new ArrayList<LegalEntityApp>();
        LegalEntityApp legalEntity = new LegalEntityApp();
        legalEntity.setLegalEntityAppId(1);
        legalEntity.setLegalEntityAppName("LegalEntity");
        legalEntityAppList.add(legalEntity);

        Mockito.when(legalEntityAppService.getLegalEntities("omonge")).thenReturn(legalEntityAppList);

        mockMvc.perform(get("/api/legal-entities").principal(auth)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].legalEntityAppId").value(1))
                .andExpect(jsonPath("$[0].legalEntityAppName").value("LegalEntity"));

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities("omonge");

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    /**
     * Tests the case for when the request given does not bring the object
     * principal, used for authentication, in that case will throw a
     * unauthorized exception
     * 
     * @throws Exception
     */
    @Test
    public void getLegalEntitiesAccessDenied() throws Exception { // 401
        mockMvc.perform(get("/api/legal-entities")).andExpect(status().isUnauthorized());
        Mockito.verify(legalEntityAppService, Mockito.times(0)).getLegalEntities("omonge");
        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    /**
     * Tests for a server exception to arise, in that case a error 500 will be
     * returned to the user.
     * 
     * @throws Exception
     */
    @Test
    public void getLegalEntitiesInternalServerError() throws Exception { // 500

        Mockito.when(legalEntityAppService.getLegalEntities("omonge")).thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/legal-entities").principal(auth)).andExpect(status().isInternalServerError());

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities("omonge");

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }
}
