package com.mcmcg.ico.bluefin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;

@Service
public class LegalEntityAppService {

    @Autowired
    private LegalEntityAppRepository legalEntityAppRepository;

    public List<LegalEntityApp> findAll() { 
        return legalEntityAppRepository.findAll(); 
    }
}
