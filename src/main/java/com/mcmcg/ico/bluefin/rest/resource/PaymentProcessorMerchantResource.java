package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorMerchant;

import lombok.Data;

@Data
public class PaymentProcessorMerchantResource implements Serializable {
    private static final long serialVersionUID = -6212668449240343946L;

    @NotNull(message = "Legal entity app cannot be empty or null")
    private Long legalEntityAppId;
    @NotBlank(message = "Merchant id cannot be empty or null")
    private String merchantId;
    @NotNull(message = "testOrProd cannot be empty or null")
    private Short testOrProd;

    public PaymentProcessorMerchant toPaymentProcessorMerchant() {
        PaymentProcessorMerchant paymentProcessorMerchant = new PaymentProcessorMerchant();
        paymentProcessorMerchant.setLegalEntityApp(new LegalEntityApp(legalEntityAppId));
        paymentProcessorMerchant.setMerchantId(merchantId);
        paymentProcessorMerchant.setTestOrProd(testOrProd);

        return paymentProcessorMerchant;
    }
}
