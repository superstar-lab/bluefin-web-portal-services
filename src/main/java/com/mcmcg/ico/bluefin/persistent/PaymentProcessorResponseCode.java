package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessorResponseCode_Lookup")
@Where(clause = "DeletedFlag=0")
public class PaymentProcessorResponseCode implements Serializable {

    private static final long serialVersionUID = -4612223418828597035L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorResponseCodeID")
    private Long paymentProcessorResponseCodeId;

    @Column(name = "PaymentProcessorResponseCode")
    private String paymentProcessorResponseCode;

    @Column(name = "PaymentProcessorResponseCodeDescription")
    private String paymentProcessorResponseCodeDescription;

    @JsonIgnore
    @OneToMany(mappedBy = "paymentProcessorResponseCode", fetch = FetchType.LAZY)
    private Collection<PaymentProcessorInternalResponseCode> internalResponseCode;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    @JsonIgnore
    @JoinColumn(name = "DeletedFlag")
    private Short deletedFlag = 0;

    @JsonProperty("processorId")
    private Long getProcessorId() {
        return this.paymentProcessor.getPaymentProcessorId();
    }

    @JsonProperty("processorName")
    private String getProcessoName() {
        return this.paymentProcessor.getProcessorName();
    }
}
