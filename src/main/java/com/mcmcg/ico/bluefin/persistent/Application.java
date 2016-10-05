package com.mcmcg.ico.bluefin.persistent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "Application_Lookup")
public class Application {

    @Id
    @Column(name = "ApplicationID")
    private Long applicationId;

    @Column(name = "ApplicationName")
    private String applicationName;

}
