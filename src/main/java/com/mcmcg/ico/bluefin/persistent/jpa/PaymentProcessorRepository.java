package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

public interface PaymentProcessorRepository
        extends JpaRepository<PaymentProcessor, Long>, QueryDslPredicateExecutor<PaymentProcessor> {

    public PaymentProcessor getPaymentProcessorByPaymentProcessorId(Long id);

    public PaymentProcessor getPaymentProcessorByProcessorName(String processorName);

}
