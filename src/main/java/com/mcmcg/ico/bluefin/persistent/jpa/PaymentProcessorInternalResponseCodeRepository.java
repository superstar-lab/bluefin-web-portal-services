package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalResponseCode;

public interface PaymentProcessorInternalResponseCodeRepository
        extends JpaRepository<PaymentProcessorInternalResponseCode, Integer> {

}
