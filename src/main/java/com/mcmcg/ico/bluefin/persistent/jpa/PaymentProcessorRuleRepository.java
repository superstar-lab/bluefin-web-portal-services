package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRule;

public interface PaymentProcessorRuleRepository
        extends JpaRepository<PaymentProcessorRule, Long>, QueryDslPredicateExecutor<PaymentProcessorRule> {
    public List<PaymentProcessorRule> findByPaymentProcessor(PaymentProcessor paymentProcessor);
}
