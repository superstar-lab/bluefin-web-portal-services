package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.model.Merchant;
import com.mcmcg.ico.bluefin.repository.MerchantDAO;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorCodeService.class);

    @Autowired
    public MerchantDAO merchantDAO;

    public List<Merchant> findByProcessorId(Long paymentProcessorID) {
        return merchantDAO.findByProcessorId(paymentProcessorID);
    }

    public List<String> findAllMerchantsExtCreditDebit(String search){
        return merchantDAO.findAllMerchantsExtCreditDebit(search);
    }

    public boolean save(List<Merchant> merchants) {
        boolean result = false;
        for(final Merchant merch : merchants) {
            if(merch.getPaymentProcessorMerchantID() == 0) {
                result = merchantDAO.save(merch);
            }else {
                result = merchantDAO.update(merch);
            }

            if(result==false){
                break;
            }
        }
        return result;
    }
}
