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

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "PaymentProcessor_InternalStatusCode")
public class PaymentProcessorInternalStatusCode implements Serializable {

    private static final long serialVersionUID = 7430312454113990013L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PaymentProcessorInternalStatusCodeID")
    private Long paymentProcessorInternalStatusCodeId;

    @JsonBackReference(value = "internalStatusCode")
    @ManyToOne
    @JoinColumn(name = "InternalStatusCode", referencedColumnName = "InternalStatusCode")
    private InternalStatusCode internalStatusCode;

    @ManyToOne
    @JoinColumn(name = "PaymentProcessorStatusCodeID")
    private PaymentProcessorStatusCode paymentProcessorStatusCode;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;
}
