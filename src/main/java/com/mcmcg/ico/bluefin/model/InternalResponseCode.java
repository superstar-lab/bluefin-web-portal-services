package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "internalResponseCodeId")
public class InternalResponseCode extends Common implements Serializable {

    private static final long serialVersionUID = 6473941024724065216L;

    private Long internalResponseCodeId;
    @JsonProperty("internalResponseCode")
    private String internalResponseCodeValue;
    private String internalResponseCodeDescription;
    private Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes;

    @JsonIgnore
    private String lastModifiedBy;
    private String transactionTypeName; 

    public void addPaymentProcessorInternalResponseCode(PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode) {
        if (paymentProcessorInternalResponseCode == null) {
            this.paymentProcessorInternalResponseCodes = new HashSet<>();
        }
        if (paymentProcessorInternalResponseCode != null) {
        	paymentProcessorInternalResponseCode.setInternalResponseCode(this); 
        }
        paymentProcessorInternalResponseCodes.add(paymentProcessorInternalResponseCode);
    }

	public Long getInternalResponseCodeId() {
		return internalResponseCodeId;
	}

	public void setInternalResponseCodeId(Long internalResponseCodeId) {
		this.internalResponseCodeId = internalResponseCodeId;
	}

	public String getInternalResponseCodeValue() {
		return internalResponseCodeValue;
	}

	public void setInternalResponseCodeValue(String internalResponseCodeValue) {
		this.internalResponseCodeValue = internalResponseCodeValue;
	}

	public String getInternalResponseCodeDescription() {
		return internalResponseCodeDescription;
	}

	public void setInternalResponseCodeDescription(String internalResponseCodeDescription) {
		this.internalResponseCodeDescription = internalResponseCodeDescription;
	}

	public Collection<PaymentProcessorInternalResponseCode> getPaymentProcessorInternalResponseCodes() {
		return paymentProcessorInternalResponseCodes;
	}

	public void setPaymentProcessorInternalResponseCodes(
			Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes) {
		this.paymentProcessorInternalResponseCodes = paymentProcessorInternalResponseCodes;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getTransactionTypeName() {
		return transactionTypeName;
	}

	public void setTransactionTypeName(String transactionTypeName) {
		this.transactionTypeName = transactionTypeName;
	}
    
    
}
