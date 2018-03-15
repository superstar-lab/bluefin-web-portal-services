package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.BinDBDetails;

public interface BinDBDAO {

	public List<BinDBDetails> fetchBinDBDetailsForCardNumbers(List<String> cardNumbers);
	
	public BinDBDetails fetchBinDBDetailForCardNumber(String cardFirst6Char);
}
