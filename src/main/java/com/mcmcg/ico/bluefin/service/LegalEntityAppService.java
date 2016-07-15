package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.LegalEntityAppRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.LegalEntityAppResource;

@Service
public class LegalEntityAppService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppService.class);

    @Autowired
    private LegalEntityAppRepository legalEntityAppRepository;
    @Autowired
    private UserRepository userRepository;

    public List<LegalEntityApp> getLegalEntities(String userName) {
        User user = userRepository.findByUsername(userName);

        if (user == null) {
            return new ArrayList<LegalEntityApp>();
        }

        List<Long> listOfIds = user.getLegalEntities().stream()
                .map(userLegalEntity -> userLegalEntity.getLegalEntityApp().getLegalEntityAppId())
                .collect(Collectors.toList());

        return legalEntityAppRepository.findAll(listOfIds);
    }
    
    /**
     * This method will find a legal entity by its id, not found exception
     * if it does not exist
     * 
     * @param id
     * @return Legal entity object if found
     */
    public LegalEntityApp getLegalEntityAppById(Long id) {
        LegalEntityApp legalEntityApp = legalEntityAppRepository.findOne(id);

        if (legalEntityApp == null) {
            LOGGER.error("Unable to find legal entity, it doesn't exists: [{}]", id);
            throw new CustomNotFoundException(
                    String.format("Unable to process request legal entity doesn't exists with given id: %s", id));
        }
        return legalEntityApp;
    }

    public LegalEntityApp createLegalEntity(LegalEntityAppResource legalEntityResource) {
        LegalEntityApp legalEntityApp = legalEntityResource.toLegalEntityApp();
        String legalEntityAppName = legalEntityApp.getLegalEntityAppName();

        if (existLegalEntityAppName(legalEntityAppName)) {
            LOGGER.error("Unable to create Legal Entity already exists: [{}]",
                    legalEntityAppName);
            throw new CustomBadRequestException(String
                    .format("Unable to create Legal Entity already exists: %s", legalEntityAppName));
        }
        return legalEntityAppRepository.save(legalEntityApp);
    }
    
    public LegalEntityApp updateLegalEntityApp(Long id, LegalEntityAppResource legalEntityAppResource) {
        LegalEntityApp legalEntityAppToUpdate = legalEntityAppRepository.findOne(id);

        if (legalEntityAppToUpdate == null) {
            LOGGER.error("Unable to update Legal Entity, it doesn't exists: [{}]", id);
            throw new CustomNotFoundException(
                    String.format("Unable to process request Legal Entity doesn't exists with given id: %s", id));
        }
        legalEntityAppResource.updateLegalEntityApp(legalEntityAppToUpdate);
        return legalEntityAppRepository.save(legalEntityAppToUpdate);
    }
    
    public void deleteLegalEntityApp(Long id) {
        LegalEntityApp legalEntityAppToDelete = legalEntityAppRepository.findOne(id);

        if (legalEntityAppToDelete == null) {
            LOGGER.error("Unable to delete Legal Entity, it doesn't exists: [{}]", id);
            throw new CustomNotFoundException(
                    String.format("Unable to process request Legal Entity doesn't exists with given id: %s", id));
        }
        legalEntityAppRepository.delete(legalEntityAppToDelete);
    }
    
    private boolean existLegalEntityAppName(String legalEntityAppName) {
        return legalEntityAppRepository.findByLegalEntityAppName(legalEntityAppName) == null ? false : true;
    }

}
