package com.mcmcg.ico.bluefin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.ws.client.PaymentRequestorWsConsumer;
import com.mcmcg.paymentprocessor.ACH1;
import com.mcmcg.paymentprocessor.CreditCard;
import com.mcmcg.paymentprocessor.PaymentProcessingRequest;

@Service
public class TransactionsService {

    @Autowired
    PaymentRequestorWsConsumer paymentRequestorWsConsumer;

    public void payment(CreditCard creditCard, ACH1 ach1) {
        PaymentProcessingRequest ppr = new PaymentProcessingRequest();
        ppr.setCreditCard(creditCard);
        ppr.setACH1(ach1);
        paymentRequestorWsConsumer.paymentRequest(ppr);
    }

}
