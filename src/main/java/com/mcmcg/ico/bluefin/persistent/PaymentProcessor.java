package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_Lookup")
public class PaymentProcessor implements Serializable {
    private static final long serialVersionUID = 655003466748410661L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorID")
    private Long paymentProcessorId;

    @Column(name = "ProcessorName")
    private String processorName;

    @JsonManagedReference(value = "paymentProcessorMerchant")
    @OneToMany(mappedBy = "paymentProcessor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

    @JsonManagedReference(value = "paymentProcessorRule")
    @OneToMany(mappedBy = "paymentProcessor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorRule> paymentProcessorRules;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    public PaymentProcessor() {
    }

    public PaymentProcessor(Long value) {
        this.paymentProcessorId = value;
    }

    public void addPaymentProcessorMerchant(PaymentProcessorMerchant paymentProcessorMerchant) {
        if (paymentProcessorMerchants == null) {
            this.paymentProcessorMerchants = new HashSet<PaymentProcessorMerchant>();
        }

        paymentProcessorMerchant.setPaymentProcessor(this);
        paymentProcessorMerchants.add(paymentProcessorMerchant);
    }
}
