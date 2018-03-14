package com.mcmcg.ico.bluefin.bindb.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.model.BinDBDetails;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.repository.BinDBDAO;

@Component
public class TransationBinDBDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(TransationBinDBDetailsService.class);
	
	@Autowired
	private BinDBDAO binDBDAO;
	
	public List<BinDBDetails> resetTransactions(List<SaleTransaction> list) {
		List<String> filteredAndUniqueCardNumberFirst6Char = list.stream().filter(getUniqueTransactionsForBin(SaleTransaction::getCardNumberFirst6Char)).map(SaleTransaction::getCardNumberFirst6Char)
	              .collect(Collectors.toList());
		List<BinDBDetails> contentBinDBs = null;
		if (filteredAndUniqueCardNumberFirst6Char != null && !filteredAndUniqueCardNumberFirst6Char.isEmpty()) {
			contentBinDBs = binDBDAO.fetchDetailsForCardNumbers(filteredAndUniqueCardNumberFirst6Char);
			if (contentBinDBs != null && !contentBinDBs.isEmpty()) {
				Map<String, Long> bindDBMap = contentBinDBs.stream().collect(Collectors.toMap(BinDBDetails::getBin, BinDBDetails::getBinDBId));
				if (bindDBMap != null && !bindDBMap.isEmpty()) {
					list.forEach(saleObj -> saleObj.setBinDBId(bindDBMap.get(saleObj.getCardNumberFirst6Char())));
				}
			}
		}
		return contentBinDBs;
	}
	
	private <T> Predicate<T> getUniqueTransactionsForBin(Function<? super T, ?> keyExtractor){
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
}
