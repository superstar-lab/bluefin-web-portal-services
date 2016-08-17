package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "paymentProcessorMerchants", "paymentProcessorRules" })
@ToString(exclude = { "paymentProcessorMerchants", "paymentProcessorRules" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PaymentProcessor_Lookup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorId")
public class PaymentProcessor implements Serializable {
    private static final long serialVersionUID = 655003466748410661L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorID")
    private Long paymentProcessorId;

    @Column(name = "ProcessorName")
    private String processorName;

    @OneToMany(mappedBy = "paymentProcessor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

    @OneToMany(mappedBy = "paymentProcessor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorRule> paymentProcessorRules;

    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "ModifiedBy", referencedColumnName = "username")
    @LastModifiedBy
    private User lastModifiedBy;

    @Column(name = "IsActive")
    private Short isActive = 0;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DatedModified", insertable = false, updatable = false)
    private Date modifiedDate;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
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

    @JsonIgnore
    public boolean isActive() {
        return isActive.equals((short) 1) ? true : false;
    }

    @JsonIgnore
    public boolean hasMerchantsAssociated() {
        return paymentProcessorMerchants == null || paymentProcessorMerchants.isEmpty() ? false : true;
    }
}
