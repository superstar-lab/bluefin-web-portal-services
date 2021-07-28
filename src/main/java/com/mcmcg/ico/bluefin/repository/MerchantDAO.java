package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.Merchant;

import java.util.List;

public interface MerchantDAO {
    List<Merchant> findByProcessorId(Long paymentProcessorID);
    boolean save(Merchant merchant);
    boolean update(Merchant merchant);
}
