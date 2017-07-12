package com.mcmcg.ico.bluefin.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcmcg.ico.bluefin.model.OriginPaymentFrequency;
import com.mcmcg.ico.bluefin.repository.OriginPaymentFrequencyDAO;

@Service
@Transactional
public class OriginPaymentFrequencyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OriginPaymentFrequencyService.class);

	@Autowired
	private OriginPaymentFrequencyDAO originPaymentFrequencyDAO;

	public List<OriginPaymentFrequency> getOriginPaymentFrequencies() {
		LOGGER.info("Getting Origin Payment Frequency list.");
		return originPaymentFrequencyDAO.findAll();
	}
}
