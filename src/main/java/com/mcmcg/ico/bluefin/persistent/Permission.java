package com.mcmcg.ico.bluefin.persistent;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer permissionId;
    private String permissionName;
    private String description;
 
    @ManyToMany(mappedBy = "permissions")
    private Collection<Role> roles;
    
    public Permission(){
    }
    
    public Permission(String permissionName){
        this.permissionName = permissionName;
    }
}
