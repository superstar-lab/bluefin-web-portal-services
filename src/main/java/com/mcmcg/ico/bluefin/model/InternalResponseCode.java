package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "internalResponseCodeId")
public class InternalResponseCode implements Serializable {

    private static final long serialVersionUID = 6473941024724065216L;

    private Long internalResponseCodeId;
    @JsonProperty("internalResponseCode")
    private String internalResponseCodeValue;

    private String internalResponseCodeDescription;

    private Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes;

    @JsonIgnore
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime modifiedDate;

    private String transactionTypeName; 

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;
    
    public void addPaymentProcessorInternalResponseCode(PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode) {
        if (paymentProcessorInternalResponseCode == null) {
            this.paymentProcessorInternalResponseCodes = new HashSet<>();
        }
        if (paymentProcessorInternalResponseCode != null) {
        	paymentProcessorInternalResponseCode.setInternalResponseCode(this); 
        }
        paymentProcessorInternalResponseCodes.add(paymentProcessorInternalResponseCode);
    }
}
