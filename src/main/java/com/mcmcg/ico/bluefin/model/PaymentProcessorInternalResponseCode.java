package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorInternalResponseCodeId")
public class PaymentProcessorInternalResponseCode implements Serializable {

    private static final long serialVersionUID = -8456355468578883069L;

    private Long paymentProcessorInternalResponseCodeId;

    private Long internalResponseCodeId;
    private InternalResponseCode internalResponseCode;

    private Long paymentProcessorResponseCodeId;
    private PaymentProcessorResponseCode paymentProcessorResponseCode;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private DateTime createdDate;

    @JsonIgnore
    private String lastModifiedBy;

}