package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorStatusCode;
import com.mcmcg.ico.bluefin.persistent.TransactionType;

public interface PaymentProcessorStatusCodeRepository extends JpaRepository<PaymentProcessorStatusCode, Long> {

    public PaymentProcessorStatusCode findByPaymentProcessorStatusCodeAndTransactionType(
            String paymentProcessorStatusCode, TransactionType transactionType);

    public List<PaymentProcessorStatusCode> findByPaymentProcessorAndTransactionType(PaymentProcessor paymentProcessor,
            TransactionType transactionType);
}
