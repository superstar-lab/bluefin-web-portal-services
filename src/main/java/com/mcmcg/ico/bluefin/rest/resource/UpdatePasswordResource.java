package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdatePasswordResource {
    private String oldPassword;
    @NotBlank(message = "Please provide a new password for the user")
    @Pattern(regexp="^.*(?=.*\\d)(?=.*[A-Z]).{8,16}", message = "Password must be between 8 to 16 characters in length and must not contain space and must contain at least one uppercase letter and one number and usename and password must not be same")
    private String newPassword;
}
