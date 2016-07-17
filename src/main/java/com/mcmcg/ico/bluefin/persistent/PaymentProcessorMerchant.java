package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_Merchant")
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

    @JsonBackReference(value = "legalEntityApp")
    @ManyToOne
    @JoinColumn(name = "LegalEntityAppID")
    private LegalEntityApp legalEntityApp;

    @ManyToOne
    @JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

}
