package com.mcmcg.ico.bluefin.persistent;

import java.util.Collection;
import java.util.Date;

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
@Table(name = "legal_entity_app")
public class LegalEntityApp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long legalEntityAppId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;
    private String legalEntityAppName;

    @OneToMany(mappedBy = "legalEntityApp")
    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

    @OneToMany(mappedBy = "legalEntityApp")
    private Collection<UserLegalEntity> userLegalEntities;
}
