package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import javax.validation.constraints.NotBlank; 

public class ThirdPartyAppResource implements Serializable {

	private static final long serialVersionUID = 304759584210372543L;

	@NotBlank(message = "Please provide a user name")
	@Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field user name must be alphanumeric")
	private String username;

	@NotBlank(message = "Please provide an email address for the user")
	private String email;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
