package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "paymentProcessorInternalResponseCodes" })
@ToString(exclude = { "paymentProcessorInternalResponseCodes" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "InternalResponseCode_Lookup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "internalResponseCodeId")
public class InternalResponseCode implements Serializable {

    private static final long serialVersionUID = 6473941024724065216L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "InternalResponseCodeID")
    private Long internalResponseCodeId;

    @Column(name = "InternalResponseCode", unique = true)
    private String internalResponseCode;

    @Column(name = "InternalResponseCodeDescription")
    private String internalResponseCodeDescription;

    @OneToMany(mappedBy = "internalResponseCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "ModifiedBy")
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateModified", insertable = false, updatable = false)
    private DateTime modifiedDate;

    @Column(name = "TransactionType")
    private String transactionTypeName; 

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;
    
    public void addPaymentProcessorInternalResponseCode(PaymentProcessorInternalResponseCode paymentProcessorInternalResponseCode) {
        if (paymentProcessorInternalResponseCode == null) {
            this.paymentProcessorInternalResponseCodes = new HashSet<PaymentProcessorInternalResponseCode>();
        }

        paymentProcessorInternalResponseCode.setInternalResponseCode(this);
        paymentProcessorInternalResponseCodes.add(paymentProcessorInternalResponseCode);
    }
}
