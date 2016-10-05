package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PaymentProcessor_Merchant")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorMechantId")
public class PaymentProcessorMerchant implements Serializable {
    private static final long serialVersionUID = 3038512746750300442L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorMerchantID")
    private Long paymentProcessorMechantId;

    @Column(name = "MerchantID")
    private String merchantId;

    @Column(name = "TestOrProd")
    private Short testOrProd;

    @JsonProperty(value = "legalEntityAppId")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "LegalEntityAppID")
    private LegalEntityApp legalEntityApp;

    @JsonProperty(value = "paymentProcessorId")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "ModifiedBy")
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateModified", insertable = false, updatable = false)
    private DateTime modifiedDate;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

}
