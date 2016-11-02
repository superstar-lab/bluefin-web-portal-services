package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;
import java.sql.Time;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;

import lombok.Data;

@Data
public class BasicPaymentProcessorResource implements Serializable {
    private static final long serialVersionUID = -602175101416750669L;

    @NotBlank(message = "Please provide a processor name")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field processor name must be alphanumeric")
    private String processorName;
    private Short isActive = 0;
    private Time remitTransactionOpenTime;
    private Time remitTransactionCloseTime;
    
    public PaymentProcessor toPaymentProcessor() {
        PaymentProcessor paymentProcessor = new PaymentProcessor();
        paymentProcessor.setProcessorName(processorName);
        paymentProcessor.setIsActive(isActive);
        paymentProcessor.setRemitTransactionOpenTime(remitTransactionOpenTime);
        paymentProcessor.setRemitTransactionCloseTime(remitTransactionCloseTime);

        return paymentProcessor;
    }
}
