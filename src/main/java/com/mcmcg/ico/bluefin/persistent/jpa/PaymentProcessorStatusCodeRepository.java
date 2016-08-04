package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorStatusCode;

public interface PaymentProcessorStatusCodeRepository extends JpaRepository<PaymentProcessorStatusCode, Long> {

    public PaymentProcessorStatusCode findByPaymentProcessorStatusCode(String paymentProcessorStatusCode);

    public List<PaymentProcessorStatusCode> findByPaymentProcessor(PaymentProcessor paymentProcessor);
}
