package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

//Table : PaymentProcessor_InternalStatusCode
public class PaymentProcessorInternalStatusCode implements Serializable{

	public PaymentProcessorStatusCode getPaymentProcessorStatusCode() {
		return paymentProcessorStatusCode;
	}

	public void setPaymentProcessorStatusCode(PaymentProcessorStatusCode paymentProcessorStatusCode) {
		this.paymentProcessorStatusCode = paymentProcessorStatusCode;
	}

	public Long getInternalStatusCodeId() {
		return internalStatusCodeId;
	}

	public void setInternalStatusCodeId(Long internalStatusCodeId) {
		this.internalStatusCodeId = internalStatusCodeId;
	}

	public Long getPaymentProcessorStatusCodeId() {
		return paymentProcessorStatusCodeId;
	}

	public void setPaymentProcessorStatusCodeId(Long paymentProcessorStatusCodeId) {
		this.paymentProcessorStatusCodeId = paymentProcessorStatusCodeId;
	}

	private static final long serialVersionUID = 743031245411398765L;

	// Column : PaymentProcessorInternalStatusCodeID
    private Long paymentProcessorInternalStatusCodeId;

	// Column : DateCreated
    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;
    
	// Column : ModifiedBy
    @JsonIgnore
    private String lastModifiedBy;
    
    // Column : InternalStatusCodeID
    private Long internalStatusCodeId;
    private InternalStatusCode internalStatusCode;
    

    // Column : PaymentProcessorStatusCodeId
    private Long paymentProcessorStatusCodeId;
    private PaymentProcessorStatusCode paymentProcessorStatusCode;
    
    public Long getPaymentProcessorInternalStatusCodeId() {
		return paymentProcessorInternalStatusCodeId;
	}

	public void setPaymentProcessorInternalStatusCodeId(Long paymentProcessorInternalStatusCodeId) {
		this.paymentProcessorInternalStatusCodeId = paymentProcessorInternalStatusCodeId;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public InternalStatusCode getInternalStatusCode() {
		return internalStatusCode;
	}

	public void setInternalStatusCode(InternalStatusCode internalStatusCode) {
		this.internalStatusCode = internalStatusCode;
	}
	
	@Override
	public String toString() {
		return "Id="+this.paymentProcessorInternalStatusCodeId+" , PaymentProcessorStatusCodeId= "+this.paymentProcessorStatusCodeId +
				", InternalStatusCodeId= " + this.internalStatusCodeId;
	}
}
