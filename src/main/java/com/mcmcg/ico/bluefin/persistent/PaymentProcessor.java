package com.mcmcg.ico.bluefin.persistent;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_Lookup")
public class PaymentProcessor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorID")
    private long paymentProcessorId;

    @Column(name = "ProcessorName")
    private String processorName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated")
    private Date createdDate;

    @OneToMany(mappedBy = "paymentProcessor")
    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

}
