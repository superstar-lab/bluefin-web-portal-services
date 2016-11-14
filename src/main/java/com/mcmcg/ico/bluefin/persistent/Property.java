package com.mcmcg.ico.bluefin.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "Property_Lookup")
public class Property {

    @Id
    @Column(name = "PropertyID")
    private long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Value")
    private String value;

    @Column(name = "Description")
    private String description;

}
