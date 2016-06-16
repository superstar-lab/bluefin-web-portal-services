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

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;

public class LegalEntityAppControllerTest {

    MockMvc mockMvc;

    @InjectMocks
    private LegalEntityAppRestController legalEntityAppControllerMock;

    @Mock
    private LegalEntityAppService legalEntityAppService;

    @Before
    public void initMocks() {

        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(legalEntityAppControllerMock).addFilters().build();
    }

    @Test
    public void getLegalEntitiesOK() throws Exception { // 200

        List<LegalEntityApp> legalEntityAppList = new ArrayList<LegalEntityApp>();
        LegalEntityApp legalEntity = new LegalEntityApp();
        legalEntity.setLegalEntityAppId(1);
        legalEntity.setLegalEntityAppName("LegalEntity");
        legalEntityAppList.add(legalEntity);

        Mockito.when(legalEntityAppService.getLegalEntities()).thenReturn(legalEntityAppList);

        mockMvc.perform(get("/api/rest/bluefin/legal-entities")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].legalEntityAppId").value(1))
                .andExpect(jsonPath("$[0].legalEntityAppName").value("LegalEntity"));

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities();

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test
    public void getLegalEntitiesNotFound() throws Exception { // 404

        Mockito.when(legalEntityAppService.getLegalEntities()).thenThrow(new CustomNotFoundException(""));

        mockMvc.perform(get("/api/rest/bluefin/legal-entities")).andExpect(status().isNotFound());

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities();

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test
    public void getLegalEntitiesBadRequest() throws Exception { // 400

        Mockito.when(legalEntityAppService.getLegalEntities()).thenThrow(new CustomBadRequestException(""));

        mockMvc.perform(get("/api/rest/bluefin/legal-entities")).andExpect(status().isBadRequest());

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities();

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test
    public void getLegalEntitiesUnauthorized() throws Exception { // 401

        Mockito.when(legalEntityAppService.getLegalEntities()).thenThrow(new CustomUnauthorizedException(""));

        mockMvc.perform(get("/api/rest/bluefin/legal-entities")).andExpect(status().isUnauthorized());

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities();

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }

    @Test
    public void getLegalEntitiesInternalServerError() throws Exception { // 500

        Mockito.when(legalEntityAppService.getLegalEntities()).thenThrow(new CustomException(""));

        mockMvc.perform(get("/api/rest/bluefin/legal-entities")).andExpect(status().isInternalServerError());

        Mockito.verify(legalEntityAppService, Mockito.times(1)).getLegalEntities();

        Mockito.verifyNoMoreInteractions(legalEntityAppService);
    }
}
