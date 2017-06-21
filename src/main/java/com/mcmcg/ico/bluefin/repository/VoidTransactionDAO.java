package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.VoidTransaction;

@FunctionalInterface
public interface VoidTransactionDAO {
	VoidTransaction findByApplicationTransactionId(final String transactionId);
}
