package com.mcmcg.ico.bluefin.persistent;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
// @Entity
// @Table(name = "InternalResponseCodeCategory_Lookup")
public class InternalResponseCodeCategory implements Serializable {

    private static final long serialVersionUID = 7856208964444208101L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "InternalResponseCodeCategoryID")
    private Long internalResponseCodeCategoryId;

    @Column(name = "InternalResponseCodeCategoryName")
    private String internalResponseCodeCategoryName;

    @Column(name = "InternalResponseCodeCategoryDescription")
    private String internalResponseCodeCategoryDescription;

    @JsonIgnore
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "DateCreated", insertable = false)
    private Date createdDate;
}
