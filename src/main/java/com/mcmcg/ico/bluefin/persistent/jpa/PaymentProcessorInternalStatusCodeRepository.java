package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorInternalStatusCode;

public interface PaymentProcessorInternalStatusCodeRepository
        extends JpaRepository<PaymentProcessorInternalStatusCode, Long> {

}
