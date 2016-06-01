package com.mcmcg.ico.bluefin.persistent;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Entity
@Table(name = "user_legal_entity")
public class UserLegalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long paymentProcessorMechantId;

    @ManyToOne
    @JoinColumn(name = "legal_entity_app_id")
    private LegalEntityApp legalEntityApp;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date createdDate;

}
