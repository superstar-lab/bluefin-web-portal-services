package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "InternalResponseCode_Lookup")
@Where(clause="DeletedFlag=0")
public class InternalResponseCode implements Serializable {

    private static final long serialVersionUID = 6473941024724065216L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "InternalResponseCodeID")
    private Long internalResponseCodeId;

    @Column(name = "InternalResponseCode", unique = true)
    private String internalResponseCode;

    @Column(name = "InternalResponseCodeDescription")
    private String internalResponseCodeDescription;

    @OneToMany(mappedBy = "internalResponseCode", fetch = FetchType.LAZY)
    private Collection<PaymentProcessorInternalResponseCode> paymentProcessorInternalResponseCodes;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "InternalResponseCodeCategoryID")
    private InternalResponseCodeCategory internalResponseCodeCategory;

    @JsonIgnore
    @Column(name = "DeletedFlag")
    private Short deletedFlag = 0;
}
