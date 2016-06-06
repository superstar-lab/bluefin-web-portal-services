package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

public interface PaymentProcessorRepository extends JpaRepository<PaymentProcessor, Integer> {
    
    public PaymentProcessor getPaymentProcessorByPaymentProcessorId(Integer id); 
    
}
