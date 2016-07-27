package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.InternalResponseCode;

public interface InternalResponseCodeRepository extends JpaRepository<InternalResponseCode, Long> {

    public InternalResponseCode findByInternalResponseCode(String internalResponseCode);

}
