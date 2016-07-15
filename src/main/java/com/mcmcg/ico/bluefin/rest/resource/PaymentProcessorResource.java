package com.mcmcg.ico.bluefin.rest.resource;

import org.hibernate.validator.constraints.NotEmpty;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

import lombok.Data;

@Data
public class PaymentProcessorResource {
    @NotEmpty(message = "processorName must not be empty")
    private String processorName;

    public PaymentProcessor toPaymentProcessor() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setProcessorName(processorName);
        return paymentProcessor;
    }

    public PaymentProcessor updatePaymentProcessor(PaymentProcessor paymentProcessor) {
        paymentProcessor.setProcessorName(processorName);
        return paymentProcessor;
    }
}
