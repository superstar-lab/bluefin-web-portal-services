package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_InternalResponseCode")
@Where(clause = "DeletedFlag=0")
public class PaymentProcessorInternalResponseCode implements Serializable {

    private static final long serialVersionUID = -8456355468578883069L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorInternalResponseCodeID")
    private Long paymentProcessorInternalResponseCodeId;

    @JsonBackReference(value = "internalResponseCode")
    @ManyToOne
    @JoinColumn(name = "InternalResponseCode", referencedColumnName = "InternalResponseCode")
    private InternalResponseCode internalResponseCode;

    @ManyToOne
    @JoinColumn(name = "PaymentProcessorResponseCodeID")
    private PaymentProcessorResponseCode paymentProcessorResponseCode;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;

    @Column(name = "DeletedFlag")
    @JsonIgnore
    private Short deletedFlag = 0;

}