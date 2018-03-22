package com.mcmcg.ico.bluefin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.BinDBDetails;
import com.mcmcg.ico.bluefin.repository.BinDBDAO;

@Service
public class BinDBService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinDBService.class);
	
	@Autowired
	private BinDBDAO binDBDAO;
	
	public List<BinDBDetails> fetchBinDBDetailsForCardNumbers(List<String> cardNumbers) {
		LOGGER.info("fetchDetailsForCardNumbers");
		return binDBDAO.fetchBinDBDetailsForCardNumbers(cardNumbers);
	}
	
	public BinDBDetails fetchBinDBDetailForCardNumber(String cardFirst6Char){
		return binDBDAO.fetchBinDBDetailForCardNumber(cardFirst6Char);
	}
	
}
