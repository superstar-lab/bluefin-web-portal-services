package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class SessionRequestResource implements Serializable {

    private static final long serialVersionUID = 4390649295743233923L;

    @NotBlank(message = "username cannot be empty or null")
    private String username;
}
