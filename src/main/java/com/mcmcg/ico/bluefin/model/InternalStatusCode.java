package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

// Associated Table : InternalStatusCode_Lookup
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "internalStatusCodeId")
public class InternalStatusCode implements Serializable {

	private static final long serialVersionUID = 3424245887382516210L;
	
	// Associated Column : InternalStatusCodeID , PK column , Auto Generated.
	private Long internalStatusCodeId;
	// Associated Column : InternalStatusCode
	@JsonProperty("internalStatusCode")
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

	public void addPaymentProcessorInternalStatusCode(PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode) {
        if (paymentProcessorInternalStatusCode == null) {
            this.paymentProcessorInternalStatusCodes = new ArrayList<>();
        }
        if (paymentProcessorInternalStatusCode != null) {
        	paymentProcessorInternalStatusCode.setInternalStatusCode(this); 
        }
        paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);
    }
	
	@Override
	public String toString() {
		return "Id="+this.internalStatusCodeId + " , Code="+this.internalStatusCodeValue + " , Desc="+this.internalStatusCodeDescription+
				" , Category="+this.internalStatusCategory + " , category_abbr="+this.internalStatusCategoryAbbr +
				" , No Of Childs="+this.getPaymentProcessorInternalStatusCodes().size();
	}
}
