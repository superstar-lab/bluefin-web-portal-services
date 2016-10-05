package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "paymentProcessorMerchants", "userLegalEntities" })
@ToString(exclude = { "paymentProcessorMerchants", "userLegalEntities" })
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "LegalEntityApp_Lookup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "legalEntityAppId")
public class LegalEntityApp implements Serializable {
    private static final long serialVersionUID = -3826977337542770003L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LegalEntityAppID")
    private Long legalEntityAppId;

    @Column(name = "LegalEntityAppName")
    private String legalEntityAppName;

    @JsonIgnore
    @OneToMany(mappedBy = "legalEntityApp", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PaymentProcessorMerchant> paymentProcessorMerchants;

    @JsonIgnore
    @OneToMany(mappedBy = "legalEntityApp", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserLegalEntity> userLegalEntities;

    @JsonIgnore
    @LastModifiedBy
    @Column(name = "ModifiedBy")
    private String lastModifiedBy;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DatedModified", insertable = false, updatable = false)
    private DateTime modifiedDate;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false, updatable = false)
    private DateTime createdDate;

    @Column(name = "IsActive")
    private Short isActive = 1;

    public LegalEntityApp() {
    }

    public LegalEntityApp(Long value) {
        legalEntityAppId = value;
    }
}
