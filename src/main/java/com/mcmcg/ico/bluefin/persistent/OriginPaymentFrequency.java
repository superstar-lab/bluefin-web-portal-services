package com.mcmcg.ico.bluefin.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "OriginPaymentFrequency_Lookup")
public class OriginPaymentFrequency {

    @Id
    @Column(name = "OriginPaymentFrequencyID")
    private long originPaymentFrequencyId;
    @Column(name = "Origin")
    private String origin;
    @Column(name = "PaymentFrequency")
    private String paymentFrequency;
}
