package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.OriginPaymentFrequency;
import com.mcmcg.ico.bluefin.persistent.jpa.OriginPaymentFrequencyRepository;

@Service
@Transactional
public class OriginPaymentFrequencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OriginPaymentFrequencyService.class);

    @Autowired
    private OriginPaymentFrequencyRepository originPaymentFrequencyRepository;

    public List<OriginPaymentFrequency> getOriginPaymentFrequencies() {
        LOGGER.info("Getting Origin Payment Frequency list.");
        return originPaymentFrequencyRepository.findAll();
    }

}
