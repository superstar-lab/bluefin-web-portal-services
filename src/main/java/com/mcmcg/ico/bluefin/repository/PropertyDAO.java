package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.ApplicationProperty;
import com.mcmcg.ico.bluefin.model.Property;

public interface PropertyDAO {
	public Property findByName(String name);
	public String getPropertyValue(String propertyName);
	public List<ApplicationProperty> findAll();
	public List<ApplicationProperty> getAllProperty();
	public ApplicationProperty saveOrUpdate(ApplicationProperty applicationProperty);
	public ApplicationProperty saveOrUpdateProperty(ApplicationProperty applicationProperty);
}
