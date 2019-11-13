package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRuleTrendsRequest;

public interface PaymentProcessorRuleTrendsDAO {

	List<PaymentProcessorRule> getTrendsByFrequencyandDateRange(
			PaymentProcessorRuleTrendsRequest paymentProcessorRuleTrendsRequest);

}
