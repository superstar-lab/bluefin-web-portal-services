package com.mcmcg.ico.bluefin.rest.resource;

import org.hibernate.validator.constraints.NotEmpty;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

import lombok.Data;

@Data
public class PaymentProcessorResource {
    @NotEmpty(message = "processorName must not be empty")
    private String processorName;
    @NotEmpty(message = "cardType must not be empty")
    private String cardType;

    public PaymentProcessor toPaymentProcessor() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setProcessorName(processorName);
        paymentProcessor.setCardType(cardType);
        return paymentProcessor;
    }

    public PaymentProcessor updatePaymentProcessor(PaymentProcessor paymentProcessor) {
        paymentProcessor.setProcessorName(processorName);
        paymentProcessor.setCardType(cardType);
        return paymentProcessor;
    }
}
