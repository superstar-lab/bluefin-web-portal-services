package com.mcmcg.ico.bluefin.rest.resource;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class CommonUserResource {
	@NotBlank(message = "Please provide a user name")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field user name must be alphanumeric")
	private String username;

	@NotBlank(message = "Please provide a first name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field first name must be alphanumeric")
	private String firstName;

	@NotBlank(message = "Please provide a last name for the user")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field last name must be alphanumeric")
	private String lastName;

	@NotBlank(message = "Please provide an email address for the user")
	private String email;
	
	public CommonUserResource() {
		// Default Constructor
	}

}
