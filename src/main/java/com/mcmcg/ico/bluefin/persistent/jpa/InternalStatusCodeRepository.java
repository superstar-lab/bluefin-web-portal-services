package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;

public interface InternalStatusCodeRepository extends JpaRepository<InternalStatusCode, Long> {

    public InternalStatusCode findByInternalStatusCode(String internalStatusCode);
}
