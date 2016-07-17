package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

import lombok.Data;

@Data
public class PaymentProcessorResource implements Serializable {
    private static final long serialVersionUID = -602175101416750669L;

    @NotBlank(message = "processorName must not be empty")
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
