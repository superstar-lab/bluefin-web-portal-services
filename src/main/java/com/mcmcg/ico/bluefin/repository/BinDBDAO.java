package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.BinDBDetails;

public interface BinDBDAO {

	public List<BinDBDetails> fetchDetailsForCardNumbers(List<String> cardNumbers);
}
