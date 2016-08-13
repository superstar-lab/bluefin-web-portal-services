package com.mcmcg.ico.bluefin.persistent.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;

public interface InternalStatusCodeRepository extends JpaRepository<InternalStatusCode, Long> {

    public InternalStatusCode findByInternalStatusCodeAndTransactionTypeName(String internalStatusCode,
            String transactionTypeName);
    
    public List<InternalStatusCode> findByTransactionTypeName(String transactionTypeName);

}
