package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import javax.persistence.Column;

import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorMechantId")
public class PaymentProcessorMerchant implements Serializable {
    private static final long serialVersionUID = 3038512746750300442L;

    private Long paymentProcessorMechantId;

    private String merchantId;

    private Short testOrProd;

    @JsonProperty(value = "legalEntityAppId")
    @JsonIdentityReference(alwaysAsId = true)
    private LegalEntityApp legalEntityApp;

    @JsonProperty(value = "paymentProcessorId")
    @JsonIdentityReference(alwaysAsId = true)
    private PaymentProcessor paymentProcessor;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "ModifiedBy")
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime modifiedDate;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;

}
