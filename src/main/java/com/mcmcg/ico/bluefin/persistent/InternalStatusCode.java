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
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mcmcg.ico.bluefin.rest.resource.Views;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "paymentProcessorInternalStatusCodes" })
@ToString(exclude = { "paymentProcessorInternalStatusCodes" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "InternalStatusCode_Lookup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "internalStatusCodeId")
public class InternalStatusCode implements Serializable {

    private static final long serialVersionUID = -8966605439938881430L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "InternalStatusCodeID")
    @JsonView(Views.ExtendPublic.class)
    private Long internalStatusCodeId;

    @Column(name = "InternalStatusCode", unique = true)
    @JsonView({Views.ExtendPublic.class, Views.Public.class})
    private String internalStatusCode;

    @JsonView({Views.ExtendPublic.class, Views.Public.class})
    @Column(name = "InternalStatusCodeDescription")
    private String internalStatusCodeDescription;

    @JsonView(Views.ExtendPublic.class)
    @OneToMany(mappedBy = "internalStatusCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorInternalStatusCode> paymentProcessorInternalStatusCodes;

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
    @JsonView({Views.ExtendPublic.class, Views.Public.class})
    private String transactionTypeName; 

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;
    
    public void addPaymentProcessorInternalStatusCode(PaymentProcessorInternalStatusCode paymentProcessorInternalStatusCode) {
        if (paymentProcessorInternalStatusCode == null) {
            this.paymentProcessorInternalStatusCodes = new HashSet<PaymentProcessorInternalStatusCode>();
        }

        paymentProcessorInternalStatusCode.setInternalStatusCode(this);
        paymentProcessorInternalStatusCodes.add(paymentProcessorInternalStatusCode);
    }
}
