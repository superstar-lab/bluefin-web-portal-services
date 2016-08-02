package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorResponseCode;

public interface PaymentProcessorResponseCodeRepository extends JpaRepository<PaymentProcessorResponseCode, Long> {

    public PaymentProcessorResponseCode findByPaymentProcessorResponseCode(String paymentProcessorResponseCode);

    public List<PaymentProcessorResponseCode> findByPaymentProcessor(PaymentProcessor paymentProcessor);

}
