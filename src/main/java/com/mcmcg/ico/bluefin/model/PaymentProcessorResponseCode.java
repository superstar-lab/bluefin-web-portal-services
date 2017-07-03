package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.ManyToOne;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentProcessorResponseCode implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    private Long paymentProcessorResponseCodeId;
    @JsonProperty("paymentProcessorResponseCode")
    private String paymentProcessorResponseCodeValue;

    private String paymentProcessorResponseCodeDescription;

    @JsonIgnore
    private Collection<PaymentProcessorInternalResponseCode> internalResponseCode;

    @ManyToOne
    @JsonIgnore
    private com.mcmcg.ico.bluefin.model.PaymentProcessor paymentProcessor;

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

    @JsonProperty("processorId")
    public Long getProcessorId() {
        return this.paymentProcessor.getPaymentProcessorId();
    }

    @JsonProperty("processorName")
    public String getProcessoName() {
        return this.paymentProcessor.getProcessorName();
    }

}
