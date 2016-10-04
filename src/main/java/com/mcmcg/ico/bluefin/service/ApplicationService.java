package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Application;
import com.mcmcg.ico.bluefin.persistent.jpa.ApplicationRepository;

@Service
@Transactional
public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    public List<Application> getApplications() {
        LOGGER.info("Getting Applications list.");
        return applicationRepository.findAll();
    }
}