package com.mcmcg.ico.bluefin.persistent;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="role")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer roleId;
    
    private String roleName;
    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;
 
    @ManyToMany
    @JoinTable(
        name = "role_permission", 
        joinColumns = @JoinColumn(name = "role_id"), 
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Collection<Permission> permissions;   

    public Role(){
    }
    
    public Role(String roleName){
        this.roleName = roleName;
    }
}
