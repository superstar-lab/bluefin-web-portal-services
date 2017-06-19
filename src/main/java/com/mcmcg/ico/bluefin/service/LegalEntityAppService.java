package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserLegalEntityAppDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.BasicLegalEntityAppResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;

@Service
@Transactional
public class LegalEntityAppService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppService.class);

	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserLegalEntityAppDAO userLegalEntityAppDAO;

	public List<LegalEntityApp> getLegalEntities(Authentication authentication) {
		User user = userDAO.findByUsername(authentication.getName());
		

		if (user == null) {
			LOGGER.warn("LegalEntityAppService :: getLegalEntities : User not found, then we need to return an empty list.  Details: username = [{}]",
					authentication.getName());
			return new ArrayList<LegalEntityApp>();
		}

		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			return legalEntityAppDAO.findAll();
		} else {
			List<LegalEntityApp> list = new ArrayList<LegalEntityApp>();

			LOGGER.info("LegalEntityAppService :: getLegalEntities : ready to iteration userLegalEntityApp ");
			for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
				long legalEntityAppId = userLegalEntityApp.getLegalEntityAppId();
				list.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));

			}
			List<Long> legalEntitiesFromUser = list.stream()
					.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toList());
			LOGGER.debug("LegalEntityAppService :: getLegalEntities : legalEntitiesFromUser size : "+legalEntitiesFromUser.size());
			return legalEntityAppDAO.findAll(legalEntitiesFromUser);
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
		LegalEntityApp legalEntityApp = legalEntityAppDAO.findByLegalEntityAppId(id);

		LOGGER.debug("LegalEntityAppService :: getLegalEntityAppById : legalEntityApp : "+legalEntityApp);
		if (legalEntityApp == null) {
			throw new CustomNotFoundException(String.format("Unable to find legal entity app with id = [%s]", id));
		}

		return legalEntityApp;
	}

	public LegalEntityApp createLegalEntity(BasicLegalEntityAppResource legalEntityResource, String modifiedBy) {
		LOGGER.info("LegalEntityAppService :: Entering to createLegalEntity : ");
		final String newLegalEntityAppName = legalEntityResource.getLegalEntityAppName();

		if (existLegalEntityAppName(newLegalEntityAppName)) {
			throw new CustomBadRequestException(
					String.format("Legal entity app name = [%s] already exists.", newLegalEntityAppName));
		}

		return legalEntityAppDAO.saveLegalEntityApp(legalEntityResource.toLegalEntityApp(), modifiedBy);
	}

	public LegalEntityApp updateLegalEntityApp(Long id, BasicLegalEntityAppResource legalEntityAppResource,
			String modifiedBy) {
		LegalEntityApp legalEntityAppToUpdate = legalEntityAppDAO.findByLegalEntityAppId(id);

		LOGGER.debug("LegalEntityAppService :: updateLegalEntityApp : legalEntityAppToUpdate : "+legalEntityAppToUpdate);
		if (legalEntityAppToUpdate == null) {
			throw new CustomNotFoundException(String.format("Unable to find legal entity app with id = [%s]", id));
		}

		// Update fields for existing Legal Entity App
		legalEntityAppToUpdate.setLegalEntityAppName(legalEntityAppResource.getLegalEntityAppName());

		return legalEntityAppDAO.updateLegalEntityApp(legalEntityAppToUpdate, modifiedBy);
	}

	public void deleteLegalEntityApp(Long id) {
		LegalEntityApp legalEntityAppToDelete = legalEntityAppDAO.findByLegalEntityAppId(id);

		if (legalEntityAppToDelete == null) {
			throw new CustomNotFoundException(String.format("Unable to find legal entity with id = [%s]", id));
		}
		try {
			deleteUserLegalEntityApp(id);
			legalEntityAppDAO.deleteLegalEntityApp(legalEntityAppToDelete);
		} catch (DataIntegrityViolationException exp) {
			if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("Failed to delete legal entity app , Exp Message= {}",exp.getMessage());
			}
			LOGGER.error("LegalEntityAppService :: deleteLegalEntityApp : Legal Entity= {} with id = {} already in use.",id,legalEntityAppToDelete.getLegalEntityAppName() );
			throw new CustomNotFoundException("Unable to delete this legal entity. There are active payment processor merchant ids that are mapped to this legal entity.");
		}
	}

	private void deleteUserLegalEntityApp(Long id) {
		List<Long> userLegalEntityApps = userLegalEntityAppDAO.fetchLegalEntityApps(id);
		LOGGER.debug("LegalEntityAppService :: deleteUserLegalEntityApp : userLegalEntityApps size : "+userLegalEntityApps.size());
		userService.removeLegalEntityFromUser(userLegalEntityApps);
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
		List<LegalEntityApp> result = legalEntityAppDAO.findAll(new ArrayList<Long>(legalEntityAppsIds));

		if (result != null && result.size() == legalEntityAppsIds.size()) {
			return result;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Legal entities app ids size ="+legalEntityAppsIds+" is not equal to results size="+ ( ( result != null ? result.size() : 0 ) ) +" returned from DB");
			}
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
		return legalEntityAppDAO.findByLegalEntityAppName(legalEntityAppName) == null ? false : true;
	}
}
