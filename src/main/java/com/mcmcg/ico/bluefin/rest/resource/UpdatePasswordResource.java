package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdatePasswordResource {
    private String oldPassword;
    @NotBlank(message = "newPassword must not be empty")
    @Pattern(regexp="^.*(?=.*\\d)(?=.*[A-Z]).{8,16}", message = "Password must be between 8 to 16 characters in length and must not contain @ symbol and must contain at least one uppercase letter and one number")
    private String newPassword;
}
