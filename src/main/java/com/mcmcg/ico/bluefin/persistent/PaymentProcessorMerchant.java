package com.mcmcg.ico.bluefin.persistent;

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

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_Merchant")
public class PaymentProcessorMerchant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorMerchantID")
    private long paymentProcessorMechantId;

    @ManyToOne
    @JoinColumn(name = "LegalEntityAppID")
    private LegalEntityApp legalEntityApp;
    @ManyToOne
    @JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    @Column(name = "RoutingKey")
    private String routingKey;
    @Column(name = "TestOrProd")
    private Short testOrProd;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    private Date createdDate;

}
