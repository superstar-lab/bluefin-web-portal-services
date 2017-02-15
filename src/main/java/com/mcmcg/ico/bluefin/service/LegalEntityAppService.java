package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.BasicLegalEntityAppResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;

@Service
@Transactional
public class LegalEntityAppService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppService.class);

    @Autowired
    private LegalEntityAppDAO legalEntityAppRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionService sessionService;

    public List<LegalEntityApp> getLegalEntities(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName());

        if (user == null) {
            LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
                    authentication.getName());
            return new ArrayList<LegalEntityApp>();
        }

        if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            return legalEntityAppRepository.findAll();
        } else {
            List<Long> legalEntitiesFromUser = user.getLegalEntities().stream()
                    .map(userLegalEntity -> userLegalEntity.getLegalEntityApp().getLegalEntityAppId())
                    .collect(Collectors.toList());
            return legalEntityAppRepository.findAll(legalEntitiesFromUser);
        }
    }

    /**
     * This method will find a legal entity by its id, not found exception if it
     * does not exist
     * 
     * @param id
     * @return Legal entity object if found
     */
    public LegalEntityApp getLegalEntityAppById(Long id) {
        LegalEntityApp legalEntityApp = legalEntityAppRepository.findByLegalEntityAppId(id);

        if (legalEntityApp == null) {
            throw new CustomNotFoundException(String.format("Unable to find legal entity app with id = [%s]", id));
        }

        return legalEntityApp;
    }

    public LegalEntityApp createLegalEntity(BasicLegalEntityAppResource legalEntityResource, String modifiedBy) {
        final String newLegalEntityAppName = legalEntityResource.getLegalEntityAppName();

        if (existLegalEntityAppName(newLegalEntityAppName)) {
            throw new CustomBadRequestException(
                    String.format("Legal entity app name = [%s] already exists.", newLegalEntityAppName));
        }

        return legalEntityAppRepository.saveLegalEntityApp(legalEntityResource.toLegalEntityApp(), modifiedBy);
    }

    public LegalEntityApp updateLegalEntityApp(Long id, BasicLegalEntityAppResource legalEntityAppResource, String modifiedBy) {
        LegalEntityApp legalEntityAppToUpdate = legalEntityAppRepository.findByLegalEntityAppId(id);

        if (legalEntityAppToUpdate == null) {
            throw new CustomNotFoundException(String.format("Unable to find legal entity app with id = [%s]", id));
        }

        // Update fields for existing Legal Entity App
        legalEntityAppToUpdate.setLegalEntityAppName(legalEntityAppResource.getLegalEntityAppName());

        return legalEntityAppRepository.updateLegalEntityApp(legalEntityAppToUpdate, modifiedBy);
    }

    public void deleteLegalEntityApp(Long id) {
        LegalEntityApp legalEntityAppToDelete = legalEntityAppRepository.findByLegalEntityAppId(id);

        if (legalEntityAppToDelete == null) {
            throw new CustomNotFoundException(String.format("Unable to find legal entity with id = [%s]", id));
        }

        legalEntityAppRepository.deleteLegalEntityApp(legalEntityAppToDelete);
    }

    /**
     * Get all legal entity app objects by the entered ids
     * 
     * @param legalEntityAppsIds
     *            list of legal entity apps ids that we need to find
     * @return list of legal entity apps
     * @throws CustomBadRequestException
     *             when at least one id does not exist
     */
    public List<LegalEntityApp> getLegalEntityAppsByIds(Set<Long> legalEntityAppsIds) {
        List<LegalEntityApp> result = legalEntityAppRepository.findAll(new ArrayList<Long>(legalEntityAppsIds));

        if (result.size() == legalEntityAppsIds.size()) {
            return result;
        }

        // Create a detail error
        if (result == null || result.isEmpty()) {
            throw new CustomBadRequestException(
                    "The following legal entity apps don't exist.  List = " + legalEntityAppsIds);
        }

        Set<Long> legalEntityAppsNotFound = legalEntityAppsIds.stream().filter(
                x -> !result.stream().map(LegalEntityApp::getLegalEntityAppId).collect(Collectors.toSet()).contains(x))
                .collect(Collectors.toSet());

        throw new CustomBadRequestException(
                "The following legal entity apps don't exist.  List = " + legalEntityAppsNotFound);
    }

    private boolean existLegalEntityAppName(String legalEntityAppName) {
        return legalEntityAppRepository.findByLegalEntityAppName(legalEntityAppName) == null ? false : true;
    }

}
