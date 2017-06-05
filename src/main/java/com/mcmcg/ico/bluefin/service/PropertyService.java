package com.mcmcg.ico.bluefin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.repository.PropertyDAO;

@Service
public class PropertyService {

	@Autowired
	private PropertyDAO propertyDAO;

	public String getPropertyValue(String propertyName) {
		return propertyDAO.getPropertyValue(propertyName);
	}
}
