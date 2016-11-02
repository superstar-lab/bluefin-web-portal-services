package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcmcg.ico.bluefin.persistent.OriginPaymentFrequency;

@Repository
public interface OriginPaymentFrequencyRepository extends JpaRepository<OriginPaymentFrequency, Long> {

}
