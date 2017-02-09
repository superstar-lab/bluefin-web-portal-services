package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.Property;

public interface PropertyDAO {
	Property findByName(String name);
}
