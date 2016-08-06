package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.VoidTransaction;

public interface VoidTransactionRepository extends JpaRepository<VoidTransaction, Long> {
    public VoidTransaction findByApplicationTransactionId(final String transactionId);

}