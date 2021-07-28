package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorMerchantId")
public class PaymentProcessorMerchant implements Serializable {
    private static final long serialVersionUID = 3038512746750300442L;

    private Long paymentProcessorMerchantId;

    private String merchantId_Debit;

	private String merchantId_Credit;

    private Short testOrProd;
    
    private Long legalEntityAppId;
    
    private Long paymentProcessorId;

    @JsonIgnore
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime modifiedDate;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;

	public Long getPaymentProcessorMerchantId() {
		return paymentProcessorMerchantId;
	}

	public void setPaymentProcessorMerchantId(Long paymentProcessorMerchantId) {
		this.paymentProcessorMerchantId = paymentProcessorMerchantId;
	}

	public String getMerchantIdDebit() {
		return merchantId_Debit;
	}

	public String getMerchantIdCredit() {
		return merchantId_Credit;
	}

	public void setMerchantIdDebit(String merchantId) {
		this.merchantId_Debit = merchantId;
	}

	public void setMerchantIdCredit(String merchantId) {
		this.merchantId_Credit = merchantId;
	}

	public Short getTestOrProd() {
		return testOrProd;
	}

	public void setTestOrProd(Short testOrProd) {
		this.testOrProd = testOrProd;
	}

	public Long getLegalEntityAppId() {
		return legalEntityAppId;
	}

	public void setLegalEntityAppId(Long legalEntityAppId) {
		this.legalEntityAppId = legalEntityAppId;
	}

	public Long getPaymentProcessorId() {
		return paymentProcessorId;
	}

	public void setPaymentProcessorId(Long paymentProcessorId) {
		this.paymentProcessorId = paymentProcessorId;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
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

    
}
