package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.RefundTransaction;

public interface RefundTransactionDAO {
	RefundTransaction findByApplicationTransactionId(final String transactionId);
}
