package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;

public interface PaymentProcessorResponseCodeRepository extends JpaRepository<PaymentProcessorResponseCode, Long> {

    public PaymentProcessorResponseCode findByPaymentProcessorResponseCode(String paymentProcessorResponseCode);
}
