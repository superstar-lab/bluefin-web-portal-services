package com.mcmcg.ico.bluefin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
