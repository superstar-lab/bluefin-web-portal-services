package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.Property;

public interface PropertyDAO {
	public Property findByName(String name);
	public String getPropertyValue(String propertyName);
}
