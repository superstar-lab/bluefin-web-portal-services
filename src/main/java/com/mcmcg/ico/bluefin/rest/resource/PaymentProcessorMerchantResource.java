package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant;

public class PaymentProcessorMerchantResource implements Serializable {
    private static final long serialVersionUID = -6212668449240343946L;

    @NotNull(message = "Please provide a legal entity app for the payment processor merchant")
    private Long legalEntityAppId;
    @NotBlank(message = "Please provide a merchant id for the payment processor merchant")
    private String merchantId;
    @NotNull(message = "Please provide a testOrProd flag for the payment processor merchant")
    private Short testOrProd;

    public PaymentProcessorMerchant toPaymentProcessorMerchant() {
        PaymentProcessorMerchant paymentProcessorMerchant = new PaymentProcessorMerchant();
        paymentProcessorMerchant.setLegalEntityAppId(legalEntityAppId);
        paymentProcessorMerchant.setMerchantId(merchantId);
        paymentProcessorMerchant.setTestOrProd(testOrProd);

        return paymentProcessorMerchant;
    }

	public Long getLegalEntityAppId() {
		return legalEntityAppId;
	}

	public void setLegalEntityAppId(Long legalEntityAppId) {
		this.legalEntityAppId = legalEntityAppId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public Short getTestOrProd() {
		return testOrProd;
	}

	public void setTestOrProd(Short testOrProd) {
		this.testOrProd = testOrProd;
	}
    
    
}
