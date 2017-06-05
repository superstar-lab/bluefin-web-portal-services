package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.Application;

public interface ApplicationDAO {
	List<Application> findAll();
}
