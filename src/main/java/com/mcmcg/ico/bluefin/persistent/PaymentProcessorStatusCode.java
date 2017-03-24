package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "internalStatusCode" })
@ToString(exclude = { "internalStatusCode" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PaymentProcessorStatusCode_Lookup")
public class PaymentProcessorStatusCode implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorStatusCodeID")
    private Long paymentProcessorStatusCodeId;

    @Column(name = "PaymentProcessorStatusCode")
    private String paymentProcessorStatusCode;

    @Column(name = "PaymentProcessorStatusDescription")
    private String paymentProcessorStatusCodeDescription;

    @JsonIgnore
    @OneToMany(mappedBy = "paymentProcessorStatusCode", fetch = FetchType.LAZY)
    private Collection<PaymentProcessorInternalStatusCode> internalStatusCode;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "ModifiedBy")
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DatedModified", insertable = false, updatable = false)
    private DateTime modifiedDate;

    @Column(name = "TransactionType")
    private String transactionTypeName;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

    @JsonProperty("processorId")
    private Long getProcessorId() {
        return this.paymentProcessor.getPaymentProcessorId();
    }

    @JsonProperty("processorName")
    private String getProcessoName() {
        return this.paymentProcessor.getProcessorName();
    }

//    public com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode convertPersistentObjectToModelObject(){
//    	com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode objToReturn = new com.mcmcg.ico.bluefin.model.PaymentProcessorStatusCode();
//    	objToReturn.setPaymentProcessorStatusCodeId(getPaymentProcessorStatusCodeId());
//    	objToReturn.setPaymentProcessorStatusCode(getPaymentProcessorStatusCode());
//    	objToReturn.setPaymentProcessorStatusCodeDescription(getPaymentProcessorStatusCodeDescription());
//    	objToReturn.setTransactionTypeName(getTransactionTypeName());
//    	return objToReturn;
//    }
}
