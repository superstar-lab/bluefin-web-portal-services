package com.mcmcg.ico.bluefin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Property;
import com.mcmcg.ico.bluefin.persistent.jpa.PropertyRepository;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    public String getPropertyValue(String propertyName) {
        Property property = propertyRepository.findByName(propertyName);
        return property == null ? "" : property.getValue();
    }
}
