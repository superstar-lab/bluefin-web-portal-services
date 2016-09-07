package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;

public interface PaymentProcessorRemittanceRepository extends JpaRepository<PaymentProcessorRemittance, Long>, QueryDslPredicateExecutor<PaymentProcessorRemittance> {
}
