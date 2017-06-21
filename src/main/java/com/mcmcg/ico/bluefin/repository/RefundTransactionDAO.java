package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.RefundTransaction;
@FunctionalInterface
public interface RefundTransactionDAO {
	RefundTransaction findByApplicationTransactionId(final String transactionId);
}
