package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorStatusCode;

public interface PaymentProcessorStatusCodeRepository extends JpaRepository<PaymentProcessorStatusCode, Long> {

    public PaymentProcessorStatusCode findByPaymentProcessorStatusCodeAndTransactionTypeNameAndPaymentProcessor(
            String paymentProcessorStatusCode, String transactionTypeName, PaymentProcessor paymentProcessor);
}
