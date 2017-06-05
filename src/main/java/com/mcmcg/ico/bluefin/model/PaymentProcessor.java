package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
/*@EqualsAndHashCode(exclude = { "paymentProcessorMerchants", "paymentProcessorRules" })
@ToString(exclude = { "paymentProcessorMerchants", "paymentProcessorRules" })*/
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorId")
public class PaymentProcessor implements Serializable {
    private static final long serialVersionUID = 655003466748410661L;

    private Long paymentProcessorId;

    private String processorName;

    private Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorMerchant> paymentProcessorMerchants;

    private Collection<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> paymentProcessorRules;

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

    @Transient
    private boolean readyToBeActivated;

    public PaymentProcessor() {
    }

    public PaymentProcessor(Long value) {
        this.paymentProcessorId = value;
    }

    public void addPaymentProcessorMerchant(PaymentProcessorMerchant paymentProcessorMerchant) {
        if (paymentProcessorMerchants == null) {
            this.paymentProcessorMerchants = new HashSet<PaymentProcessorMerchant>();
        }

        paymentProcessorMerchant.setPaymentProcessorId(this.paymentProcessorId);
        paymentProcessorMerchants.add(paymentProcessorMerchant);
    }

    @JsonIgnore
    public boolean isActive() {
        return isActive.equals((short) 1) ? true : false;
    }

    @JsonIgnore
    public boolean hasMerchantsAssociated() {
        return paymentProcessorMerchants == null || paymentProcessorMerchants.isEmpty() ? false : true;
    }

    @JsonIgnore
    public boolean hasRulesAssociated() {
        return paymentProcessorRules == null || paymentProcessorRules.isEmpty() ? false : true;
    }

    public com.mcmcg.ico.bluefin.model.PaymentProcessor createPaymentProcessor(){
    	com.mcmcg.ico.bluefin.model.PaymentProcessor returnObj = new com.mcmcg.ico.bluefin.model.PaymentProcessor();
    	returnObj.setPaymentProcessorId(this.getPaymentProcessorId());
    	return returnObj;
    }
}
