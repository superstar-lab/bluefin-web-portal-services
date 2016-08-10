package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;
import com.mcmcg.ico.bluefin.persistent.TransactionType;

public interface InternalStatusCodeRepository extends JpaRepository<InternalStatusCode, Long> {

    public InternalStatusCode findByInternalStatusCodeAndTransactionType(String internalStatusCode,
            TransactionType transactionType);
}
