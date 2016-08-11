package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.InternalResponseCode;

public interface InternalResponseCodeRepository extends JpaRepository<InternalResponseCode, Long> {

    public InternalResponseCode findByInternalResponseCodeAndTransactionTypeName(String internalResponseCode,
            String transactionTypeName);
    
    public List<InternalResponseCode> findByTransactionTypeName(String transactionTypeName);

}
