package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserLegalEntityAppDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.BasicLegalEntityAppResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

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
			LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
					authentication.getName());
			return new ArrayList<>();
		}

		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			return legalEntityAppDAO.findAll();
		} else {
			List<LegalEntityApp> list = new ArrayList<>();

			LOGGER.info("ready to iteration userLegalEntityApp ");
			for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
				long legalEntityAppId = userLegalEntityApp.getLegalEntityAppId();
				list.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));

			}
			List<Long> legalEntitiesFromUser = list.stream()
					.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toList());
			LOGGER.debug("legalEntitiesFromUser size ={} ",legalEntitiesFromUser.size());
			return legalEntityAppDAO.findAll(legalEntitiesFromUser);
		}
	}
	
	
	
	public List<LegalEntityApp> getActiveLegalEntities(Authentication authentication) {
		User user = userDAO.findByUsername(authentication.getName());
		

		if (user == null) {
			LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
					authentication.getName());
			return new ArrayList<>();
		}

		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			return legalEntityAppDAO.findAllActive();
		} else {
			List<LegalEntityApp> list = new ArrayList<>();

			LOGGER.info("ready to iteration userLegalEntityApp ");
			for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
				long legalEntityAppId = userLegalEntityApp.getLegalEntityAppId();
				LegalEntityApp legalEntity =legalEntityAppDAO.findActiveLegalEntityAppId(legalEntityAppId);
				if (null != legalEntity) {
					list.add(legalEntity);
				}

			}
			List<Long> legalEntitiesFromUser = list.stream()
					.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toList());
			LOGGER.debug("legalEntitiesFromUser size ={} ",legalEntitiesFromUser.size());
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

		LOGGER.debug("legalEntityApp:={} ",legalEntityApp);
		if (legalEntityApp == null) {
			throw new CustomNotFoundException(String.format("Unable to find legal entity app with id = [%s]", id));
		}

		return legalEntityApp;
	}

	public LegalEntityApp createLegalEntity(BasicLegalEntityAppResource legalEntityResource, String modifiedBy) {
		LOGGER.info("Entering to create Legal Entity : ");
		final String newLegalEntityAppName = legalEntityResource.getLegalEntityAppName();

		if (existLegalEntityAppName(newLegalEntityAppName)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("Legal Entity App Creation Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDBY, modifiedBy, BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityResource.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
					"Legal Entity App Name already exists."));
			
			throw new CustomBadRequestException(
					String.format("Legal entity app name = [%s] already exists.", newLegalEntityAppName));
		}

		return legalEntityAppDAO.saveLegalEntityApp(legalEntityResource.toLegalEntityApp(), modifiedBy);
	}

	public LegalEntityApp updateLegalEntityApp(Long id, BasicLegalEntityAppResource legalEntityAppResource,
			String modifiedBy) {
		LegalEntityApp legalEntityAppToUpdate = legalEntityAppDAO.findByLegalEntityAppId(id);

		LOGGER.debug("legalEntityAppToUpdate ={} ",legalEntityAppToUpdate);
		if (legalEntityAppToUpdate == null) {
			LOGGER.error(LoggingUtil.adminAuditInfo("Legal Entity App Update Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDBY, modifiedBy, BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityAppResource.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
					"Unable to find legal entity app with id : ", String.valueOf(id)));
			
			throw new CustomNotFoundException(String.format("Unable to find legal entity app with id = [%s]", id));
		}

		// Update fields for existing Legal Entity App
		legalEntityAppToUpdate.setLegalEntityAppName(legalEntityAppResource.getLegalEntityAppName());
		legalEntityAppToUpdate.setIsActive(legalEntityAppResource.getIsActive());
		legalEntityAppToUpdate.setPrNumber(legalEntityAppResource.getPrNumber());

		return legalEntityAppDAO.updateLegalEntityApp(legalEntityAppToUpdate, modifiedBy);
	}

	public void deleteLegalEntityApp(Long id) {
		LegalEntityApp legalEntityAppToDelete = legalEntityAppDAO.findByLegalEntityAppId(id);

		if (legalEntityAppToDelete == null) {
			LOGGER.error(LoggingUtil.adminAuditInfo("Legal Entity App Deletion Request", BluefinWebPortalConstants.SEPARATOR,
					"Unable to find Legal Entity with Id : ", String.valueOf(id)));
			throw new CustomNotFoundException(String.format("Unable to find legal entity with id = [%s]", id));
		}
		try {
			deleteUserLegalEntityApp(id);
			legalEntityAppDAO.deleteLegalEntityApp(legalEntityAppToDelete);
		} catch (DataIntegrityViolationException exp) {
			if ( LOGGER.isDebugEnabled() ) {
				LOGGER.debug("Failed to delete legal entity app",exp);
			}
			LOGGER.error("Legal Entity= {} with id = {} already in use.",id,legalEntityAppToDelete.getLegalEntityAppName() );
			throw new CustomNotFoundException("Unable to delete this legal entity. There are active payment processor merchant ids that are mapped to this legal entity.");
		}
	}

	private void deleteUserLegalEntityApp(Long id) {
		List<Long> userLegalEntityApps = userLegalEntityAppDAO.fetchLegalEntityApps(id);
		LOGGER.debug("userLegalEntityApps size ={} ",userLegalEntityApps.size());
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
				LOGGER.debug("Legal entities app ids size = {} is not equal to results size= {} returned from DB",legalEntityAppsIds,result != null ? result.size() : 0);
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
	
	public LegalEntityApp getLegalEntityAppName(String legalEntityAppName) {
		return legalEntityAppDAO.findByLegalEntityAppName(legalEntityAppName);
	}
	
	public List<LegalEntityApp> getAllLegalEntities(Authentication authentication) {

		User user = userDAO.findByUsername(authentication.getName());
		if (user == null) {
			LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
					authentication.getName());
			return new ArrayList<>();
		}

		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication) || sessionService.hasPermissionToManageAllUser(authentication)) {
			return legalEntityAppDAO.findAll();
		}
	
		throw new CustomException("User don't have permission to get all legal entity");
	}
}
