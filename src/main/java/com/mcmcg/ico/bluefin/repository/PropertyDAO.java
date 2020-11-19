package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.ApplicationProperty;
import com.mcmcg.ico.bluefin.model.Property;

public interface PropertyDAO {
	public Property findByName(String name);
	public String getPropertyValue(String propertyName);
	public List<ApplicationProperty> findAll();
	public List<ApplicationProperty> getAllProperty();
	public String getProperties(String[] applicationPropertyList);
	public ApplicationProperty update(ApplicationProperty applicationProperty, String modifiedBy);
	public ApplicationProperty updateProperty(ApplicationProperty applicationProperty, String modifiedBy);
	public ApplicationProperty save(ApplicationProperty applicationProperty);
	public ApplicationProperty saveApplicationProperty(ApplicationProperty applicationProperty);
	public String delete(String applicationPropertyId);
	public String deleteApplicationProperty(String applicationPropertyId);
}
