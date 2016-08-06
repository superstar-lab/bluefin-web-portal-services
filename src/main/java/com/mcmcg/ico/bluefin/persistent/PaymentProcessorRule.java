package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mcmcg.ico.bluefin.model.CardType;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_Rule")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "paymentProcessorRuleId")
public class PaymentProcessorRule implements Serializable {
    private static final long serialVersionUID = 255255719776828551L;

    @PreUpdate
    @PrePersist
    public void beforePersist() {
        if (hasNoLimit()) {
            this.maximumMonthlyAmount = BigDecimal.ZERO;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorRuleID")
    private Long paymentProcessorRuleId;

    @JsonProperty(value = "paymentProcessorId")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "PaymentProcessorID")
    private PaymentProcessor paymentProcessor;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "CardType")
    private CardType cardType;

    @Column(name = "MaximumMonthlyAmount", columnDefinition = "money")
    private BigDecimal maximumMonthlyAmount = BigDecimal.ZERO;

    @Column(name = "NoMaximumMonthlyAmountFlag")
    private Short noMaximumMonthlyAmountFlag = (short) 0;

    @Column(name = "Priority")
    private Short priority;

    @Column(name = "MonthToDateCumulativeAmount", columnDefinition = "money")
    private BigDecimal monthToDateCumulativeAmount;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    public boolean hasNoLimit() {
        return noMaximumMonthlyAmountFlag.equals((short) 1);
    }
}
