package com.mcmcg.ico.bluefin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.ApplicationProperty;
import com.mcmcg.ico.bluefin.repository.PropertyDAO;

@Service
public class PropertyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyService.class);
	
	@Autowired
	private PropertyDAO propertyDAO;

	public String getPropertyValue(String propertyName) {
		LOGGER.debug("Entering to getPropertyValue, propertyName={}",propertyName);
		return propertyDAO.getPropertyValue(propertyName);
	}
	
	public List<ApplicationProperty> getAllProperty() {
		LOGGER.debug("Entering to getAllProperties");
		return propertyDAO.getAllProperty();
	}
	
	public ApplicationProperty updateProperty(ApplicationProperty applicationProperty, String modifiedBy) {
		LOGGER.debug("Entering to updateProperty");
		return propertyDAO.updateProperty(applicationProperty, modifiedBy);
	}

	public ApplicationProperty saveApplicationProperty(ApplicationProperty applicationProperty) {
		LOGGER.debug("Entering to saveProperty");
		return propertyDAO.saveApplicationProperty(applicationProperty);
	}

	public String deleteApplicationProperty(String applicationPropertyId) {
		LOGGER.debug("Entering to deleteProperty");
		return propertyDAO.deleteApplicationProperty(applicationPropertyId);
	}
}
