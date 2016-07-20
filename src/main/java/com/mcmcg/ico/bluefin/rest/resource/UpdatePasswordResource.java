package com.mcmcg.ico.bluefin.rest.resource;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdatePasswordResource {
    private String oldPassword;
    @NotBlank(message = "newPassword must not be empty")
    private String newPassword;
}
