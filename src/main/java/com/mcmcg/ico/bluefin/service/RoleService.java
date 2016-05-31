package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.persistent.Role;
import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<String> getRoles() {
        List<Role> roles = roleRepository.findAll();
        if (roles == null) {
            throw new CustomNotFoundException("Roles information not found");
        }

        List<String> result = new ArrayList<String>();
        for (Role role : roles) {
            result.add(role.getRoleName());
        }
        return result;
    }

}
