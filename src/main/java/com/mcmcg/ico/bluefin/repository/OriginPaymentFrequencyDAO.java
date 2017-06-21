package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.OriginPaymentFrequency;
@FunctionalInterface
public interface OriginPaymentFrequencyDAO {
	List<OriginPaymentFrequency> findAll();
}
