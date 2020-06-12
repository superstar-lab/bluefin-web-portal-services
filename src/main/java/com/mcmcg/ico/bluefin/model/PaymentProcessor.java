package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Collection;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorId")
public class PaymentProcessor implements Serializable {
    private static final long serialVersionUID = 655003466748410661L;

    private Long paymentProcessorId;

    private String processorName;

    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

    private Collection<PaymentProcessorRule> paymentProcessorRules;

    @JsonIgnore
    private String lastModifiedBy;

    private Short isActive = 0;

    private Time remitTransactionOpenTime;

    private Time remitTransactionCloseTime;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime modifiedDate;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private DateTime createdDate;

    private boolean readyToBeActivated;

    public PaymentProcessor() {
    	// Default constructor
    }

    public PaymentProcessor(Long value) {
        this.paymentProcessorId = value;
    }

    public void addPaymentProcessorMerchant(PaymentProcessorMerchant paymentProcessorMerchant) {

        if (paymentProcessorMerchant != null) {
        	
        	this.paymentProcessorMerchants = new HashSet<>();
        	paymentProcessorMerchant.setPaymentProcessorId(this.paymentProcessorId);
        	paymentProcessorMerchants.add(paymentProcessorMerchant);
        }
        
    }

    @JsonIgnore
    public boolean isActive() {
    	boolean activeState = false;
    	
    	if(isActive.equals((short) 1)) {
    		activeState = true;
    	}
    	
        return activeState;
    }

    @JsonIgnore
    public boolean hasMerchantsAssociated() {
    	boolean merchantsAssociated = true;
    	
        if(paymentProcessorMerchants == null || paymentProcessorMerchants.isEmpty() ) {
        	merchantsAssociated = false;
        }
        
        return merchantsAssociated;
    }

    @JsonIgnore
    public boolean hasRulesAssociated() {
    	boolean rulesAssociated = true;
    	
        if( paymentProcessorRules == null || paymentProcessorRules.isEmpty() ) {
        	rulesAssociated = false;
        }
        return	rulesAssociated;
    }

    public PaymentProcessor createPaymentProcessor(){
    	PaymentProcessor returnObj = new PaymentProcessor();
    	returnObj.setPaymentProcessorId(this.getPaymentProcessorId());
    	return returnObj;
    }

	public Long getPaymentProcessorId() {
		return paymentProcessorId;
	}

	public void setPaymentProcessorId(Long paymentProcessorId) {
		this.paymentProcessorId = paymentProcessorId;
	}

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	public Collection<PaymentProcessorMerchant> getPaymentProcessorMerchants() {
		return paymentProcessorMerchants;
	}

	public void setPaymentProcessorMerchants(Collection<PaymentProcessorMerchant> paymentProcessorMerchants) {
		this.paymentProcessorMerchants = paymentProcessorMerchants;
	}

	public Collection<PaymentProcessorRule> getPaymentProcessorRules() {
		return paymentProcessorRules;
	}

	public void setPaymentProcessorRules(Collection<PaymentProcessorRule> paymentProcessorRules) {
		this.paymentProcessorRules = paymentProcessorRules;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Short getIsActive() {
		return isActive;
	}

	public void setIsActive(Short isActive) {
		this.isActive = isActive;
	}

	public Time getRemitTransactionOpenTime() {
		return remitTransactionOpenTime;
	}

	public void setRemitTransactionOpenTime(Time remitTransactionOpenTime) {
		this.remitTransactionOpenTime = remitTransactionOpenTime;
	}

	public Time getRemitTransactionCloseTime() {
		return remitTransactionCloseTime;
	}

	public void setRemitTransactionCloseTime(Time remitTransactionCloseTime) {
		this.remitTransactionCloseTime = remitTransactionCloseTime;
	}

	public DateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(DateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}

	public boolean isReadyToBeActivated() {
		return readyToBeActivated;
	}

	public void setReadyToBeActivated(boolean readyToBeActivated) {
		this.readyToBeActivated = readyToBeActivated;
	}
    
    
}
