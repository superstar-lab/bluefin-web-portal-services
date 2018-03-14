package com.mcmcg.ico.bluefin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.repository.BinDBDAO;

@Service
public class BinDBService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinDBService.class);
	
	@Autowired
	private BinDBDAO binDBDAO;
	
	public void fetchAllBinDBs() {
		LOGGER.info("fetchAllBinDBs");
		binDBDAO.fetchAllBinDBs();
	}
	
}
