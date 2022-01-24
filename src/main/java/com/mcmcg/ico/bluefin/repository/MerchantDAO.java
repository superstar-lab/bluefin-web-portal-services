package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.Merchant;

import java.util.List;

public interface MerchantDAO {
    List<Merchant> findByProcessorId(Long paymentProcessorID);
    List<String> findAllMerchantsExtCreditDebit(String search);
    boolean save(Merchant merchant);
    boolean update(Merchant merchant);
}
