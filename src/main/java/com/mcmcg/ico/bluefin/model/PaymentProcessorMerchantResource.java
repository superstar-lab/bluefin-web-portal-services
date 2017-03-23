package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class PaymentProcessorMerchantResource implements Serializable {
    private static final long serialVersionUID = -6212668449240343946L;

    @NotNull(message = "Please provide a legal entity app for the payment processor merchant")
    private Long legalEntityAppId;
    @NotBlank(message = "Please provide a merchant id for the payment processor merchant")
    private String merchantId;
    @NotNull(message = "Please provide a testOrProd flag for the payment processor merchant")
    private Short testOrProd;

    public com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant toPaymentProcessorMerchant() {
    	com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant paymentProcessorMerchant = new com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant();
        //paymentProcessorMerchant.setLegalEntityApp(new LegalEntityApp(legalEntityAppId));
        paymentProcessorMerchant.setMerchantId(merchantId);
        paymentProcessorMerchant.setTestOrProd(testOrProd);

        return paymentProcessorMerchant;
    }
}
