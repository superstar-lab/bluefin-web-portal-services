package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

// Associated Table : InternalStatusCode_Lookup
public class InternalStatusCode implements Serializable {

	private static final long serialVersionUID = 3424245887382516210L;
	
	// Associated Column : InternalStatusCodeID , PK column , Auto Generated.
	private Long internalStatusCodeId;
	// Associated Column : InternalStatusCode
	private String internalStatusCodeValue;
	// Associated Column : InternalStatusCodeDescription
	private String internalStatusCodeDescription;
	// Associated Column : ModifiedBy
	private String lastModifiedBy;
	// Associated Column : InternalStatusCategoryAbbr
	private String internalStatusCategoryAbbr;
	// Associated Column : InternalStatusCategory
	private String internalStatusCategory;
	// Associated Column : DatedModified
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime modifiedDate;
	// Associated Column : TransactionType
	private String transactionTypeName;
	// Associated Column : DateCreated
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime createdDate;
	
	private List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes;
	
	public Long getInternalStatusCodeId() {
		return internalStatusCodeId;
	}
	
	public void setInternalStatusCodeId(Long internalStatusCodeId) {
		this.internalStatusCodeId = internalStatusCodeId;
	}
	public String getInternalStatusCodeValue() {
	
		return internalStatusCodeValue;
	}
	
	public void setInternalStatusCode(String internalStatusCode) {
		this.internalStatusCodeValue = internalStatusCode;
	}
	
	public String getInternalStatusCodeDescription() {
		return internalStatusCodeDescription;
	}
	
	public void setInternalStatusCodeDescription(String internalStatusCodeDescription) {
		this.internalStatusCodeDescription = internalStatusCodeDescription;
	}
	
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	
	public String getInternalStatusCategoryAbbr() {
		return internalStatusCategoryAbbr;
	}
	
	public void setInternalStatusCategoryAbbr(String internalStatusCategoryAbbr) {
		this.internalStatusCategoryAbbr = internalStatusCategoryAbbr;
	}
	
	public String getInternalStatusCategory() {
		return internalStatusCategory;
	}
	
	public void setInternalStatusCategory(String internalStatusCategory) {
		this.internalStatusCategory = internalStatusCategory;
	}
	
	public DateTime getModifiedDate() {
		return modifiedDate;
	}
	
	public void setModifiedDate(DateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public String getTransactionTypeName() {
		return transactionTypeName;
	}
	
	public void setTransactionTypeName(String transactionTypeName) {
		this.transactionTypeName = transactionTypeName;
	}
	
	public DateTime getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}
	
	public List<PaymentProcessorInternalStatusCode> getPaymentProcessorInternalStatusCodes() {
		if (paymentProcessorInternalStatusCodes == null) {
			paymentProcessorInternalStatusCodes = new ArrayList<>();
		}
		return paymentProcessorInternalStatusCodes;
	}

	public void setPaymentProcessorInternalStatusCodes(
			List<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes) {
		this.paymentProcessorInternalStatusCodes = paymentProcessorInternalStatusCodes;
	}
	
	@Override
	public String toString() {
		return "Id="+this.internalStatusCodeId + " , Code="+this.internalStatusCodeValue + " , Desc="+this.internalStatusCodeDescription+
				" , Category="+this.internalStatusCategory + " , category_abbr="+this.internalStatusCategoryAbbr +
				" , No Of Childs="+this.getPaymentProcessorInternalStatusCodes().size();
	}
}
