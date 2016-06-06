package com.mcmcg.ico.bluefin.persistent;

import java.util.Collection;
import java.util.Date;

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

import lombok.Data;

@Data
@Entity
@Table(name = "legal_entity_app")
public class LegalEntityApp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) 
    private Integer legalEntityAppId;

    private String legalEntityAppName;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;

    @JsonIgnore
    @OneToMany(mappedBy = "legalEntityApp", fetch = FetchType.LAZY)
    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

    @JsonIgnore
    @OneToMany(mappedBy = "legalEntityApp", fetch = FetchType.LAZY)
    private Collection<UserLegalEntity> userLegalEntities;

}
