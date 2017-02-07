package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.Application;
import com.mcmcg.ico.bluefin.repository.ApplicationDAO;

@Service
@Transactional
public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private ApplicationDAO applicationDAO;

    public List<Application> getApplications() {
        LOGGER.info("Getting Applications list.");
        return applicationDAO.findAll();
    }
}