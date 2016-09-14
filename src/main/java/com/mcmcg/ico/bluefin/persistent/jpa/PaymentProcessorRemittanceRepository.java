package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.SaleTransaction;

public interface PaymentProcessorRemittanceRepository extends JpaRepository<SaleTransaction, Long>, QueryDslPredicateExecutor<SaleTransaction>, PaymentProcessorRemittanceRepositoryCustom {
}