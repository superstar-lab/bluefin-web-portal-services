package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

import lombok.Data;

@Data
public class BasicPaymentProcessorResource implements Serializable {
    private static final long serialVersionUID = -602175101416750669L;

    @NotBlank(message = "Processor name cannot be null or empty")
    @Pattern(regexp = "^[\\w]*$", message = "Processor name must be alphanumeric")
    private String processorName;
    private Short isActive = 0;

    public PaymentProcessor toPaymentProcessor() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setProcessorName(processorName);
        paymentProcessor.setIsActive(isActive);

        return paymentProcessor;
    }
}
