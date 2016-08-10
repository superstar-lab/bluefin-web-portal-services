package com.mcmcg.ico.bluefin.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.TransactionType;
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionTypeRepository;

@Service
@Transactional
public class TransactionTypeService {

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    public List<TransactionType> getTransactionTypes() {
        return transactionTypeRepository.findAll();
    }
    
    public TransactionType getTransactionTypeById(Long transactionTypeId) {
        return transactionTypeRepository.findOne(transactionTypeId);
    }
}
