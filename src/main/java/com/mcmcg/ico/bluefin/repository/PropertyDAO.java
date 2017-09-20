package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.Property;

public interface PropertyDAO {
	public Property findByName(String name);
	public String getPropertyValue(String propertyName);
	public List<Property> findAll();
	public List<Property> getAllProperty();
}
