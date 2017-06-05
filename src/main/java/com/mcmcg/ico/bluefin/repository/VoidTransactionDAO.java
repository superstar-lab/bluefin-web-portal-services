package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.VoidTransaction;

public interface VoidTransactionDAO {
	VoidTransaction findByApplicationTransactionId(final String transactionId);
}
