package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.CascadeType;
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

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "InternalStatusCode_Lookup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "internalStatusCodeId")
public class InternalStatusCode implements Serializable {

    private static final long serialVersionUID = -8966605439938881430L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "InternalStatusCodeID")
    private Long internalStatusCodeId;

    @Column(name = "InternalStatusCode", unique = true)
    private String internalStatusCode;

    @Column(name = "InternalStatusCodeDescription")
    private String internalStatusCodeDescription;

    @OneToMany(mappedBy = "internalStatusCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes;

    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "ModifiedBy", referencedColumnName = "username")
    @LastModifiedBy
    private User lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DatedModified", insertable = false, updatable = false)
    private Date modifiedDate;

    @Column(name = "TransactionType")
    private String transactionTypeName; 

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private Date createdDate;
    
    public void addPaymentProcessorInternalStatusCode(PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode) {
        if (paymentProcessorInternalStatusCode == null) {
            this.paymentProcessorInternalStatusCodes = new HashSet<PaymentProcessorInternalStatusCode>();
        }

        paymentProcessorInternalStatusCode.setInternalStatusCode(this);
        paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);
    }
}
