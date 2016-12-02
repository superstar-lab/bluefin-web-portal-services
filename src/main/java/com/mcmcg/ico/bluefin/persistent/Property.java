package com.mcmcg.ico.bluefin.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "ApplicationProperty_Lookup")
public class Property {

    @Id
    @Column(name = "ApplicationpropertyID")
    private long id;

    @Column(name = "ApplicationPropertyName")
    private String name;

    @Column(name = "ApplicationPropertyValue")
    private String value;
    
    @Column(name = "Description")
    private String description;
    
    @JsonIgnore
    @Transient
    @Column(name = "DataType")
    private String dataType;
    
    @JsonIgnore
    @Transient
    @Column(name = "DateCreated")
    private DateTime created;
    
    @JsonIgnore
    @Transient
    @Column(name = "DateModified")
    private DateTime modified;
    
    @JsonIgnore
    @Transient
    @Column(name = "ModifiedBy")
    private String modifiedBy;

}
